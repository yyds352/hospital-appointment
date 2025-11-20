import request from '@/utils/request'

// 获取科室列表
export function getDepartmentList(params) {
  return request({
    url: '/departments',
    method: 'get',
    params
  })
}

// 获取科室详情
export function getDepartmentById(id) {
  return request({
    url: `/departments/${id}`,
    method: 'get'
  })
}

// 创建科室
export function createDepartment(data) {
  return request({
    url: '/departments',
    method: 'post',
    data
  })
}

// 更新科室
export function updateDepartment(id, data) {
  return request({
    url: `/departments/${id}`,
    method: 'put',
    data
  })
}

// 删除科室
export function deleteDepartment(id) {
  return request({
    url: `/departments/${id}`,
    method: 'delete'
  })
}

// 导出为兼容性函数名
export const listDepartments = getDepartmentList 