// 获取预约数据
const fetchAppointments = async () => {
  loading.value = true;
  try {
    const response = await getPatientAppointments();
    
    if (response && Array.isArray(response)) {
      console.log('获取预约信息成功', response);
      appointments.value = response;
    } else {
      console.error('获取预约信息失败: 无效的响应格式', response);
      ElMessage.error('获取预约信息失败，请稍后重试');
      appointments.value = [];
    }
  } catch (error) {
    console.error('获取预约信息失败', error);
    ElMessage.error('获取预约信息失败，请稍后重试');
    appointments.value = [];
  } finally {
    loading.value = false;
  }
}; 