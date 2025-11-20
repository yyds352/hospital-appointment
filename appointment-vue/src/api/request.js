// 请求拦截器 - 添加更多日志以便调试
service.interceptors.request.use(
  config => {
    console.log('请求详情 (URL):', config.url);
    console.log('请求详情 (Method):', config.method);
    console.log('请求详情 (Headers):', config.headers);
    
    // 如果有请求数据，也输出
    if (config.data) {
      console.log('请求数据:', config.data);
    }

    // 检查是否有token
    const token = localStorage.getItem('token');
    if (token) {
      // 确保token格式正确
      const formattedToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
      console.log('添加Authorization头:', formattedToken);
      config.headers.Authorization = formattedToken;
    }
    
    // 最终请求配置
    console.log('最终请求详情 (URL):', config.url);
    console.log('最终请求详情 (Method):', config.method);
    console.log('最终请求详情 (Headers):', config.headers);
    
    return config;
  },
  error => {
    console.error('请求配置错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器 - 添加更多调试日志
service.interceptors.response.use(
  response => {
    // 打印详细响应信息
    console.log('响应状态码:', response.status);
    console.log('响应数据类型:', typeof response.data);
    
    if (response.data === undefined) {
      console.warn('响应数据为undefined!');
    } else if (response.data === null) {
      console.warn('响应数据为null!');
    } else if (typeof response.data === 'object') {
      // 安全地检查对象内容
      if (Array.isArray(response.data)) {
        console.log('响应是数组，长度:', response.data.length);
      } else {
        const keys = Object.keys(response.data);
        console.log('响应对象属性:', keys);
        if (keys.includes('content') && Array.isArray(response.data.content)) {
          console.log('包含content数组属性，长度:', response.data.content.length);
        }
      }
    }
    
    // 完整记录
    console.log('Response:', response);
    
    // 旧版正常处理逻辑
    const res = response.data;
    if (res.code && res.code !== 200) {
      if (res.code === 401) {
        // 身份验证失败，重定向到登录
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        window.location.href = '/login';
      }
      return Promise.reject(new Error(res.message || 'Error'));
    }
    return response;
  },
  error => {
    // 增强错误日志
    console.error('请求失败:', error);
    if (error.response) {
      console.error('错误状态码:', error.response.status);
      console.error('错误响应:', error.response.data);
    } else if (error.request) {
      console.error('没有收到响应:', error.request);
    } else {
      console.error('请求配置错误:', error.message);
    }
    
    // 处理401未授权错误
    if (error.response && error.response.status === 401) {
      console.log('检测到401未授权错误，清除凭据并重定向到登录页');
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      window.location.href = '/login';
    }
    
    return Promise.reject(error);
  }
); 