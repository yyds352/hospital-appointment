/**
 * 认证相关的工具函数
 */

const TokenKey = 'token';
const UserInfoKey = 'userInfo';

/**
 * 获取token
 * @returns {string} token
 */
export function getToken() {
  return localStorage.getItem(TokenKey);
}

/**
 * 设置token
 * @param {string} token 
 */
export function setToken(token) {
  return localStorage.setItem(TokenKey, token);
}

/**
 * 移除token
 */
export function removeToken() {
  return localStorage.removeItem(TokenKey);
}

/**
 * 获取用户信息
 * @returns {Object} 用户信息对象
 */
export function getUserInfo() {
  const userInfoStr = localStorage.getItem(UserInfoKey);
  if (userInfoStr) {
    try {
      return JSON.parse(userInfoStr);
    } catch (e) {
      console.error('解析用户信息失败:', e);
      return null;
    }
  }
  return null;
}

/**
 * 设置用户信息
 * @param {Object} userInfo 
 */
export function setUserInfo(userInfo) {
  if (userInfo) {
    return localStorage.setItem(UserInfoKey, JSON.stringify(userInfo));
  }
}

/**
 * 移除用户信息
 */
export function removeUserInfo() {
  return localStorage.removeItem(UserInfoKey);
}

/**
 * 清除所有认证信息
 */
export function clearAuth() {
  removeToken();
  removeUserInfo();
}

/**
 * 判断用户是否已登录
 * @returns {boolean}
 */
export function isLoggedIn() {
  return !!getToken() && !!getUserInfo();
}

/**
 * 获取用户角色
 * @returns {string|null} 用户角色
 */
export function getUserRole() {
  const userInfo = getUserInfo();
  return userInfo ? userInfo.role : null;
} 