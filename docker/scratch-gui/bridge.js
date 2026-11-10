/**
 * ============================================================================
 * Scratch GUI postMessage 桥接脚本
 * ============================================================================
 *
 * 使用方式：
 * 将此脚本注入到 scratch-gui 的 index.html 中（在 </body> 前添加 <script src="bridge.js"></script>）
 * 或在 scratch-gui 编译前将此脚本内容合并到 src/ 中
 *
 * 功能：
 * 1. 监听父页面（magao-platform）发来的 postMessage 指令
 * 2. 通过 scratch-gui 的 window.vm (VirtualMachine) 执行操作
 * 3. 将操作结果通过 postMessage 回传给父页面
 * 4. 支持竞赛模式：限制积木类别、锁定编辑器
 * ============================================================================
 */

(function () {
  'use strict';

  // ==================== 常量 ====================
  const PARENT_ORIGIN = '*'; // 生产环境建议限制为具体域名
  const VM_READY_CHECK_INTERVAL = 200; // VM 就绪检查间隔（ms）
  const VM_READY_TIMEOUT = 15000; // VM 就绪超时（ms）

  // ==================== 状态 ====================
  let vm = null; // Scratch VirtualMachine 实例
  let isLocked = false; // 编辑器是否锁定
  let isReady = false; // 桥接是否就绪

  // ==================== 工具函数 ====================

  /** 向父页面发送消息 */
  function postToParent(type, payload) {
    try {
      window.parent.postMessage({ type, payload }, PARENT_ORIGIN);
    } catch (e) {
      console.error('[ScratchBridge] postToParent 失败:', e);
    }
  }

  /** 等待 VM 就绪 */
  function waitForVM() {
    return new Promise((resolve, reject) => {
      // 如果 VM 已存在，直接返回
      if (window.vm) {
        vm = window.vm;
        resolve(vm);
        return;
      }

      let elapsed = 0;
      const timer = setInterval(() => {
        elapsed += VM_READY_CHECK_INTERVAL;
        if (window.vm) {
          clearInterval(timer);
          vm = window.vm;
          resolve(vm);
        } else if (elapsed >= VM_READY_TIMEOUT) {
          clearInterval(timer);
          reject(new Error('VM 初始化超时'));
        }
      }, VM_READY_CHECK_INTERVAL);
    });
  }

  /** 将 ArrayBuffer 转为 Base64 */
  function arrayBufferToBase64(buffer) {
    const bytes = new Uint8Array(buffer);
    let binary = '';
    for (let i = 0; i < bytes.byteLength; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    return btoa(binary);
  }

  // ==================== 消息处理器 ====================

  const messageHandlers = {

    /** 初始化 */
    INIT: async function (payload) {
      try {
        await waitForVM();
        isReady = true;

        // 竞赛模式处理
        if (payload && payload.examMode) {
          applyExamMode(payload);
        }

        postToParent('READY', { success: true });
      } catch (err) {
        postToParent('ERROR', { message: 'VM 初始化失败: ' + err.message });
      }
    },

    /** 加载项目 */
    LOAD_PROJECT: async function (payload) {
      try {
        if (!vm) await waitForVM();
        if (!payload || !payload.projectJson) {
          postToParent('ERROR', { message: '缺少项目数据' });
          return;
        }

        // projectJson 可能是 base64 编码的 sb3 或纯 JSON
        let buffer;
        if (payload.projectJson.startsWith('UEs')) {
          // base64 编码的 zip (sb3)
          const binary = atob(payload.projectJson);
          const bytes = new Uint8Array(binary.length);
          for (let i = 0; i < binary.length; i++) {
            bytes[i] = binary.charCodeAt(i);
          }
          buffer = bytes.buffer;
        } else {
          // 纯 JSON 项目数据
          const jsonStr = typeof payload.projectJson === 'string'
            ? payload.projectJson
            : JSON.stringify(payload.projectJson);
          buffer = new TextEncoder().encode(jsonStr).buffer;
        }

        await vm.loadProject(buffer);
        postToParent('PROJECT_DATA', { success: true });
      } catch (err) {
        postToParent('ERROR', { message: '加载项目失败: ' + err.message });
      }
    },

    /** 保存项目 */
    SAVE_PROJECT: async function () {
      try {
        if (!vm) await waitForVM();

        const sb3Blob = await vm.saveProjectSb3();
        const arrayBuffer = await sb3Blob.arrayBuffer();
        const base64 = arrayBufferToBase64(arrayBuffer);

        // 获取积木 JSON（用于判题）
        const blocksJson = getBlocksJson();

        const projectData = {
          projectJson: base64,
          blocksJson: blocksJson,
          timestamp: Date.now(),
        };

        postToParent('PROJECT_SAVED', projectData);
      } catch (err) {
        postToParent('ERROR', { message: '保存项目失败: ' + err.message });
      }
    },

    /** 获取项目数据（用于导出 sb3） */
    GET_PROJECT_DATA: async function () {
      try {
        if (!vm) await waitForVM();

        const sb3Blob = await vm.saveProjectSb3();
        const arrayBuffer = await sb3Blob.arrayBuffer();
        const base64 = arrayBufferToBase64(arrayBuffer);

        const blocksJson = getBlocksJson();

        postToParent('PROJECT_DATA', {
          projectJson: base64,
          blocksJson: blocksJson,
          timestamp: Date.now(),
        });
      } catch (err) {
        postToParent('ERROR', { message: '获取项目数据失败: ' + err.message });
      }
    },

    /** 运行项目 */
    RUN_PROJECT: function () {
      if (isLocked) return;
      if (vm) {
        vm.greenFlag();
        postToParent('RUN_STARTED', {});
      }
    },

    /** 停止项目 */
    STOP_PROJECT: function () {
      if (vm) {
        vm.stopAll();
        postToParent('RUN_STOPPED', {});
      }
    },

    /** 锁定编辑器 */
    LOCK_EDITOR: function (payload) {
      if (payload && payload.locked) {
        isLocked = true;
        if (vm) vm.stopAll();
        // 添加锁定遮罩
        applyLockOverlay();
      }
    },

    /** 限制积木类别（竞赛模式） */
    RESTRICT_BLOCKS: function (payload) {
      if (!payload || !payload.allowedCategories) return;
      restrictBlockCategories(payload.allowedCategories);
    },

    /** 切屏通知 */
    SCREEN_SWITCH: function () {
      // 可在此添加切屏后的处理逻辑
    },
  };

  // ==================== 辅助函数 ====================

  /** 获取积木 JSON（用于判题分析） */
  function getBlocksJson() {
    if (!vm || !vm.runtime) return '{}';

    try {
      const targets = vm.runtime.targets;
      const blocks = {};

      targets.forEach(function (target) {
        if (target.blocks) {
          const targetBlocks = target.blocks._blocks || target.blocks;
          Object.assign(blocks, targetBlocks);
        }
      });

      return JSON.stringify(blocks);
    } catch (err) {
      console.error('[ScratchBridge] 获取积木 JSON 失败:', err);
      return '{}';
    }
  }

  /** 应用竞赛模式 */
  function applyExamMode(config) {
    // 隐藏菜单栏
    const menuBar = document.querySelector('[class*="menu-bar"]');
    if (menuBar) menuBar.style.display = 'none';

    // 隐藏分享按钮
    const shareBtn = document.querySelector('[class*="share-button"]');
    if (shareBtn) shareBtn.style.display = 'none';

    // 隐藏个人资料按钮
    const profileBtn = document.querySelector('[class*="account-nav"]');
    if (profileBtn) profileBtn.style.display = 'none';

    // 隐藏 Logo
    const logo = document.querySelector('[class*="logo-img"]');
    if (logo) logo.style.display = 'none';

    // 限制积木类别
    if (config.allowedCategories && config.allowedCategories.length > 0) {
      restrictBlockCategories(config.allowedCategories);
    }
  }

  /** 限制积木类别 */
  function restrictBlockCategories(allowed) {
    const allCategories = [
      'motion', 'looks', 'sound', 'events', 'control',
      'sensing', 'operators', 'variables', 'myblocks',
      'pen', 'music', 'videoSensing', 'translate'
    ];
    const hidden = allCategories.filter(function (c) { return allowed.indexOf(c) === -1; });

    const style = document.createElement('style');
    style.id = 'scratch-block-restrict';
    const selectors = hidden.map(function (c) {
      return '[data-category="' + c + '"]';
    }).join(', ');

    if (selectors) {
      style.textContent = selectors + ' { display: none !important; }';
      document.head.appendChild(style);
    }
  }

  /** 应用锁定遮罩 */
  function applyLockOverlay() {
    // 移除已有遮罩
    const existing = document.getElementById('scratch-lock-overlay');
    if (existing) existing.remove();

    const overlay = document.createElement('div');
    overlay.id = 'scratch-lock-overlay';
    overlay.style.cssText = 'position:fixed;inset:0;background:rgba(255,255,255,0.9);' +
      'display:flex;align-items:center;justify-content:center;z-index:99999;' +
      'font-size:18px;color:#909399;flex-direction:column;gap:12px;';
    overlay.innerHTML = '<div style="font-size:48px;">&#x1f512;</div>' +
      '<p>作品已提交，编辑器已锁定</p>';
    document.body.appendChild(overlay);
  }

  // ==================== 事件监听 ====================

  /** 监听父页面消息 */
  window.addEventListener('message', function (event) {
    if (!event.data || typeof event.data.type !== 'string') return;

    const msg = event.data;
    const handler = messageHandlers[msg.type];

    if (handler) {
      // 异步处理器自动 catch
      const result = handler(msg.payload);
      if (result && typeof result.catch === 'function') {
        result.catch(function (err) {
          postToParent('ERROR', { message: err.message });
        });
      }
    }
  });

  // ==================== 自动初始化 ====================

  // scratch-gui 加载完成后，VM 会自动挂载到 window.vm
  // 我们等待 VM 就绪后通知父页面
  if (document.readyState === 'complete') {
    waitForVM().then(function () {
      isReady = true;
      postToParent('READY', { success: true });
    }).catch(function (err) {
      postToParent('ERROR', { message: err.message });
    });
  } else {
    window.addEventListener('load', function () {
      waitForVM().then(function () {
        isReady = true;
        postToParent('READY', { success: true });
      }).catch(function (err) {
        postToParent('ERROR', { message: err.message });
      });
    });
  }

  console.log('[ScratchBridge] 桥接脚本已加载，等待 VM 初始化...');
})();
