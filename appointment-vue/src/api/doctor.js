import request from '@/utils/request'

// 获取医生列表
export function getDoctorList(params) {
  return request({
    url: '/doctor',
    method: 'get',
    params
  })
}

// 获取医生详情
export function getDoctorById(id) {
  return request({
    url: `/doctor/${id}`,
    method: 'get'
  })
}

// 创建医生
export function createDoctor(data) {
  // 克隆数据以避免修改原始引用
  const doctorData = { ...data };
  
  // 确保password和confirmPassword一致
  if (doctorData.password) {
    doctorData.confirmPassword = doctorData.password;
  } else if (doctorData.confirmPassword) {
    doctorData.password = doctorData.confirmPassword;
  }
  
  // 如果没有密码但有userId，提供默认密码
  if (doctorData.userId && !doctorData.password) {
    doctorData.password = '123456';
    doctorData.confirmPassword = '123456';
    console.log('提供默认密码以满足API要求');
  }
  
  // 确保username字段存在，尤其是当提供userId时
  if (doctorData.userId && !doctorData.username) {
    console.error('创建医生记录需要username字段，即使提供了userId');
    throw new Error('创建医生记录需要提供用户名');
  }
  
  console.log('发送到后端的医生数据:', doctorData);
  
  return request({
    url: '/doctor',
    method: 'post',
    data: doctorData
  });
}

// 更新医生
export function updateDoctor(id, data) {
  // 创建一个新对象，避免修改原始数据
  const updateData = { ...data }
  
  // 如果更新密码，确保confirmPassword与password相同
  if (updateData.password) {
    updateData.confirmPassword = updateData.password
  }
  
  console.log(`更新医生ID ${id} 的数据:`, updateData)
  
  return request({
    url: `/doctor/${id}`,
    method: 'put',
    data: updateData
  })
}

// 删除医生
export function deleteDoctor(id) {
  return request({
    url: `/doctor/${id}`,
    method: 'delete'
  })
}

// 获取科室的医生列表
export function getDoctorsByDepartment(departmentId) {
  return request({
    url: `/doctor/department/${departmentId}`,
    method: 'get'
  })
}

// 导出为兼容性函数名
export const listDoctors = getDoctorList 

// 根据用户ID获取医生信息
export function getDoctorByUserId(userId) {
  return request({
    url: `/doctor/user/${userId}`,
    method: 'get'
  })
}

// 获取医生列表（别名）
export function getDoctors(params) {
  return request({
    url: '/doctor',
    method: 'get',
    params
  })
}

// 获取医生详情（别名）
export function getDoctorDetail(id) {
  return request({
    url: `/doctor/${id}`,
    method: 'get'
  })
}

// 创建医生并关联到现有用户
export function createDoctorWithExistingUser(data) {
  // 克隆数据避免修改原始引用
  const postData = JSON.parse(JSON.stringify(data));
  
  // 删除可能的多余字段
  delete postData.id;
  delete postData.department;
  delete postData.password; // 不需要密码，使用现有用户
  delete postData.confirmPassword;
  delete postData.rePassword;
  delete postData.passwordConfirm;
  
  // 确保用户信息存在 - username是必须的
  if (!postData.username) {
    throw new Error('关联医生记录需要用户名');
  }
  
  // 确保名字字段存在
  if (!postData.name && postData.username) {
    postData.name = postData.username;
  }
  
  console.log('关联现有用户创建医生数据:', postData);
  
  return request({
    url: '/doctor/link-user',
    method: 'post',
    data: postData
  });
}

// 根据用户名获取用户信息
export function getUserByUsername(username) {
  return request({
    url: `/doctor/user/${username}`,
    method: 'get'
  })
}