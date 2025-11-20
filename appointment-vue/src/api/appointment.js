import request from '@/utils/request'

// 症状分析相关API
export function analyzeSymptoms(data) {
  return request({
    url: '/api/symptom-analysis/analyze',
    method: 'post',
    data
  })
}

export function extractKeywords(symptoms) {
  return request({
    url: '/api/symptom-analysis/extract-keywords',
    method: 'post',
    data: { symptoms }
  })
}

export function calculateMatchScore(data) {
  return request({
    url: '/api/symptom-analysis/calculate-match-score',
    method: 'post',
    data
  })
}

// 一键预约相关API
export function createOneStepAppointment(data) {
  return request({
    url: '/api/one-step/appointment',
    method: 'post',
    data
  })
}

export function getTimeSlotAvailability(params) {
  return request({
    url: '/api/one-step/time-slots/availability',
    method: 'get',
    params
  })
}

export function getPatientInfo() {
  return request({
    url: '/api/one-step/patient-info',
    method: 'get'
  })
}

export function validateOneStepAppointment() {
  return request({
    url: '/api/one-step/validation',
    method: 'get'
  })
}

/**
 * 查询可预约时间
 * @param {object} params - 查询参数
 * @returns {Promise}
 */
export function getAvailableSlots(params) {
  return request({
    url: '/appointments/available-slots',
    method: 'get',
    params
  })
}

/**
 * 创建预约
 * @param {object} data - 预约信息
 * @returns {Promise}
 */
export function createAppointment(data) {
  return request({
    url: '/appointments',
    method: 'post',
    data
  })
}

/**
 * 取消预约
 * @param {number} id - 预约ID
 * @returns {Promise}
 */
export function cancelAppointment(id) {
  return request({
    url: `/appointments/${id}/cancel`,
    method: 'post'
  })
}

/**
 * 获取患者预约列表
 * @param {object} params - 查询参数
 * @returns {Promise}
 */
export function getPatientAppointments(params) {
  return request({
    url: '/appointments/patient',
    method: 'get',
    params
  })
}

/**
 * 获取医生预约列表
 * @param {number} doctorId - 医生ID
 * @param {object} params - 查询参数
 * @returns {Promise}
 */
export function getDoctorAppointments(doctorId, params) {
  return request({
    url: `/appointments/doctor/${doctorId}`,
    method: 'get',
    params
  })
}

/**
 * 获取预约详情
 * @param {number} id - 预约ID
 * @returns {Promise}
 */
export function getAppointmentDetail(id) {
  return request({
    url: `/appointments/${id}`,
    method: 'get'
  })
}

/**
 * 更新预约状态
 * @param {number} id - 预约ID
 * @param {string} status - 预约状态
 * @returns {Promise}
 */
export function updateAppointmentStatus(id, status) {
  return request({
    url: `/appointments/${id}/status`,
    method: 'put',
    data: { status }
  })
}

/**
 * 获取科室列表
 * @returns {Promise}
 */
export function getDepartments() {
  return request({
    url: '/departments',
    method: 'get'
  })
}



/**
 * 获取所有预约列表（管理员用）
 * @param {object} params - 查询参数
 * @returns {Promise}
 */
export function getAppointmentList(params) {
  return request({
    url: '/appointments',
    method: 'get',
    params
  })
}

/**
 * 获取科室预约列表
 * @param {number} departmentId - 科室ID
 * @param {object} params - 查询参数
 * @returns {Promise}
 */
export function getDepartmentAppointments(departmentId, params) {
  return request({
    url: `/departments/${departmentId}/appointments`,
    method: 'get',
    params
  })
}

/**
 * 检查医生在指定时间是否可用
 * @param {number} doctorId - 医生ID
 * @param {string} appointmentTime - 预约时间
 * @returns {Promise}
 */
export function checkDoctorAvailability(doctorId, appointmentTime) {
  return request({
    url: `/doctor/${doctorId}/availability`,
    method: 'get',
    params: { appointmentTime }
  })
}

/**
 * 获取医生排班
 * @param {number} doctorId - 医生ID
 * @param {string} startTime - 开始时间
 * @param {string} endTime - 结束时间
 * @returns {Promise}
 */
export function getDoctorSchedule(doctorId, startTime, endTime) {
  return request({
    url: `/doctor/${doctorId}/schedule`,
    method: 'get',
    params: { startTime, endTime }
  })
}

/**
 * 获取医生某月排班
 * @param {number} doctorId - 医生ID
 * @param {number} year - 年份
 * @param {number} month - 月份(1-12)
 * @returns {Promise}
 */
export function getDoctorSchedulesByMonth(doctorId, year, month) {
  console.log(`查询医生${doctorId}的${year}年${month}月排班`);
  
  // 创建该月的第一天作为查询参数
  const date = new Date(year, month - 1, 1);
  
  // 格式化为YYYY-MM-DD格式，确保服务器能正确识别日期
  const dateStr = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  
  console.log(`构建查询参数 - 年份:${year}, 月份:${month}, 日期参数:${dateStr}`);
  
  return request({
    url: `/doctor/${doctorId}/schedules`,
    method: 'get',
    params: { 
      date: dateStr,
      type: 'month'
    }
  })
}





/**
 * 检查时间段可用性
 * @param {number} doctorId - 医生ID
 * @param {string} date - 日期
 * @param {string} period - 时间段（上午/下午/晚上）
 * @returns {Promise}
 */
export function checkTimeSlotAvailability(doctorId, date, period) {
  return request({
    url: `/doctor/${doctorId}/time-slots/availability`,
    method: 'get',
    params: { date, period }
  })
}

/**
 * 获取科室排班
 * @param {number} departmentId - 科室ID
 * @param {string} date - 日期
 * @returns {Promise}
 */
export function getDepartmentSchedule(departmentId, date) {
  return request({
    url: `/departments/${departmentId}/schedule`,
    method: 'get',
    params: { date }
  })
}

/**
 * 获取今天的预约数量
 * @returns {Promise}
 */
export function getTodayAppointmentsCount() {
  return request({
    url: '/appointments/today/count',
    method: 'get'
  })
}

/**
 * 获取排班列表
 * @param {object} params - 查询参数
 * @returns {Promise}
 */
export function getScheduleList(params) {
  return request({
    url: '/schedules',
    method: 'get',
    params
  })
}

/**
 * 获取医生排班列表
 * @param {number} doctorId - 医生ID
 * @param {string|Date} date - 日期
 * @returns {Promise}
 */
export function getDoctorSchedules(doctorId, date) {
  // 确保date是YYYY-MM-DD格式字符串
  let dateParam;
  if (date instanceof Date) {
    dateParam = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  } else if (typeof date === 'string') {
    // 如果是ISO字符串，提取YYYY-MM-DD部分
    dateParam = date.split('T')[0];
  } else {
    dateParam = date;
  }
  
  console.log('查询医生排班，日期参数:', dateParam);
  
  return request({
    url: `/doctor/${doctorId}/schedules`,
    method: 'get',
    params: { date: dateParam }
  })
}