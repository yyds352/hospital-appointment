import request from '@/utils/request'

// 角色ID映射
export const ROLE_MAP = {
  'STUDENT': 1,
  'TEACHER': 2,
  'DOCTOR': 3,
  'ADMIN': 4
}

/**
 * 用户登录
 * @param {object} data - 登录信息
 * @returns {Promise}
 */
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 用户登出
 * @returns {Promise}
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

/**
 * 获取当前用户信息
 * @returns {Promise}
 */
export function getUserInfo() {
  return request({
    url: '/auth/current-user',
    method: 'get'
  })
}

/**
 * 用户注册
 * @param {object} data - 注册信息
 * @returns {Promise}
 */
export function register(data) {
  console.log('Register form data:', data);
  // 确保数据包含roleId
  if (data.role && !data.roleId) {
    data.roleId = ROLE_MAP[data.role] || 1;
  }
  
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

/**
 * 修改密码
 * @param {object} data - 密码信息
 * @returns {Promise}
 */
export function updatePassword(data) {
  return request({
    url: '/users/password',
    method: 'put',
    data
  })
}

/**
 * 更新用户信息
 * @param {object} data - 用户信息
 * @returns {Promise}
 */
export function updateUserInfo(data) {
  return request({
    url: '/users/current',
    method: 'put',
    data
  })
}

/**
 * 获取用户列表
 * @param {object} params - 查询参数
 * @returns {Promise}
 */
export function getUserList(params) {
  return request({
    url: '/users',
    method: 'get',
    params
  })
}

/**
 * 获取指定用户信息
 * @param {number} id - 用户ID
 * @returns {Promise}
 */
export function getUserById(id) {
  return request({
    url: `/users/${id}`,
    method: 'get'
  })
}

/**
 * 删除用户
 * @param {number} id - 用户ID
 * @returns {Promise}
 */
export function deleteUser(id) {
  return request({
    url: `/users/${id}`,
    method: 'delete'
  })
}

/**
 * 重置用户密码
 * @param {number} id - 用户ID
 * @returns {Promise}
 */
export function resetUserPassword(id) {
  return request({
    url: `/users/${id}/reset-password`,
    method: 'post'
  })
}

/**
 * 更新用户状态
 * @param {number} id - 用户ID
 * @param {number} status - 状态 (0:禁用, 1:启用)
 * @returns {Promise}
 */
export function updateUserStatus(id, status) {
  return request({
    url: `/users/${id}/status`,
    method: 'put',
    data: { status }
  })
}

/**
 * 添加用户
 * @param {object} data - 用户信息
 * @returns {Promise}
 */
export function addUser(data) {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

/**
 * 更新用户
 * @param {number} id - 用户ID
 * @param {object} data - 用户信息
 * @returns {Promise}
 */
export function updateUser(id, data) {
  return request({
    url: `/users/${id}`,
    method: 'put',
    data
  })
}

// 检查用户名是否已存在
export function checkUsernameExists(username) {
  return request({
    url: `/users/check-username/${username}`,
    method: 'get'
  });
}

// 根据用户名获取用户信息
export function getUserByUsername(username) {
  return request({
    url: `/users/by-username/${username}`,
    method: 'get'
  });
}