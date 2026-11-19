<template>
  <!-- 法律合规页面：隐私政策 / 用户协议 / 儿童个人信息保护规则 -->
  <div class="legal-page">
    <div class="legal-container">
      <div class="legal-header">
        <h1>{{ current.title }}</h1>
        <p class="legal-updated">更新日期：{{ updatedAt }}　|　生效日期：{{ updatedAt }}</p>
      </div>

      <nav class="legal-tabs">
        <router-link v-for="t in tabs" :key="t.type" :to="`/legal/${t.type}`" class="legal-tab" :class="{ active: type === t.type }">
          {{ t.label }}
        </router-link>
      </nav>

      <article class="legal-body">
        <p class="legal-intro">{{ current.intro }}</p>
        <section v-for="(sec, i) in current.sections" :key="i" class="legal-section">
          <h2>{{ i + 1 }}. {{ sec.title }}</h2>
          <p v-for="(p, j) in sec.paragraphs" :key="j">{{ p }}</p>
          <ul v-if="sec.items?.length">
            <li v-for="(it, k) in sec.items" :key="k">{{ it }}</li>
          </ul>
        </section>

        <div class="legal-contact">
          <p><strong>联系我们</strong></p>
          <p>如对本{{ current.short }}有任何疑问、意见或投诉，请通过页面底部公布的客服电话或邮箱联系我们，我们将在 15 个工作日内回复。</p>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * LegalPage - 法律合规文本页（合规必需 P0-19）
 * 依据《个人信息保护法》《儿童个人信息网络保护规定》《未成年人保护法》拟定。
 * 通过路由 /legal/:type 区分：privacy 隐私政策 / agreement 用户协议 / children 儿童个人信息保护规则。
 */
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const updatedAt = '2026年11月19日'

const type = computed(() => {
  const t = String(route.params.type || 'privacy')
  return ['privacy', 'agreement', 'children'].includes(t) ? t : 'privacy'
})

interface LegalSection { title: string; paragraphs: string[]; items?: string[] }
interface LegalDoc { title: string; short: string; intro: string; sections: LegalSection[] }

