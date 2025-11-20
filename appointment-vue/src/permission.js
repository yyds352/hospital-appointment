import router from './router'
import store from './store'

// 白名单路由
const whiteList = ['/login', '/register', '/auth-redirect', '/bind', '/register-result', '/forum']

// 需要管理员权限的路径
const adminPaths = ['/forum/management']

// 路由映射表（修复路径）
const routeMapping = {
  '/forum/list': '/forum',
  '/forum/category': '/home/forum/category',
  '/forum/management': '/home/forum/management',
  '/appointment/list': '/home/appointment/list',
  '/appointment/my': '/home/appointment/my',
  '/forum/my-posts': '/home/forum/my-posts',
  '/forum/favorites': '/home/forum/favorites',
  '/doctor/appointments': '/home/doctor/appointments',
  '/doctor/schedule': '/home/doctor/schedule'
}

/**
 * 检查用户是否有权限访问路由
 * @param {Object} route - 目标路由
 * @param {Array} roles - 用户角色数组
 * @returns {Boolean} - 是否有权限
 */
function hasPermission(route, roles) {
  // 确保roles是数组
  if (!Array.isArray(roles) || roles.length === 0) {
    return false
  }
  
  // 如果路由设置了roles要求，检查用户角色是否在允许的角色列表中
  if (route.meta && route.meta.roles) {
    return roles.some(role => route.meta.roles.includes(role))
  }
  
  // 如果没有设置roles要求，则允许访问
  return true
}

/**
 * 从store获取用户信息（如果还没有）
 */
async function getUserInfo() {
  try {
    // 如果store中没有用户信息，尝试获取
    if (!store.state.user.userInfo || store.state.user.roles.length === 0) {
      await store.dispatch('user/getInfo')
    }
    return {
      userInfo: store.state.user.userInfo,
      roles: store.state.user.roles
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    return null
  }
}

router.beforeEach(async (to, from, next) => {
  console.log('Permission check for path:', to.path)
  
  // 设置页面标题
  document.title = to.meta?.title ? `${to.meta.title} - 医院预约系统` : '医院预约系统'
  
  // 首先检查白名单，支持子路径匹配
  const isInWhiteList = whiteList.some(whitePath => {
    if (whitePath === '/forum') {
      // /forum 支持子路径匹配，但排除管理员路径和管理员专用功能
      const adminOnlyPaths = ['/forum/management', '/forum/category']
      return to.path.startsWith('/forum') && !adminOnlyPaths.includes(to.path)
    }
    return to.path === whitePath
  })
  
  // 如果在白名单中，检查是否需要重定向
  if (isInWhiteList) {
    console.log('Path is in whitelist, allow access:', to.path)
    // 检查是否是直接访问子路径，需要重定向到/home路径
    const directAccessPaths = ['/appointment/list', '/appointment/my', '/forum/my-posts', '/forum/category', '/forum/management', '/forum/favorites']
    if (directAccessPaths.includes(to.path) && !to.path.startsWith('/home/')) {
      console.log('Direct access to subpath detected, redirecting to /home equivalent')
      const targetPath = '/home' + to.path
      next(targetPath)
      return
    }
    next()
    return
  }
  
  // 获取用户登录状态
  const hasToken = localStorage.getItem('token')
  
  console.log('Token status:', !!hasToken)
  
  if (hasToken) {
    // 有token的情况
    if (to.path === '/login') {
      console.log('Already logged in, redirect to home')
      // 已登录且要跳转的页面是登录页，直接跳转到首页
      next({ path: '/home' })
      return
    }
    
    // 获取用户信息
    const userData = await getUserInfo()
    if (!userData) {
      console.log('Failed to get user info, redirect to login')
      // 获取用户信息失败，可能是token无效，清除token并跳转到登录页
      store.dispatch('user/fedLogout')
      next('/login')
      return
    }
    
    const { roles } = userData
    console.log('User roles:', roles)
    
    // 检查是否需要路径修复
    if (routeMapping[to.path]) {
      console.log('Path mapping found, redirect from', to.path, 'to', routeMapping[to.path])
      next(routeMapping[to.path])
      return
    }
    
    // 检查角色权限
    if (hasPermission(to, roles)) {
      console.log('User has permission to access:', to.path)
      next()
    } else {
      console.log('User does not have permission to access:', to.path)
      console.log('User roles:', roles)
      console.log('Route required roles:', to.meta?.roles)
      // 没有权限，跳转到403页面或首页
      next('/home')
    }
  } else {
    // 没有token的情况
    console.log('No token found, checking whitelist for path:', to.path)
    
    // 检查是否是管理员专用路径
    if (adminPaths.includes(to.path)) {
      console.log('Admin path requires login, redirect to login')
      next('/login')
      return
    }
    
    console.log('Path not in whitelist, redirect to login')
    // 其他没有访问权限的页面将被重定向到登录页面
    next('/login')
  }
})

router.afterEach((to) => {
  // 导航完成后的日志输出
  console.log('Navigation completed to:', to.path)
})