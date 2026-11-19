/**
 * 完整路由配置
 * 包含所有页面路由、meta 字段：title/roles/keepAlive
 */
import type { RouteRecordRaw } from 'vue-router'
import Layout from '@/layouts/DefaultLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import ExamLayout from '@/layouts/ExamLayout.vue'

const routes: RouteRecordRaw[] = [
  // ====== 登录/注册 ======
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginPage.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/RegisterPage.vue'),
    meta: { title: '注册' },
  },

  // ====== 前台页面（学生端） ======
  {
    path: '/',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/HomePage.vue'),
        meta: { title: '首页', keepAlive: true },
      },
      // 用户中心
      {
        path: 'user/profile',
        name: 'Profile',
        component: () => import('@/views/user/ProfilePage.vue'),
        meta: { title: '个人中心', keepAlive: true },
      },
      {
        path: 'user/center',
        name: 'UserCenter',
        component: () => import('@/views/user/UserCenterPage.vue'),
        meta: { title: '个人中心', keepAlive: true },
      },
      {
        path: 'user/settings',
        name: 'Settings',
        component: () => import('@/views/user/SettingsPage.vue'),
        meta: { title: '设置' },
      },
      // 课程
      {
        path: 'course',
        name: 'CourseList',
        component: () => import('@/views/course/CourseListPage.vue'),
        meta: { title: '课程商城', keepAlive: true },
      },
      {
        path: 'course/:id',
        name: 'CourseDetail',
        component: () => import('@/views/course/CourseDetailPage.vue'),
        meta: { title: '课程详情' },
      },
      {
        path: 'learn/:courseId',
        name: 'CourseLearn',
        component: () => import('@/views/course/CourseLearnPage.vue'),
        meta: { title: '课程学习' },
      },
      {
        path: 'community',
        name: 'Community',
        component: () => import('@/views/community/CommunityPage.vue'),
        meta: { title: '创作社区' },
      },
      // 独立创作编辑器（恢复被删除的 /practice/editor、/practice/scratch/create）
      {
        path: 'editor',
        name: 'Editor',
        component: () => import('@/views/editor/EditorPage.vue'),
        meta: { title: '创作编辑器' },
      },
      // 考试
      {
        path: 'exam',
        name: 'ExamList',
        component: () => import('@/views/exam/ExamListPage.vue'),
        meta: { title: '赛事中心', keepAlive: true },
      },
      {
        path: 'question-bank',
        name: 'QuestionBank',
        component: () => import('@/views/exam/QuestionBankPage.vue'),
        meta: { title: '题库中心', keepAlive: true },
      },
      {
        path: 'wrong-book',
        name: 'WrongBook',
        component: () => import('@/views/exam/WrongBookPage.vue'),
        meta: { title: '错题本', keepAlive: true },
      },
      {
        path: 'exam/:id',
        name: 'ExamDetail',
        component: () => import('@/views/exam/ExamDetailPage.vue'),
        meta: { title: '赛事详情' },
      },
      {
        path: 'exam/register/:examId',
        name: 'ExamRegister',
        component: () => import('@/views/exam/ExamRegisterPage.vue'),
        meta: { title: '赛事报名' },
      },
      {
        path: 'exam/result-query',
        name: 'ExamResultQuery',
        component: () => import('@/views/exam/ExamResultQueryPage.vue'),
        meta: { title: '成绩查询' },
      },
      {
        path: 'exam/result/:examId',
        name: 'ExamResult',
        component: () => import('@/views/exam/ExamResultPage.vue'),
        meta: { title: '考试成绩' },
      },
      // 成长
      {
        path: 'growth',
        name: 'MyGrowth',
        component: () => import('@/views/growth/MyGrowthPage.vue'),
        meta: { title: '我的成长' },
      },
      {
        path: 'growth/point-shop',
        name: 'PointShop',
        component: () => import('@/views/growth/PointShopPage.vue'),
        meta: { title: '积分商城' },
      },
      {
        path: 'growth/leaderboard',
        name: 'Leaderboard',
        component: () => import('@/views/growth/LeaderboardPage.vue'),
        meta: { title: '排行榜' },
      },
      // 营销
      {
        path: 'membership',
        name: 'Membership',
        component: () => import('@/views/marketing/MembershipPage.vue'),
        meta: { title: '会员中心' },
      },
      {
        path: 'coupon',
        name: 'Coupon',
        component: () => import('@/views/marketing/CouponPage.vue'),
        meta: { title: '优惠券' },
      },
      // 支付
      {
        path: 'order',
        name: 'OrderList',
        component: () => import('@/views/payment/OrderListPage.vue'),
        meta: { title: '我的订单' },
      },
      {
        path: 'pay/:orderId',
        name: 'Pay',
        component: () => import('@/views/payment/PayPage.vue'),
        meta: { title: '支付' },
      },
      // 消息
      {
        path: 'message',
        name: 'Message',
        component: () => import('@/views/message/MessageListPage.vue'),
        meta: { title: '消息中心', keepAlive: true },
      },
      // 资讯
      {
        path: 'news',
        name: 'NewsList',
        component: () => import('@/views/news/NewsListPage.vue'),
        meta: { title: '最新资讯', keepAlive: true },
      },
      {
        path: 'news/:id',
        name: 'NewsDetail',
        component: () => import('@/views/news/NewsListPage.vue'),
        meta: { title: '资讯详情', keepAlive: true },
      },
      // 关于我们
      {
        path: 'about',
        name: 'About',
        component: () => import('@/views/about/AboutPage.vue'),
        meta: { title: '关于我们' },
      },
      // 法律合规页面（P0-19：隐私政策 / 用户协议 / 儿童个人信息保护规则）
      {
        path: 'legal/:type',
        name: 'Legal',
        component: () => import('@/views/legal/LegalPage.vue'),
        meta: { title: '法律与隐私' },
      },
    ],
  },

  // ====== 考试专用布局（全屏无干扰） ======
  {
    path: '/exam-taking/:examId',
    component: ExamLayout,
    children: [
      {
        path: '',
        name: 'ExamTaking',
        component: () => import('@/views/exam/ExamTakingPage.vue'),
        meta: { title: '在线考试' },
      },
    ],
  },

  // ====== 后台管理 ======
  {
    path: '/admin',
    component: AdminLayout,
    meta: { roles: ['admin', 'teacher'] },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/DashboardPage.vue'),
        meta: { title: '管理首页', roles: ['admin', 'teacher'] },
      },
      {
        path: 'student',
        name: 'StudentManage',
        component: () => import('@/views/admin/StudentManagePage.vue'),
        meta: { title: '学员管理', roles: ['admin'] },
      },
      {
        path: 'teacher',
        name: 'TeacherManage',
        component: () => import('@/views/admin/TeacherManagePage.vue'),
        meta: { title: '教师管理', roles: ['admin'] },
      },
      {
        path: 'regcode',
        name: 'RegCodeManage',
        component: () => import('@/views/admin/RegCodeManagePage.vue'),
        meta: { title: '注册码管理', roles: ['admin'] },
      },
      {
        path: 'course',
        name: 'CourseManage',
        component: () => import('@/views/admin/CourseManagePage.vue'),
        meta: { title: '课程管理', roles: ['admin', 'teacher'] },
      },
      {
        path: 'trial',
        name: 'TrialManage',
        component: () => import('@/views/admin/TrialManagePage.vue'),
        meta: { title: '试听预约', roles: ['admin', 'teacher'] },
      },
      {
        path: 'exam',
        name: 'ExamManage',
        component: () => import('@/views/admin/ExamManagePage.vue'),
        meta: { title: '赛事管理', roles: ['admin', 'teacher'] },
      },
      {
        path: 'banner',
        name: 'BannerManage',
        component: () => import('@/views/admin/BannerManagePage.vue'),
        meta: { title: 'Banner管理', roles: ['admin'] },
      },
      {
        path: 'registration',
        name: 'RegistrationManage',
        component: () => import('@/views/admin/RegistrationManagePage.vue'),
        meta: { title: '报名管理', roles: ['admin', 'teacher'] },
      },
      {
        path: 'question-bank',
        name: 'QuestionBankManage',
        component: () => import('@/views/admin/QuestionBankPage.vue'),
        meta: { title: '题库中心', roles: ['admin', 'teacher'] },
      },
      {
        path: 'paper',
        name: 'PaperManage',
        component: () => import('@/views/admin/PaperManagePage.vue'),
        meta: { title: '试卷中心', roles: ['admin', 'teacher'] },
      },
      {
        path: 'score',
        name: 'ScoreManage',
        component: () => import('@/views/admin/ScoreManagePage.vue'),
        meta: { title: '成绩管理', roles: ['admin', 'teacher'] },
      },
      {
        path: 'class',
        name: 'ClassManage',
        component: () => import('@/views/admin/ClassManagePage.vue'),
        meta: { title: '班级管理', roles: ['admin', 'teacher'] },
      },
      {
        path: 'marketing',
        name: 'MarketingManage',
        component: () => import('@/views/admin/MarketingManagePage.vue'),
        meta: { title: '营销管理', roles: ['admin'] },
      },
      {
        path: 'file',
        name: 'FileManage',
        component: () => import('@/views/admin/FileManagePage.vue'),
        meta: { title: '文件管理', roles: ['admin'] },
      },
      {
        path: 'settings',
        name: 'SystemSettings',
        component: () => import('@/views/admin/SystemSettingsPage.vue'),
        meta: { title: '系统设置', roles: ['admin'] },
      },
      {
        path: 'news',
        name: 'AdminNews',
        component: () => import('@/views/admin/NewsManagePage.vue'),
        meta: { title: '资讯管理', roles: ['admin'] },
      },
      {
        path: 'category',
        name: 'AdminCategory',
        component: () => import('@/views/admin/CategoryManagePage.vue'),
        meta: { title: '分类管理', roles: ['admin'] },
      },
      {
        path: 'partner',
        name: 'AdminPartner',
        component: () => import('@/views/admin/PartnerManagePage.vue'),
        meta: { title: '合作伙伴', roles: ['admin'] },
      },
      {
        path: 'homepage-config',
        name: 'AdminHomepageConfig',
        component: () => import('@/views/admin/HomepageConfigPage.vue'),
        meta: { title: '首页配置', roles: ['admin'] },
      },
      {
        path: 'about-config',
        name: 'AdminAboutConfig',
        component: () => import('@/views/admin/AboutConfigPage.vue'),
        meta: { title: '关于我们配置', roles: ['admin'] },
      },
      {
        path: 'storage-config',
        name: 'AdminStorageConfig',
        component: () => import('@/views/admin/StorageConfigPage.vue'),
        meta: { title: '云存储配置', roles: ['admin'] },
      },
      {
        path: 'notification',
        name: 'AdminNotification',
        component: () => import('@/views/admin/NotificationManagePage.vue'),
        meta: { title: '通知管理', roles: ['admin'] },
      },
      {
        path: 'operation-log',
        name: 'AdminOperationLog',
        component: () => import('@/views/admin/OperationLogPage.vue'),
        meta: { title: '操作日志', roles: ['admin'] },
      },
      {
        path: 'data-backup',
        name: 'AdminDataBackup',
        component: () => import('@/views/admin/DataBackupPage.vue'),
        meta: { title: '数据备份', roles: ['admin'] },
      },
      {
        path: 'admin-account',
        name: 'AdminAccountManage',
        component: () => import('@/views/admin/AdminAccountManagePage.vue'),
        meta: { title: '管理员账号', roles: ['admin'] },
      },
      {
        path: 'system-monitor',
        name: 'AdminSystemMonitor',
        component: () => import('@/views/admin/SystemMonitorPage.vue'),
        meta: { title: '系统监控', roles: ['admin'] },
      },
    ],
  },

  // ====== 404 页面（兜底路由） ======
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFoundPage.vue'),
    meta: { title: '页面不存在' },
  },
]

export default routes
