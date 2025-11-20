import axios from 'axios'
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api', // 使用Vite代理，避免跨域问题
  timeout: 10000,
  withCredentials: true
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    console.log('请求详情 (URL):', config.url);
    console.log('请求详情 (Method):', config.method);
    console.log('请求详情 (Headers):', config.headers);
    
    // 获取token
    const token = getToken();
    
    // 如果有token，则添加到请求头
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
      console.log('添加Authorization头:', `Bearer ${token}`);
    }
    
    // 设置Content-Type
    if (config.method === 'post' || config.method === 'put') {
      config.headers['Content-Type'] = 'application/json';
    }
    
    // 检查是否包含密码字段，并确保confirmPassword与password一致
    if (config.method === 'post' && config.data) {
      let data = config.data;
      // 如果数据是字符串（已经stringify的JSON），先解析
      if (typeof data === 'string') {
        try {
          data = JSON.parse(data);
        } catch (e) {
          console.error('解析请求数据失败:', e);
        }
      }
      
      // 检查密码字段
      if (data.password !== undefined) {
        // 确保confirmPassword与password一致
        if (data.confirmPassword === undefined) {
          data.confirmPassword = data.password;
          console.log('自动补充confirmPassword字段');
        } else if (data.password !== data.confirmPassword) {
          data.confirmPassword = data.password;
          console.log('自动纠正不匹配的confirmPassword字段');
        }
        
        // 记录密码字段信息用于调试
        console.log('密码字段检查 (Request):', {
          password: data.password, 
          confirmPassword: data.confirmPassword, 
          passwordLength: data.password ? data.password.length : 0, 
          confirmPasswordLength: data.confirmPassword ? data.confirmPassword.length : 0, 
          isEqual: data.password === data.confirmPassword
        });
        
        // 确保更新后的数据被使用
        if (typeof config.data === 'string') {
          config.data = JSON.stringify(data);
        } else {
          config.data = data;
        }
      }
      
      // 记录完整的请求数据
      console.log('请求数据:', JSON.stringify(config.data));
    }
    
    // 添加时间戳以防止缓存
    if (config.method === 'get') {
      config.params = {
        _t: new Date().getTime(),
        ...config.params
      }
    }
    
    // 详细记录请求数据
    if (config.data) {
      try {
        const reqData = JSON.parse(JSON.stringify(config.data));
        console.log('请求详情 (URL):', config.url);
        console.log('请求详情 (Method):', config.method);
        console.log('请求详情 (Headers):', config.headers);
        console.log('请求详情 (Data):', reqData);
        
        // 添加密码字段分析
        if (reqData.password || reqData.confirmPassword) {
          console.log('密码字段检查 (Request):', {
            password: reqData.password,
            confirmPassword: reqData.confirmPassword,
            passwordLength: reqData.password ? reqData.password.length : 0,
            confirmPasswordLength: reqData.confirmPassword ? reqData.confirmPassword.length : 0,
            isEqual: reqData.password === reqData.confirmPassword
          });
        }
      } catch (e) {
        console.error('请求数据解析失败:', e);
      }
    }
    
    return config
  },
  error => {
    console.error('请求配置错误:', error);
    return Promise.reject(error);
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    console.log('Response:', response)
    // 登录特殊处理 - 打印原始响应数据用于调试
    if (response.config.url.includes('/login')) {
      console.log('登录响应原始数据:', response.data)
    }
    
    const res = response.data
    
    // 如果是文件下载等二进制响应，直接返回
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return response;
    }
    
    // 处理自定义错误码
    if (res && typeof res === 'object' && res.code !== undefined) {
      // 如果不是成功的业务状态码
      if (res.code !== 200) {
        // 如果是登录相关错误（包括token无效或过期）
        if (res.code === 401 || res.code === 500 && res.message === '未登录') {
          // 清空本地token并跳转登录页
          localStorage.removeItem('token');
          localStorage.removeItem('userInfo');
          
          // 如果不是已经在登录页，才跳转
          if (router.currentRoute.value.path !== '/login') {
            ElMessage.error('身份验证已失效，请重新登录');
            router.push('/login');
          }
          
          return Promise.reject(new Error('未登录或身份已过期'));
        }
        
        // 其他业务错误
        console.log('\n 业务错误:', res);
        ElMessage.error(res.message || '请求失败');
        return Promise.reject(res);
      }
      
      // 登录接口和获取当前用户信息接口特殊处理 - 返回完整响应
      if (response.config.url.includes('/login') || response.config.url.includes('/current-user')) {
        console.log('特殊接口返回数据:', res);
        console.log('特殊接口data字段:', res.data);
        // 返回完整响应，让store中的逻辑处理数据结构
        return res;
      }
      
      // 其他接口正常返回data
      return res.data;
    }
    
    // 如果响应不是标准格式，返回整个响应
    return res;
  },
  error => {
    // 特殊处理：删除帖子的403错误（可能是降级策略的一部分）
    if (error.response && 
        error.response.status === 403 && 
        error.config && 
        error.config.method === 'delete' && 
        (error.config.url.includes('/admin/forum/posts/') || error.config.url.includes('/forum/posts/'))) {
      // 对于删除帖子的403错误，只在控制台输出简单信息，不显示错误消息
      console.log(`删除帖子接口返回403，可能是权限不足或降级策略: ${error.config.url}`);
      return Promise.reject(error);
    }

    // 特殊处理：获取评论的404错误（可能是帖子没有评论）
    if (error.response && 
        error.response.status === 404 && 
        error.config && 
        error.config.method === 'get' && 
        error.config.url.includes('/forum/posts/') && 
        error.config.url.includes('/comments')) {
      // 对于获取评论的404错误，只在控制台输出简单信息，不显示错误消息
      console.log(`帖子评论接口返回404，可能是该帖子没有评论: ${error.config.url}`);
      return Promise.reject(error);
    }
    
    console.error('请求失败:', error);
    
    // 处理401未授权错误
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      window.location.href = '/login';
    }
    
    // 打印更详细的错误信息便于调试
    if (error.response) {
      console.error('错误响应数据:', error.response.data)
      console.error('错误响应状态:', error.response.status)
      console.error('请求配置:', error.config)
      
      // 尝试解析请求数据
      try {
        if (error.config && error.config.data) {
          const requestData = JSON.parse(error.config.data)
          console.error('错误请求数据:', requestData)
        }
      } catch (e) {
        console.error('解析错误请求数据失败:', e)
      }
    }
    
    ElMessage.error(error.message || '网络请求失败')
    return Promise.reject(error)
  }
)

export default request