const docs: Record<string, LegalDoc> = {
  privacy: {
    title: '隐私政策',
    short: '隐私政策',
    intro: '码高少儿编程教育平台（以下简称"我们"）重视并保护每一位用户尤其是未成年人的个人信息安全。本政策说明我们如何收集、使用、存储和保护您的个人信息。使用本平台即表示您已阅读并同意本政策。',
    sections: [
      {
        title: '我们收集的信息',
        paragraphs: ['为实现账号注册、课程学习、赛事报名与支付等核心功能，我们仅在必要范围内收集以下信息：'],
        items: [
          '账号信息：手机号、昵称、密码（加密存储）、头像。',
          '身份信息：真实姓名、年龄或出生日期（用于未成年人识别与监护人同意）。',
          '监护人信息：家长手机号（未成年人账号必需，用于监护人确认与重要通知）。',
          '学习数据：课程进度、练习与考试作答记录、作品、积分与成长数据。',
          '交易信息：订单号、商品、金额、支付状态、退款记录（支付凭证由支付机构处理，我们不存储银行卡号等敏感支付信息）。',
          '技术信息：IP 地址、设备型号、浏览器类型、操作日志（用于安全审计与故障排查）。',
        ],
      },
      {
        title: '我们如何使用信息',
        paragraphs: ['我们仅将收集的个人信息用于以下目的，不会超出该范围使用：'],
        items: [
          '创建并维护账号、提供课程与赛事服务、记录学习进度与成绩。',
          '处理订单、支付、退款及售后服务。',
          '进行安全审计、防范欺诈与账号盗用、保障平台与用户资产安全。',
          '在取得同意的前提下，向监护人推送学习报告与重要通知。',
          '遵守法律法规要求或响应监管机构依法提出的要求。',
        ],
      },
      {
        title: '信息的共享与披露',
        paragraphs: [
          '我们不会出售您的个人信息。仅在以下情形下共享：',
        ],
        items: [
          '与为我们提供支付、短信、云存储、视频点播、代码评测等服务的第三方服务商共享完成对应功能所必需的最小信息；该等服务商须承担同等保密义务。',
          '依照法律法规、司法机关或行政机关的合法要求披露。',
          '在发生合并、收购等情形时，如涉及个人信息转移，我们将事先告知并依法保障您的权利。',
        ],
      },
      {
        title: '信息安全与存储',
        paragraphs: [
          '我们采用密码加密存储、传输加密（HTTPS）、最小权限访问控制、操作审计日志等技术与管理措施保护信息安全。个人信息存储于中华人民共和国境内。我们仅在实现目的所必需的期限内保留信息，法律另有规定的除外。',
        ],
      },
      {
        title: '您的权利',
        paragraphs: ['您（或未成年人的监护人）有权：'],
        items: [
          '查阅、复制、更正、补充您的个人信息。',
          '删除个人信息，或撤回此前给予的同意（撤回同意不影响撤回前基于同意已进行的处理）。',
          '注销账号，注销后我们将依法删除或匿名化处理您的个人信息。',
          '要求我们对处理规则进行解释说明。',
        ],
      },
      {
        title: '未成年人保护',
        paragraphs: [
          '本平台面向 6-18 岁青少年。我们严格遵循《儿童个人信息网络保护规定》：处理不满十四周岁儿童个人信息前，将征得监护人的明确同意；仅收集实现服务所必需的信息；并提供监护人查阅、更正、删除及撤回同意的渠道。详见《儿童个人信息保护规则》。',
        ],
      },
      {
        title: '政策更新',
        paragraphs: ['因业务调整或法律变化，我们可能更新本政策。更新后将在本页面公布并注明日期；涉及重大变更的，我们将以站内通知或短信方式再次征求您的同意。'],
      },
    ],
  },
  agreement: {
    title: '用户协议',
    short: '用户协议',
    intro: '欢迎使用码高少儿编程教育平台。本协议由您（未成年人用户须由监护人共同阅读并同意）与平台运营方共同订立，请仔细阅读并遵守。',
    sections: [
      {
        title: '服务内容',
        paragraphs: ['平台提供编程课程、题库练习、在线考试与赛事、创作社区、积分成长等服务。部分服务为付费服务，需完成支付后方可使用。平台的运营主体、联系方式见页面底部信息。'],
      },
      {
        title: '账号注册与使用',
        paragraphs: [
          '您应使用本人（或未成年人监护人授权的）真实信息注册账号，并对账号下的行为负责。',
          '未成年人注册及使用本平台须取得监护人同意；监护人应履行监护职责，关注未成年人的使用与消费行为。',
          '请妥善保管账号密码，发现账号被盗用应及时通知我们。我们有权对共享账号、恶意注册等行为进行限制。',
        ],
      },
      {
        title: '付费服务与退款',
        paragraphs: [
          '课程、会员及其他付费商品的价格以支付页面实时展示为准，支付成功后即开通相应权益。',
          '虚拟商品一经开通原则上不支持无理由退款；因平台原因导致服务无法提供的，我们将按实际未使用部分办理退款。具体规则以下单页面说明为准。',
          '未成年人进行大额消费前，应由监护人确认；监护人发现未经同意的消费，可联系我们依法处理。',
        ],
      },
      {
        title: '用户行为规范',
        paragraphs: ['您承诺不实施以下行为：'],
        items: [
          '发布违法、侵权、虚假、低俗或其他不良信息。',
          '抄袭、剽窃他人作品，或利用平台从事作弊、刷分、攻击系统等行为。',
          '逆向工程、绕过判题沙箱或安全机制，或干扰平台正常运行。',
          '冒用他人身份、侵犯他人隐私或合法权益。',
        ],
      },
      {
        title: '知识产权',
        paragraphs: ['平台课程内容、题目、软件及相关资源的知识产权归我们或权利人所有。您创作的作品著作权归您所有，但授予平台为展示、评测目的在必要范围内使用的权利。'],
      },
      {
        title: '免责与责任限制',
        paragraphs: [
          '因不可抗力、网络故障、第三方服务中断等非我方原因造成的服务中断，我们将尽力恢复但不承担由此产生的间接损失。',
          '浏览器内运行结果仅供参考，正式成绩以平台提交判题结果为准。',
        ],
      },
      {
        title: '协议变更与法律适用',
        paragraphs: ['我们可因业务调整更新本协议，更新后在本页面公布。本协议的订立、履行与争议解决适用中华人民共和国法律。'],
      },
    ],
  },
  children: {
    title: '儿童个人信息保护规则',
    short: '儿童个人信息保护规则',
    intro: '本规则依据《中华人民共和国个人信息保护法》《儿童个人信息网络保护规定》制定，专门说明我们如何保护不满十四周岁儿童（以下简称"儿童"）的个人信息。监护人务必仔细阅读。',
    sections: [
      {
        title: '适用范围',
        paragraphs: ['本规则适用于本平台收集、存储、使用、转移、披露儿童个人信息的全部活动。若您是儿童的监护人，请陪伴儿童阅读本规则，并决定是否同意我们处理其个人信息。'],
      },
      {
        title: '监护人同意',
        paragraphs: [
          '处理儿童个人信息前，我们将通过注册页或专门流程取得监护人的明确同意。监护人同意后，我们方才开始收集相关信息。',
          '监护人可随时联系我们撤回同意；撤回后我们将停止基于该同意的处理，并依法删除或匿名化相关信息。撤回同意可能导致儿童无法继续使用部分需信息支持的功能。',
        ],
      },
      {
        title: '收集与使用的原则',
        paragraphs: ['我们遵循最小必要、公平正当、明确告知的原则处理儿童个人信息：'],
        items: [
          '仅收集提供编程学习、赛事、支付等服务所必需的信息。',
          '不将儿童个人信息用于与其无关的营销目的。',
          '不对儿童进行用户画像用于商业营销。',
          '不以儿童个人信息向第三方提供任何形式的好处。',
        ],
      },
      {
        title: '委托处理与共享',
        paragraphs: [
          '我们委托第三方处理儿童个人信息时，将进行安全评估并与受托方签署保密与保护协议，约定处理目的、期限与方式，并对其处理活动进行监督。未经监护人单独同意，我们不向第三方公开披露儿童个人信息。',
        ],
      },
      {
        title: '安全保护措施',
        paragraphs: ['我们对儿童个人信息采取加密存储、访问控制、最小授权、安全审计、专人管理等措施，并定期开展安全评估与人员培训，防止信息泄露、篡改或丢失。'],
      },
      {
        title: '监护人的权利与行使方式',
        paragraphs: [
          '监护人有权查阅、复制、更正、删除儿童的个人信息，有权撤回同意、拒绝我们继续处理，有权要求注销儿童账号。',
          '监护人可通过页面底部公布的客服电话或邮箱向我们提出请求。我们将在核验监护人身份后 15 个工作日内予以响应。',
          '发现儿童个人信息可能被泄露、篡改或丢失的，请立即联系我们，我们将启动应急预案。',
        ],
      },
    ],
  },
}

