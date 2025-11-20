import { createStore } from 'vuex'
import user from './modules/user'
import app from './modules/app'
import permission from './modules/permission'

const store = createStore({
  modules: {
    user,
    app,
    permission
  },
  strict: process.env.NODE_ENV !== 'production'
})

export default store 