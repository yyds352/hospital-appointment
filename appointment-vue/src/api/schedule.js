import request from '@/utils/request'

// 获取医生排班列表
export function getDoctorSchedules(doctorId, params) {
  return request({
    url: `/schedule/doctor/${doctorId}/schedules`,
    method: 'get',
    params
  })
}

// 格式化日期为 YYYY-MM-DD 格式
function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 获取科室排班列表
export function getDepartmentSchedules(departmentId, params) {
  return request({
    url: `/schedule/department/${departmentId}`,
    method: 'get',
    params
  })
}

// 获取医生未来排班
export function getFutureSchedulesByDoctor(doctorId) {
  return request({
    url: `/schedule/doctor/${doctorId}/future`,
    method: 'get'
  })
}

// 获取科室未来排班
export function getFutureSchedulesByDepartment(departmentId) {
  return request({
    url: `/schedule/department/${departmentId}/future`,
    method: 'get'
  })
}

// 获取排班列表
export function getScheduleList(params) {
  return request({
    url: '/schedule',
    method: 'get',
    params
  })
}

// 获取最近的排班数据
export function getRecentSchedules(limit = 10) {
  return request({
    url: '/schedule/recent',
    method: 'get',
    params: { limit }
  })
}

// 创建排班
export function createSchedule(data) {
  return request({
    url: '/schedule',
    method: 'post',
    data
  })
}

// 更新排班
export function updateSchedule(id, data) {
  return request({
    url: `/schedule/${id}`,
    method: 'put',
    data
  })
}

// 删除排班
export function deleteSchedule(id) {
  return request({
    url: `/schedule/${id}`,
    method: 'delete'
  })
}

// 获取排班详情
export function getScheduleById(id) {
  return request({
    url: `/schedule/${id}`,
    method: 'get'
  })
} 