const current = computed(() => docs[type.value])
const tabs = [
  { type: 'agreement', label: '用户协议' },
  { type: 'privacy', label: '隐私政策' },
  { type: 'children', label: '儿童信息保护' },
]
</script>

<style scoped lang="scss">
.legal-page {
  min-height: 70vh;
  background: #f7f8fa;
  padding: 40px 16px 80px;
}
.legal-container {
  max-width: 900px;
  margin: 0 auto;
  background: #fff;
  border-radius: 12px;
  padding: 40px 48px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.04);
}
.legal-header {
  border-bottom: 1px solid #e5e6eb;
  padding-bottom: 20px;
  h1 { font-size: 28px; margin: 0 0 8px; color: #1d2129; }
  .legal-updated { color: #86909c; font-size: 13px; margin: 0; }
}
.legal-tabs {
  display: flex;
  gap: 8px;
  margin: 24px 0;
  flex-wrap: wrap;
}
.legal-tab {
  padding: 8px 18px;
  border-radius: 20px;
  background: #f2f3f5;
  color: #4e5969;
  text-decoration: none;
  font-size: 14px;
  transition: all .2s;
  &.active, &:hover { background: #165dff; color: #fff; }
}
.legal-body {
  color: #4e5969;
  font-size: 15px;
  line-height: 1.9;
}
.legal-intro { color: #1d2129; font-weight: 500; }
.legal-section {
  margin: 28px 0;
  h2 { font-size: 18px; color: #1d2129; margin: 0 0 10px; }
  p { margin: 8px 0; }
  ul { margin: 8px 0; padding-left: 24px; li { margin: 4px 0; } }
}
.legal-contact {
  margin-top: 36px;
  padding: 20px;
  background: #f7f8fa;
  border-radius: 8px;
  p { margin: 4px 0; }
}
@media (max-width: 768px) {
  .legal-container { padding: 24px 20px; }
}
</style>
