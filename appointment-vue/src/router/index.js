import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/Layout.vue'
import OneStepAppointment from '@/views/appointment/OneStepAppointment.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    redirect: '/login',
    meta: { requiresAuth: false }
  },
  {
    path: '/forum',
    redirect: '/home/forum',
    meta: { requiresAuth: false }
  },
  {
    path: '/profile',
    redirect: '/home/profile',
    meta: { requiresAuth: true }
  },
  {
    path: '/department/list',
    redirect: '/home/department/list',
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/doctors/list',
    redirect: '/home/doctors/list',
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/schedule/management',
    redirect: '/home/schedule/management',
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/system/users',
    redirect: '/home/system/users',
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/home',
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      // 学生和教师路由（包括管理员）
      {
        path: 'appointment',
        name: 'Appointment',
        meta: { title: '预约管理', roles: ['STUDENT', 'TEACHER', 'ADMIN'] },
        children: [
          {
            path: 'quick',
            name: 'QuickAppointment',
            component: () => import('@/views/appointment/QuickAppointment.vue'),
            meta: { title: '快速预约', roles: ['STUDENT', 'TEACHER', 'ADMIN'] }
          },
          {
            path: 'one-step',
            name: 'OneStepAppointment',
            component: () => import('@/views/appointment/OneStepAppointment.vue'),
            meta: { title: '一键预约', roles: ['STUDENT', 'TEACHER', 'ADMIN'] }
          },
          {
            path: 'list',
            name: 'AppointmentList',
            component: () => import('@/views/appointment/AppointmentList.vue'),
            meta: { title: '预约挂号', roles: ['STUDENT', 'TEACHER', 'ADMIN'] }
          },
          {
            path: 'my',
            name: 'MyAppointment',
            component: () => import('@/views/appointment/MyAppointment.vue'),
            meta: { title: '我的预约', roles: ['STUDENT', 'TEACHER', 'ADMIN'] }
          }
        ]
      },
      // 医生路由
      {
        path: 'doctor',
        name: 'Doctor',
        meta: { title: '医生管理', roles: ['DOCTOR'] },
        children: [
          {
            path: 'appointments',
            name: 'DoctorAppointments',
            component: () => import('@/views/appointment/DoctorAppointmentList.vue'),
            meta: { title: '预约管理', roles: ['DOCTOR'] }
          },
          {
            path: 'schedule',
            name: 'DoctorSchedule',
            component: () => import('@/views/schedule/DoctorSchedule.vue'),
            meta: { title: '我的排班', roles: ['DOCTOR'] }
          },
          {
            path: 'list',
            name: 'DoctorList',
            component: () => import('@/views/doctor/DoctorList.vue'),
            meta: { title: '医生列表', roles: ['ADMIN'] }
          }
        ]
      },
      // 管理员路由
      {
        path: 'department',
        name: 'Department',
        meta: { title: '科室管理', roles: ['ADMIN'] },
        children: [
          {
            path: 'list',
            name: 'DepartmentList',
            component: () => import('@/views/department/DepartmentList.vue'),
            meta: { title: '科室列表', roles: ['ADMIN'] }
          }
        ]
      },
      {
        path: 'doctors',
        name: 'Doctors',
        meta: { title: '医生管理', roles: ['ADMIN'] },
        children: [
          {
            path: 'list',
            name: 'DoctorsList',
            component: () => import('@/views/doctor/DoctorList.vue'),
            meta: { title: '医生列表', roles: ['ADMIN'] }
          }
        ]
      },
      {
        path: 'schedule',
        name: 'Schedule',
        meta: { title: '排班管理', roles: ['ADMIN'] },
        children: [
          {
            path: 'management',
            name: 'ScheduleManagement',
            component: () => import('@/views/schedule/ScheduleManagement.vue'),
            meta: { title: '排班管理', roles: ['ADMIN'] }
          }
        ]
      },
      {
        path: 'system',
        name: 'System',
        meta: { title: '系统管理', roles: ['ADMIN'] },
        children: [
          {
            path: 'users',
            name: 'UserManagement',
            component: () => import('@/views/system/UserManagement.vue'),
            meta: { title: '用户管理', roles: ['ADMIN'] }
          },
          {
            path: 'user-add',
            name: 'UserAdd',
            component: () => import('@/views/system/UserAdd.vue'),
            meta: { title: '添加用户', roles: ['ADMIN'] }
          },
          {
            path: 'user-edit/:id',
            name: 'UserEdit',
            component: () => import('@/views/system/UserEdit.vue'),
            meta: { title: '编辑用户', roles: ['ADMIN'] }
          }
        ]
      },

      // 论坛路由
      {
        path: 'forum',
        name: 'Forum',
        meta: {
          title: '健康论坛',
          icon: 'ChatDotRound',
          roles: ['ADMIN', 'STUDENT', 'TEACHER', 'DOCTOR'],
          requiresAuth: false, // 允许未登录用户查看论坛
          alwaysShow: true
        },
        children: [
          {
            path: '',
            name: 'ForumList',
            component: () => import('@/views/forum/ForumList.vue'),
            meta: { title: '浏览论坛' }
          },
          {
            path: 'postlist',
            name: 'PostList',
            component: () => import('@/views/forum/PostList.vue'),
            meta: { title: '论坛列表' }
          },
          {
            path: 'post/:id',
            name: 'PostDetail',
            component: () => import('@/views/forum/ForumPostDetail.vue'),
            meta: { 
              title: '帖子详情',
              activeMenu: '/forum',
              hidden: true
            }
          },
          {
            path: 'my-posts',
            name: 'MyPosts',
            component: () => import('@/views/forum/MyPosts.vue'),
            meta: { title: '我的帖子', roles: ['ADMIN', 'STUDENT', 'TEACHER', 'DOCTOR'] }
          },
          {
            path: 'favorites',
            name: 'UserFavorites',
            component: () => import('@/views/forum/UserFavorites.vue'),
            meta: { title: '我的收藏' }
          },
          {
            path: 'category',
            name: 'PostCategory',
            component: () => import('@/views/forum/PostCategory.vue'),
            meta: { 
              title: '分类管理',
              roles: ['ADMIN']
            }
          },
          {
            path: 'management',
            name: 'ForumManagement',
            component: () => import('@/views/forum/ContentManagement.vue'),
            meta: { 
              title: '论坛管理',
              roles: ['ADMIN']
            }
          }
        ]
      },

      // 个人中心
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', roles: ['ADMIN', 'STUDENT', 'TEACHER', 'DOCTOR'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router