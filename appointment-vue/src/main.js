import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 全局样式
import '@/styles/index.scss'

// 权限控制模块需要在应用初始化前引入
import '@/permission'

// 创建应用实例
const app = createApp(App)

// 使用插件
app.use(store)
app.use(router)
app.use(ElementPlus)

// 全局错误处理
app.config.errorHandler = (error, instance, info) => {
  console.error('Global error:', error)
  console.error('Error info:', info)
  
  // 显示用户友好的错误信息
  if (error.message && error.message.includes('Failed to fetch dynamically imported module')) {
    console.error('模块加载失败，可能是网络问题或文件路径错误')
  }
}

// 全局警告处理
app.config.warnHandler = (msg, instance, trace) => {
  console.warn('Vue warning:', msg, trace)
}

// 简化的初始化函数
async function initApp() {
  console.log('🚀 Starting app initialization...')
  console.log('📄 Document ready state:', document.readyState)
  console.log('🎯 App element exists:', !!document.getElementById('app'))
  
  try {
    // 检查DOM元素
    const appElement = document.getElementById('app')
    if (!appElement) {
      throw new Error('找不到#app元素')
    }
    
    console.log('📦 App element content:', appElement.innerHTML.substring(0, 100))
    
    // 直接挂载应用 - 这是最关键的一步
    console.log('🎯 Mounting Vue app...')
    app.mount('#app')
    console.log('✅ Vue app mounted successfully')
    
    // 检查挂载后的状态
    setTimeout(() => {
      console.log('📊 Post-mount check - App element content:', appElement.innerHTML.substring(0, 100))
      if (appElement.innerHTML.includes('正在加载')) {
        console.warn('⚠️ App still showing loading state after mount - this might be normal')
      } else {
        console.log('✅ App content updated after mount')
      }
    }, 500)
    
    // 挂载后再进行其他初始化 - 延迟执行确保DOM更新
    setTimeout(async () => {
      try {
        // 初始化用户状态
        console.log('👤 Initializing user state...')
        await store.dispatch('user/initUserState')
        console.log('✅ User state initialized')
      } catch (error) {
        console.error('❌ Failed to initialize user state:', error)
        // 用户状态初始化失败不影响应用基本功能
      }
    }, 200)
    
  } catch (error) {
    console.error('❌ Failed to mount Vue app:', error)
    console.error('📋 Error stack:', error.stack)
    
    // 如果挂载失败，显示错误信息
    const appElement = document.getElementById('app')
    if (appElement) {
      appElement.innerHTML = `
        <div style="
          display: flex; 
          flex-direction: column; 
          justify-content: center; 
          align-items: center; 
          height: 100vh; 
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
          color: white; 
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
          text-align: center;
          padding: 20px;
        ">
          <div style="background: rgba(255,255,255,0.1); padding: 30px; border-radius: 15px; backdrop-filter: blur(10px);">
            <h2 style="margin-bottom: 15px; font-size: 24px;">🚨 应用加载失败</h2>
            <p style="margin-bottom: 20px; opacity: 0.9;">${error.message}</p>
            <button onclick="location.reload()" style="
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
              color: white;
              border: none;
              padding: 12px 24px;
              border-radius: 8px;
              cursor: pointer;
              font-size: 16px;
              font-weight: 600;
              transition: all 0.3s ease;
            " onmouseover="this.style.transform='translateY(-2px)'" onmouseout="this.style.transform='translateY(0)'">
              🔄 重新加载
            </button>
          </div>
        </div>
      `
    }
  }
}

// 确保DOM加载完成后再初始化
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initApp)
} else {
  // DOM已经加载完成
  initApp()
}

// 添加全局调试函数
window.debugVueApp = function() {
  console.log('=== 🔧 Vue应用调试信息 ===');
  console.log('🎯 Vue版本:', window.Vue ? window.Vue.version : '未找到');
  console.log('📱 Vue应用实例:', window.__VUE_APP__ || '未找到');
  console.log('🗺️ Vue Router:', window.__VUE_ROUTER__ || '未找到');
  console.log('💾 Vuex Store:', window.__VUEX__ || '未找到');
  console.log('🎯 #app元素:', document.getElementById('app'));
  console.log('📝 #app内容:', document.getElementById('app')?.innerHTML?.substring(0, 200));
  console.log('🌐 当前URL:', window.location.href);
  console.log('👤 用户代理:', navigator.userAgent);
  console.log('========================');
}

// 定期输出调试信息（减少频率）
setInterval(() => {
  if (window.debugVueApp) {
    console.log('🔍 定期调试检查 - Vue应用状态:');
    window.debugVueApp();
  }
}, 10000)
