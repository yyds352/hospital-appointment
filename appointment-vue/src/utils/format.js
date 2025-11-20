/**
 * 格式化日期
 * @param {Date|string} date 日期对象或日期字符串
 * @returns {string} 格式化后的日期字符串 (YYYY-MM-DD)
 */
export function formatDate(date) {
    if (!date) return '';
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

/**
 * 格式化日期时间
 * @param {string|Date} date 日期对象或日期字符串
 * @param {boolean} showTime 是否显示时间
 * @returns {string} 格式化后的日期时间字符串
 */
export function formatDateTime(date, showTime = true) {
  if (!date) return '-';
  
  const dateObj = date instanceof Date ? date : new Date(date);
  
  // 检查日期是否有效
  if (isNaN(dateObj.getTime())) {
    return '-';
  }
  
  const year = dateObj.getFullYear();
  const month = String(dateObj.getMonth() + 1).padStart(2, '0');
  const day = String(dateObj.getDate()).padStart(2, '0');
  
  if (!showTime) {
    return `${year}-${month}-${day}`;
  }
  
  const hours = String(dateObj.getHours()).padStart(2, '0');
  const minutes = String(dateObj.getMinutes()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hours}:${minutes}`;
}

/**
 * 格式化时间
 * @param {Date|string} time 时间对象或字符串
 * @returns {string} 格式化后的时间字符串 (HH:mm)
 */
export function formatTime(time) {
    if (!time) return '';
    const date = new Date(time);
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${hours}:${minutes}`;
}

// 格式化日期为 YYYY-MM-DD
export function formatDateToYYYYMMDD(date) {
    if (!date) return '';
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

/**
 * 格式化数字，添加千位分隔符
 * @param {number} num 需要格式化的数字
 * @returns {string} 格式化后的数字字符串
 */
export function formatNumber(num) {
  if (num === undefined || num === null) return '-';
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

/**
 * 截断文本
 * @param {string} text 需要截断的文本
 * @param {number} length 最大长度
 * @returns {string} 截断后的文本
 */
export function truncateText(text, length = 100) {
  if (!text) return '';
  if (text.length <= length) return text;
  return text.substring(0, length) + '...';
}

/**
 * 格式化文件大小
 * @param {number} bytes 文件大小（字节）
 * @returns {string} 格式化后的文件大小
 */
export function formatFileSize(bytes) {
  if (bytes === 0) return '0 Bytes';
  
  const units = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(1024));
  
  return parseFloat((bytes / Math.pow(1024, i)).toFixed(2)) + ' ' + units[i];
}

/**
 * 获取友好的时间显示
 * @param {string|Date} date 日期对象或日期字符串
 * @returns {string} 友好的时间显示
 */
export function getFriendlyTime(date) {
  if (!date) return '-';
  
  const dateObj = date instanceof Date ? date : new Date(date);
  const now = new Date();
  const diff = Math.floor((now - dateObj) / 1000); // 差异（秒）
  
  if (diff < 60) {
    return '刚刚';
  } else if (diff < 3600) {
    return Math.floor(diff / 60) + '分钟前';
  } else if (diff < 86400) {
    return Math.floor(diff / 3600) + '小时前';
  } else if (diff < 2592000) {
    return Math.floor(diff / 86400) + '天前';
  } else {
    return formatDateTime(dateObj, false);
  }
} 