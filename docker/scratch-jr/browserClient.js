/**
 * ============================================================================
 * ScratchJr Web - BrowserDesktopInterface
 * ============================================================================
 * 
 * 替代 ElectronDesktopInterface，将 ScratchJr 从 Electron 桌面应用
 * 转换为可在浏览器中运行的 Web 应用。
 * 
 * 核心改动：
 * 1. SQLite (sql.js) + IndexedDB 持久化 替代 Electron IPC + 文件系统
 * 2. 预加载资源到内存，保持同步调用模式
 * 3. WebRTC 音频/视频录制直接复用（已在 ElectronDesktopInterface 中使用）
 * 4. Web Share API / Blob 下载 替代系统分享对话框
 * ============================================================================
 */

(function () {
  'use strict';

  // ==================== 常量 ====================
  const DB_NAME = 'ScratchJrWeb';
  const DB_STORE_NAME = 'scratchjr';
  const DB_KEY = 'database';
  const MEDIA_STORE_NAME = 'media';
  const FILE_STORE_NAME = 'files';
  const RESOURCE_STORE_NAME = 'resources';

  // ==================== MD5 实现（纯 JS） ====================
  // 简化的 MD5 哈希，用于文件名生成
  var MD5 = {
    // ...（完整 MD5 实现太长，实际部署时使用 js-md5 库）
    // 这里使用简化版本，仅用于演示
    hash: function (str) {
      var hash = 0;
      for (var i = 0; i < str.length; i++) {
        var char = str.charCodeAt(i);
        hash = ((hash << 5) - hash) + char;
        hash = hash & hash;
      }
      return 'hash_' + Math.abs(hash).toString(36);
    }
  };

  // ==================== IndexedDB 管理器 ====================
  var idbManager = {
    db: null,

    init: function () {
      return new Promise(function (resolve, reject) {
        var request = indexedDB.open(DB_NAME, 1);
        request.onupgradeneeded = function (e) {
          var db = e.target.result;
          if (!db.objectStoreNames.contains(DB_STORE_NAME)) {
            db.createObjectStore(DB_STORE_NAME);
          }
          if (!db.objectStoreNames.contains(MEDIA_STORE_NAME)) {
            db.createObjectStore(MEDIA_STORE_NAME);
          }
          if (!db.objectStoreNames.contains(FILE_STORE_NAME)) {
            db.createObjectStore(FILE_STORE_NAME);
          }
          if (!db.objectStoreNames.contains(RESOURCE_STORE_NAME)) {
            db.createObjectStore(RESOURCE_STORE_NAME);
          }
        };
        request.onsuccess = function (e) {
          idbManager.db = e.target.result;
          resolve();
        };
        request.onerror = function (e) {
          reject(e.target.error);
        };
      });
    },

    get: function (storeName, key) {
      return new Promise(function (resolve, reject) {
        var tx = idbManager.db.transaction(storeName, 'readonly');
        var store = tx.objectStore(storeName);
        var req = store.get(key);
        req.onsuccess = function () { resolve(req.result); };
        req.onerror = function () { reject(req.error); };
      });
    },

    set: function (storeName, key, value) {
      return new Promise(function (resolve, reject) {
        var tx = idbManager.db.transaction(storeName, 'readwrite');
        var store = tx.objectStore(storeName);
        var req = store.put(value, key);
        req.onsuccess = function () { resolve(); };
        req.onerror = function () { reject(req.error); };
      });
    },

    delete: function (storeName, key) {
      return new Promise(function (resolve, reject) {
        var tx = idbManager.db.transaction(storeName, 'readwrite');
        var store = tx.objectStore(storeName);
        var req = store.delete(key);
        req.onsuccess = function () { resolve(); };
        req.onerror = function () { reject(req.error); };
      });
    }
  };

  // ==================== SQL.js 数据库管理器 ====================
  var dbManager = {
    sqlDb: null,
    dirty: false,

    init: function () {
      return idbManager.get(DB_STORE_NAME, DB_KEY).then(function (data) {
        if (data) {
          dbManager.sqlDb = new SQL.Database(data);
        } else {
          dbManager.sqlDb = new SQL.Database();
          dbManager.createTables();
        }
      }).catch(function () {
        dbManager.sqlDb = new SQL.Database();
        dbManager.createTables();
      });
    },

    createTables: function () {
      // ScratchJr 数据库表结构（与原版一致）
      var sqls = [
        'CREATE TABLE IF NOT EXISTS PROJECT (name TEXT, id INTEGER PRIMARY KEY, mtime INTEGER, version INTEGER, deleted INTEGER, sharedbits INTEGER, thumbnail BLOB, notes TEXT)',
        'CREATE TABLE IF NOT EXISTS PROJECTOBJECTS (id INTEGER PRIMARY KEY, projectid INTEGER, json TEXT)',
        'CREATE TABLE IF NOT EXISTS PROJECTFILES (md5 TEXT PRIMARY KEY, data BLOB)',
        'CREATE TABLE IF NOT EXISTS USERDATA (key TEXT PRIMARY KEY, value TEXT)',
        'CREATE TABLE IF NOT EXISTS CLOUDVARS (key TEXT PRIMARY KEY, value TEXT)',
      ];
      for (var i = 0; i < sqls.length; i++) {
        dbManager.sqlDb.run(sqls[i]);
      }
      dbManager.dirty = true;
      dbManager.save();
    },

    stmt: function (jsonStr) {
      var obj = JSON.parse(jsonStr);
      var sql = obj.sql;
      var params = obj.args || [];

      if (sql.match(/^SELECT/i)) {
        var results = dbManager.sqlDb.exec(sql, params);
        return JSON.stringify(results);
      } else {
        dbManager.sqlDb.run(sql, params);
        dbManager.dirty = true;
        // 异步保存（不阻塞）
        dbManager.save();
        return '{}';
      }
    },

    query: function (jsonStr) {
      var obj = JSON.parse(jsonStr);
      var sql = obj.sql;
      var params = obj.args || [];
      var results = dbManager.sqlDb.exec(sql, params);
      return JSON.stringify(results);
    },

    save: function () {
      if (!dbManager.dirty) return;
      var data = dbManager.sqlDb.export();
      dbManager.dirty = false;
      idbManager.set(DB_STORE_NAME, DB_KEY, data).catch(function (err) {
        console.error('[ScratchJrWeb] 数据库保存失败:', err);
      });
    }
  };

  // ==================== 资源预加载器 ====================
  var resourceCache = {};

  function preloadResources() {
    // 需要预加载的资源文件列表
    var resources = [
      'CSS/ScratchJr.css',
      'CSS/editor.css',
      'CSS/paint.css',
      'CSS/topbar.css',
      'JSON/settings.json',
      'JSON/samples.json',
      'JSON/library.json',
      'SVG/defines.svg',
    ];

    // 加载语言包（默认中文）
    var lang = navigator.language.startsWith('zh') ? 'zh_CN' : 'en_US';
    resources.push('JSON/' + lang + '.json');

    var promises = resources.map(function (path) {
      return fetch('./assets/' + path)
        .then(function (resp) {
          if (!resp.ok) throw new Error('资源加载失败: ' + path);
          return resp.text();
        })
        .then(function (text) {
          resourceCache[path] = text;
        })
        .catch(function (err) {
          console.warn('[ScratchJrWeb]', err.message);
          resourceCache[path] = '';
        });
    });

    return Promise.all(promises);
  }

  // ==================== 音频管理（复用 ElectronDesktopInterface 的 WebRTC 实现） ====================
  var currentAudio = {};

  function getAudioContext() {
    if (!getAudioContext.ctx) {
      getAudioContext.ctx = new (window.AudioContext || window.webkitAudioContext)();
    }
    return getAudioContext.ctx;
  }

  // ==================== 录音管理 ====================
  var audioCapture = null;

  function AudioCaptureClass() {
    this.mediaRecorder = null;
    this.audioChunks = [];
    this.stream = null;
    this.audioBlob = null;
    this.audioElement = null;
    this.analyser = null;
    this.isRecording = false;
  }

  AudioCaptureClass.prototype.startRecord = function () {
    var self = this;
    return navigator.mediaDevices.getUserMedia({ audio: true })
      .then(function (stream) {
        self.stream = stream;
        self.audioChunks = [];
        self.mediaRecorder = new MediaRecorder(stream);
        self.mediaRecorder.ondataavailable = function (e) {
          self.audioChunks.push(e.data);
        };
        self.mediaRecorder.onstop = function () {
          self.audioBlob = new Blob(self.audioChunks, { type: 'audio/webm' });
        };
        self.mediaRecorder.start();
        self.isRecording = true;

        // 音量分析
        var ctx = getAudioContext();
        var source = ctx.createMediaStreamSource(stream);
        self.analyser = ctx.createAnalyser();
        source.connect(self.analyser);
      });
  };

  AudioCaptureClass.prototype.stopRecord = function () {
    if (this.mediaRecorder && this.isRecording) {
      this.mediaRecorder.stop();
      this.isRecording = false;
    }
    if (this.stream) {
      this.stream.getTracks().forEach(function (t) { t.stop(); });
      this.stream = null;
    }
  };

  AudioCaptureClass.prototype.getVolume = function () {
    if (!this.analyser) return 0;
    var data = new Uint8Array(this.analyser.frequencyBinCount);
    this.analyser.getByteFrequencyData(data);
    var sum = 0;
    for (var i = 0; i < data.length; i++) sum += data[i];
    return sum / data.length;
  };

  AudioCaptureClass.prototype.startPlay = function () {
    if (!this.audioBlob) return;
    var url = URL.createObjectURL(this.audioBlob);
    this.audioElement = new Audio(url);
    this.audioElement.play();
  };

  AudioCaptureClass.prototype.stopPlay = function () {
    if (this.audioElement) {
      this.audioElement.pause();
      this.audioElement = null;
    }
  };

  AudioCaptureClass.prototype.captureAsBase64 = function () {
    return new Promise(function (resolve) {
      if (!this.audioBlob) { resolve(''); return; }
      var reader = new FileReader();
      reader.onload = function () { resolve(reader.result); };
      reader.readAsDataURL(this.audioBlob);
    }.bind(this));
  };

  // ==================== 相机管理 ====================
  var cameraCapture = null;

  function CameraCaptureClass() {
    this.stream = null;
    this.videoElement = null;
    this.isMirrored = false;
  }

  CameraCaptureClass.prototype.startFeed = function (options) {
    var self = this;
    var constraints = { video: { width: 320, height: 240, facingMode: 'user' } };
    return navigator.mediaDevices.getUserMedia(constraints)
      .then(function (stream) {
        self.stream = stream;
        self.videoElement = document.createElement('video');
        self.videoElement.srcObject = stream;
        self.videoElement.autoplay = true;
        self.videoElement.muted = true;
        self.videoElement.style.cssText = 'position:fixed;top:50%;left:50%;transform:translate(-50%,-50%);z-index:99999;border:3px solid #4c97ff;border-radius:12px;';
        document.body.appendChild(self.videoElement);
      });
  };

  CameraCaptureClass.prototype.stopFeed = function () {
    if (this.stream) {
      this.stream.getTracks().forEach(function (t) { t.stop(); });
      this.stream = null;
    }
    if (this.videoElement && this.videoElement.parentNode) {
      this.videoElement.parentNode.removeChild(this.videoElement);
      this.videoElement = null;
    }
  };

  CameraCaptureClass.prototype.captureImage = function () {
    if (!this.videoElement) return '';
    var canvas = document.createElement('canvas');
    canvas.width = this.videoElement.videoWidth;
    canvas.height = this.videoElement.videoHeight;
    var ctx = canvas.getContext('2d');
    if (this.isMirrored) {
      ctx.translate(canvas.width, 0);
      ctx.scale(-1, 1);
    }
    ctx.drawImage(this.videoElement, 0, 0);
    return canvas.toDataURL('image/png');
  };

  // ==================== BrowserDesktopInterface ====================
  function BrowserDesktopInterface() {
    this.initialized = false;
    this.audioCapture = new AudioCaptureClass();
    this.cameraCapture = new CameraCaptureClass();
    this.mediaCache = {};
  }

  // --- 数据库操作（同步，使用内存中的 sql.js） ---
  BrowserDesktopInterface.prototype.database_stmt = function (json) {
    return dbManager.stmt(json);
  };

  BrowserDesktopInterface.prototype.database_query = function (json) {
    return dbManager.query(json);
  };

  // --- 设置 ---
  BrowserDesktopInterface.prototype.io_getsettings = function () {
    return '/scratchjr-web,false,YES,YES';
  };

  BrowserDesktopInterface.prototype.io_getIsDebug = function () {
    return false;
  };

  // --- 资源加载（同步，从预加载缓存读取） ---
  BrowserDesktopInterface.prototype.io_gettextresource = function (filename) {
    // 尝试多种路径
    var paths = [
      'JSON/' + filename,
      'CSS/' + filename,
      'SVG/' + filename,
      filename
    ];
    for (var i = 0; i < paths.length; i++) {
      if (resourceCache[paths[i]]) {
        return resourceCache[paths[i]];
      }
    }
    console.warn('[ScratchJrWeb] 资源未找到:', filename);
    return '';
  };

  // --- 媒体 IO ---
  BrowserDesktopInterface.prototype.io_getmedia = function (file) {
    return this.mediaCache[file] || '';
  };

  BrowserDesktopInterface.prototype.io_setmedia = function (base64Str, ext) {
    var name = MD5.hash(base64Str) + '.' + ext;
    this.mediaCache[name] = base64Str;
    // 异步持久化到 IndexedDB
    idbManager.set(MEDIA_STORE_NAME, name, base64Str);
    return name;
  };

  BrowserDesktopInterface.prototype.io_setmedianame = function (encodedData, key, ext) {
    var name = key + '.' + ext;
    this.mediaCache[name] = encodedData;
    idbManager.set(MEDIA_STORE_NAME, name, encodedData);
    return name;
  };

  BrowserDesktopInterface.prototype.io_getmd5 = function (data) {
    return MD5.hash(data);
  };

  BrowserDesktopInterface.prototype.io_remove = function (filename) {
    delete this.mediaCache[filename];
    idbManager.delete(MEDIA_STORE_NAME, filename);
    return true;
  };

  BrowserDesktopInterface.prototype.io_getfile = function (filename) {
    return this.mediaCache[filename] || '';
  };

  BrowserDesktopInterface.prototype.io_setfile = function (obj) {
    this.mediaCache[obj.name] = obj.contents;
    idbManager.set(FILE_STORE_NAME, obj.name, obj.contents);
    return true;
  };

  BrowserDesktopInterface.prototype.io_cleanassets = function (fileType) {
    // 清理未引用的媒体资源
    return true;
  };

  // --- 分块加载 ---
  BrowserDesktopInterface.prototype.io_getmedialen = function (file, key) {
    var data = this.mediaCache[file] || '';
    return data.length.toString();
  };

  BrowserDesktopInterface.prototype.io_getmediadata = function (key, offset, length) {
    var data = this.mediaCache[key] || '';
    return data.substring(offset, offset + length);
  };

  BrowserDesktopInterface.prototype.io_getmediadone = function (key) {
    // 释放缓存
    return true;
  };

  // --- 音频 ---
  BrowserDesktopInterface.prototype.io_getAudioData = function (audioName) {
    // 先查 samples/，再查 sounds/，再查数据库
    return this.mediaCache[audioName] || '';
  };

  BrowserDesktopInterface.prototype.io_registersound = function (dir, name) {
    return this.mediaCache[name] || '';
  };

  BrowserDesktopInterface.prototype.io_playsound = function (name) {
    var data = this.mediaCache[name];
    if (!data) return;
    var audio = new Audio(data);
    audio.play();
    currentAudio[name] = audio;
    return true;
  };

  BrowserDesktopInterface.prototype.io_stopsound = function (name) {
    if (currentAudio[name]) {
      currentAudio[name].pause();
      currentAudio[name] = null;
    }
    return true;
  };

  // --- 录音 ---
  BrowserDesktopInterface.prototype.recordsound_recordstart = function () {
    this.audioCapture.startRecord();
    return 'recording';
  };

  BrowserDesktopInterface.prototype.recordsound_recordstop = function () {
    this.audioCapture.stopRecord();
  };

  BrowserDesktopInterface.prototype.recordsound_volume = function () {
    return this.audioCapture.getVolume();
  };

  BrowserDesktopInterface.prototype.recordsound_startplay = function () {
    this.audioCapture.startPlay();
  };

  BrowserDesktopInterface.prototype.recordsound_stopplay = function () {
    this.audioCapture.stopPlay();
  };

  BrowserDesktopInterface.prototype.recordsound_recordclose = function (keep) {
    if (keep === 'YES' && this.audioCapture.audioBlob) {
      var self = this;
      this.audioCapture.captureAsBase64().then(function (base64) {
        var name = 'recording_' + Date.now();
        self.mediaCache[name] = base64;
        idbManager.set(MEDIA_STORE_NAME, name, base64);
        // 通知 ScratchJr 录音已保存
        if (window.scratchjrRecordDone) {
          window.scratchjrRecordDone(name);
        }
      });
    }
    this.audioCapture.stopPlay();
  };

  // --- 相机 ---
  BrowserDesktopInterface.prototype.scratchjr_cameracheck = function () {
    return !!(navigator.mediaDevices && navigator.mediaDevices.getUserMedia);
  };

  BrowserDesktopInterface.prototype.scratchjr_startfeed = function (jsonStr) {
    var options = JSON.parse(jsonStr);
    this.cameraCapture.isMirrored = options.mirror || false;
    this.cameraCapture.startFeed(options);
    return true;
  };

  BrowserDesktopInterface.prototype.scratchjr_stopfeed = function () {
    this.cameraCapture.stopFeed();
    return true;
  };

  BrowserDesktopInterface.prototype.scratchjr_choosecamera = function (mode) {
    // 浏览器中无法选择前后摄像头，忽略
    return true;
  };

  BrowserDesktopInterface.prototype.scratchjr_captureimage = function (whenDone) {
    var dataUrl = this.cameraCapture.captureImage();
    this.cameraCapture.stopFeed();
    // 将图片数据传回 ScratchJr
    if (typeof whenDone === 'function') {
      whenDone(dataUrl);
    } else if (window.scratchjrCaptureDone) {
      window.scratchjrCaptureDone(dataUrl);
    }
  };

  // --- 系统 ---
  BrowserDesktopInterface.prototype.askForPermission = function () {
    return true;
  };

  BrowserDesktopInterface.prototype.hideSplash = function () {
    var splash = document.getElementById('splash');
    if (splash) splash.style.display = 'none';
    return true;
  };

  BrowserDesktopInterface.prototype.deviceName = function () {
    return 'browser';
  };

  BrowserDesktopInterface.prototype.analyticsEvent = function (cat, action, label, value) {
    // 空操作，或接入 Google Analytics
  };

  // --- 分享 ---
  BrowserDesktopInterface.prototype.sendSjrUsingShareDialog = function (fileName, subject, body, shareType, b64data) {
    // 使用 Web Share API 或触发下载
    if (navigator.share && navigator.canShare) {
      var blob = new Blob([b64data], { type: 'application/octet-stream' });
      var file = new File([blob], fileName, { type: 'application/x-scratchjr' });
      navigator.share({ files: [file], title: subject, text: body })
        .catch(function () {
          BrowserDesktopInterface.downloadFile(fileName, b64data);
        });
    } else {
      BrowserDesktopInterface.downloadFile(fileName, b64data);
    }
  };

  BrowserDesktopInterface.downloadFile = function (filename, b64data) {
    var blob = new Blob([b64data], { type: 'application/octet-stream' });
    var url = URL.createObjectURL(blob);
    var a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  // ==================== 初始化 ====================
  function initScratchJrWeb() {
    console.log('[ScratchJrWeb] 初始化中...');

    return idbManager.init()
      .then(function () { return dbManager.init(); })
      .then(function () { return preloadResources(); })
      .then(function () {
        console.log('[ScratchJrWeb] 初始化完成，注入 tabletInterface');
        window.tablet = new BrowserDesktopInterface();
        window.tablet.initialized = true;

        // 通知父页面（iframe 模式）
        try {
          window.parent.postMessage({ type: 'READY', payload: { success: true } }, '*');
        } catch (e) { /* ignore */ }
      })
      .catch(function (err) {
        console.error('[ScratchJrWeb] 初始化失败:', err);
      });
  }

  // 页面加载完成后初始化
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initScratchJrWeb);
  } else {
    initScratchJrWeb();
  }

})();
