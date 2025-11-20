// import { defineConfig, loadEnv } from 'vite'
// import vue from '@vitejs/plugin-vue'
// import { fileURLToPath, URL } from 'node:url'
// import path from 'path'

// // 根据当前的模式（development 或 production）加载对应的环境变量
// const env = loadEnv(import.meta.env.MODE, process.cwd(), '')

// export default defineConfig(({ mode }) => {
//   return {
//     plugins: [vue()],
//     resolve: {
//       alias: {
//         '@': fileURLToPath(new URL('./src', import.meta.url))
//       }
//     },
//     server: {
//       port: 3000,
//       proxy: {
//         '/api': {
//           target: env.VITE_API_BASE_URL || 'http://localhost:8080', // 使用环境变量或默认值
//           changeOrigin: true,
//           ws: true,
//           rewrite: (path) => path.replace(/^\/api/, '')
//         }
//       }
//     },
//     base: '/',
//     build: {
//       outDir: 'dist',
//       assetsDir: 'assets',
//       sourcemap: true
//     },
//     // 在客户端代码中定义环境变量
//     define: {
//       __API_BASE_URL__: JSON.stringify(env.VITE_API_BASE_URL),
//     }
//   }
// })
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig(({ mode }) => {
  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port: 3000,
      strictPort: true, // 严格使用3000端口，不自动切换
      proxy: {
        '/api': {
          target: 'http://localhost:8080', // 本地开发环境
          changeOrigin: true,
          ws: true
          // 注意：这里不需要重写，保持 /api 前缀
        }
      }
    },
    base: '/',
    build: {
      outDir: 'dist',
      assetsDir: 'assets',
      sourcemap: true
    }
  }
})