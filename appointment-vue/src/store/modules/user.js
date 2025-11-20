import { login, logout, getUserInfo, register, updateUserInfo as apiUpdateUserInfo, updatePassword } from '@/api/user'
import { setToken, getToken, removeToken, setUserInfo, getUserInfo as getStoredUserInfo, removeUserInfo } from '@/utils/auth'
import router from '@/router'
import { ElMessage } from 'element-plus'

const state = {
  token: getToken(),
  userInfo: getStoredUserInfo(),
  roles: [],
  permissions: [],
  appointmentId: null
}

const mutations = {
  SET_TOKEN: (state, token) => {
    state.token = token
  },
  SET_USER_INFO: (state, userInfo) => {
    state.userInfo = userInfo
  },
  SET_ROLES: (state, roles) => {
    state.roles = roles
  },
  SET_PERMISSIONS: (state, permissions) => {
    state.permissions = permissions
  },
  SET_APPOINTMENT_ID(state, id) {
    state.appointmentId = id
  },
  CLEAR_USER_INFO(state) {
    state.userInfo = null
    state.token = ''
    state.roles = []
    state.permissions = []
    state.appointmentId = null
    removeToken()
    removeUserInfo()
  }
}

const actions = {
  // 用户登录
  login({ commit, dispatch }, userInfo) {
    const { username, password } = userInfo
    return new Promise((resolve, reject) => {
      login({ username: username.trim(), password: password })
        .then(response => {
          console.log('登录响应原始数据:', response);
          
          // 处理不同的响应数据结构
          let userData, token;
          
          // 检查标准响应格式 {code: 200, message: 'success', data: {...}}
          if (response && response.code === 200 && response.data) {
            console.log('检测到标准API响应格式');
            userData = response.data;
            token = userData.token; // token在data对象中
          }
          // 检查响应格式 - 最常见的情况：后端直接返回了包含用户信息和token的对象
          else if (response && response.token && !response.code) {
            // 直接返回了包含token和用户信息的对象
            console.log('检测到直接返回格式，token存在');
            userData = response;
            token = response.token;
          } 
          // 处理后端返回的UserDTO结构
          else if (response && response.id && response.username) {
            // 这是直接的UserDTO对象
            console.log('检测到UserDTO格式，用户ID:', response.id);
            userData = response;
            token = response.token;
          }
          // 处理嵌套结构 {userInfo: {...}, token: '...'}
          else if (response && response.userInfo && response.token) {
            console.log('检测到嵌套结构格式');
            userData = response.userInfo;
            token = response.token;
          }
          // 尝试从localStorage获取token
          else {
            console.log('未识别的响应格式，尝试直接处理');
            userData = response;
            token = getToken(); // 尝试从storage获取token
          }
          
          // 确保有token
          if (!token) {
            console.error('无法获取认证令牌, 响应数据:', response);
            throw new Error('登录失败: 无法获取认证令牌');
          }
          
          // 设置token
          commit('SET_TOKEN', token);
          setToken(token);
          
          // 确保有用户数据
          if (!userData) {
            console.error('无法获取用户信息, 响应数据:', response);
            throw new Error('登录失败: 无法获取用户信息');
          }
          
          console.log('处理后的用户数据:', userData);
          
          // 设置用户信息
          commit('SET_USER_INFO', userData);
          setUserInfo(userData);
          
          // 设置角色 - 确保获取正确的角色字段
          let role = null;
          if (typeof userData.role === 'string') {
            role = userData.role;
          } else if (userData.roleId || userData.role_id) {
            // 根据角色ID映射为角色名称
            const roleId = userData.roleId || userData.role_id;
            const roleMap = {
              1: 'STUDENT',
              2: 'TEACHER',
              3: 'DOCTOR',
              4: 'ADMIN'
            };
            role = roleMap[roleId] || 'USER';
          }
          
          console.log('提取的角色:', role);
          
          const roles = role ? [role] : [];
          commit('SET_ROLES', roles);
          
          // 生成权限路由
          dispatch('permission/generateRoutes', role, { root: true });
          dispatch('permission/fixRoutes', null, { root: true });
          
          // 登录后立即获取完整用户信息
          dispatch('getInfo').then(() => {
            console.log('登录后获取用户信息完成')
          }).catch(error => {
            console.error('登录后获取用户信息失败:', error)
          })
          
          router.push('/home');
          ElMessage.success('登录成功');
          resolve(response);
        })
        .catch(error => {
          console.error('登录失败:', error);
          commit('CLEAR_USER_INFO');
          ElMessage.error(error.response?.data?.message || error.message || '登录失败，请重试');
          reject(error);
        });
    });
  },

  // 获取用户信息
  getInfo({ commit, state, dispatch }) {
    return new Promise((resolve, reject) => {
      // 如果已有用户信息，直接返回
      if (state.userInfo && state.roles.length > 0) {
        console.log('已有用户信息，直接返回:', state.userInfo.username)
        resolve({ userInfo: state.userInfo, roles: state.roles })
        return
      }
      
      // 否则从服务器获取
      getUserInfo()
        .then(response => {
          console.log('getUserInfo响应数据:', response);
          const userInfo = response
          
          // 设置用户信息
          commit('SET_USER_INFO', userInfo)
          setUserInfo(userInfo)
          
          // 设置角色 - 确保获取正确的角色字段
          let role = null
          if (typeof userInfo.role === 'string') {
            role = userInfo.role.toUpperCase()
          } else if (userInfo.roleId || userInfo.role_id) {
            // 根据角色ID映射为角色名称
            const roleId = userInfo.roleId || userInfo.role_id
            const roleMap = {
              1: 'STUDENT',
              2: 'TEACHER',
              3: 'DOCTOR',
              4: 'ADMIN'
            }
            role = roleMap[roleId] || 'USER'
          }
          
          const roles = role ? [role] : []
          commit('SET_ROLES', roles)
          
          // 生成权限路由
          dispatch('permission/generateRoutes', userInfo.role, { root: true })
          dispatch('permission/fixRoutes', null, { root: true })
          
          console.log('从服务器获取用户信息成功:', userInfo.username)
          resolve({ userInfo, roles })
        })
        .catch(error => {
          // 如果获取失败，清空信息
          console.error('从服务器获取用户信息失败:', error)
          commit('CLEAR_USER_INFO')
          reject(error)
        })
    })
  },

  // 退出登录
  logout({ commit, state }) {
    return new Promise((resolve, reject) => {
      logout()
        .then(() => {
          // 清空状态
          commit('CLEAR_USER_INFO')
          
          // 跳转到登录页
          router.push('/login')
          resolve()
        })
        .catch(error => {
          // 即使退出登录API失败，也清空本地状态
          commit('CLEAR_USER_INFO')
          
          router.push('/login')
          reject(error)
        })
    })
  },

  // 前端登出
  fedLogout({ commit }) {
    return new Promise(resolve => {
      commit('CLEAR_USER_INFO')
      
      router.push('/login')
      resolve()
    })
  },

  // 注册
  async register({ commit }, userInfo) {
    try {
      console.log('Register request:', userInfo)
      const response = await register(userInfo)
      console.log('Register response:', response)
      
      // 处理不同的响应格式
      // 情况1: 包含code的标准API响应
      if (response && typeof response.code !== 'undefined') {
        if (response.code === 500) {
          throw new Error(response.message || '注册失败，请重试')
        }
        
        if (response.code === 200 || response.code === 0) {
          ElMessage.success('注册成功，请登录')
          router.push('/login')
          return response.data
        }
        
        // 其他code情况，使用返回的错误信息
        if (response.message) {
          throw new Error(response.message)
        }
      }
      // 情况2: 直接返回用户数据对象 (通常表示成功)
      else if (response && response.id && response.username) {
        ElMessage.success('注册成功，请登录')
        router.push('/login')
        return response
      }
      // 情况3: 嵌套在data中的用户数据
      else if (response && response.data && response.data.id) {
        ElMessage.success('注册成功，请登录')
        router.push('/login')
        return response.data
      }
      
      // 无法识别的响应格式
      throw new Error('注册失败，请重试')
    } catch (error) {
      console.error('Register action error:', error)
      // 优先使用后端返回的错误信息
      const errorMessage = error.response?.data?.message || error.message || '注册失败，请重试'
      ElMessage.error(errorMessage)
      throw error
    }
  },

  // 更新用户信息
  async updateUserInfo({ commit, state, dispatch }, userInfo) {
    try {
      if (!userInfo || !userInfo.id) {
        throw new Error('用户ID不能为空')
      }
      
      console.log('Vuex: 准备发送更新请求:', userInfo)
      
      // 调用更新API
      const response = await apiUpdateUserInfo(userInfo)
      console.log('Vuex: 更新响应:', response)
      
      if (response) {
        // 确保角色字段是大写的
        const processedUserInfo = {
          ...response,
          role: response.role?.toUpperCase()
        }
        console.log('Vuex: 处理后的用户信息:', processedUserInfo)
        commit('SET_USER_INFO', processedUserInfo)
        ElMessage.success('个人信息更新成功')
        return processedUserInfo
      }
      
      return null
    } catch (error) {
      console.error('Vuex: 更新用户信息失败:', error)
      
      // 处理特定错误码
      if (error.response) {
        if (error.response.status === 403) {
          ElMessage.error('您没有权限执行此操作')
        } else if (error.response.status === 405) {
          ElMessage.error('当前接口不支持此操作，请联系管理员')
        } else {
          ElMessage.error(error.response.data?.message || '更新个人信息失败')
        }
      } else {
        ElMessage.error(error.message || '更新个人信息失败')
      }
      
      throw error
    }
  },

  // 修改密码
  async updatePassword({ commit }, passwordData) {
    try {
      console.log('Vuex: 准备发送修改密码请求:', passwordData)
      const response = await updatePassword(passwordData)
      console.log('Vuex: 修改密码响应:', response)
      return response
    } catch (error) {
      console.error('Vuex: 修改密码失败:', error)
      throw error
    }
  },

  // 初始化用户状态
  initUserState({ commit, dispatch }) {
    try {
      const token = localStorage.getItem('token')
      const userInfoStr = localStorage.getItem('userInfo')
      
      if (!token || !userInfoStr) {
        return false
      }
      
      const userInfo = JSON.parse(userInfoStr)
      
      commit('SET_TOKEN', token)
      commit('SET_USER_INFO', userInfo)
      
      // 设置角色
      const roles = userInfo.role ? [userInfo.role] : []
      commit('SET_ROLES', roles)
      
      // 初始化权限路由
      dispatch('permission/generateRoutes', userInfo.role, { root: true })
      dispatch('permission/fixRoutes', null, { root: true })
      
      console.log('用户状态初始化成功:', { token: !!token, userInfo: userInfo.username, role: userInfo.role })
      return true
    } catch (error) {
      console.error('初始化用户状态失败:', error)
      commit('CLEAR_USER_INFO')
      return false
    }
  },

  // 获取当前用户信息（用于论坛等页面）
  getCurrentUser({ commit, state, dispatch }) {
    return new Promise((resolve, reject) => {
      // 如果已有用户信息，直接返回
      if (state.userInfo) {
        resolve(state.userInfo)
        return
      }
      
      // 否则调用getInfo获取用户信息
      dispatch('getInfo')
        .then(({ userInfo }) => {
          resolve(userInfo)
        })
        .catch(error => {
          console.error('获取当前用户信息失败:', error)
          reject(error)
        })
    })
  }
}

const getters = {
  userInfo: state => state.userInfo,
  token: state => state.token,
  appointmentId: state => state.appointmentId,
  isAuthenticated: state => {
    console.log('Checking auth status:', {
      hasUserInfo: !!state.userInfo,
      hasToken: !!state.token,
      token: state.token
    })
    return !!state.userInfo && !!state.token
  },
  roles: state => state.roles,
  permissions: state => state.permissions
}

const store = {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}

// 导出 store 实例
export default store

// 导出 useUserStore hook
export function useUserStore() {
  return {
    state: store.state,
    getters: store.getters,
    ...store.actions
  }
}