// app模块
const state = {
  sidebar: {
    isCollapse: false
  },
  device: 'desktop'
}

const mutations = {
  TOGGLE_SIDEBAR: state => {
    state.sidebar.isCollapse = !state.sidebar.isCollapse
  },
  CLOSE_SIDEBAR: state => {
    state.sidebar.isCollapse = true
  },
  OPEN_SIDEBAR: state => {
    state.sidebar.isCollapse = false
  },
  TOGGLE_DEVICE: (state, device) => {
    state.device = device
  }
}

const actions = {
  toggleSidebar({ commit }) {
    commit('TOGGLE_SIDEBAR')
  },
  closeSidebar({ commit }) {
    commit('CLOSE_SIDEBAR')
  },
  openSidebar({ commit }) {
    commit('OPEN_SIDEBAR')
  },
  toggleDevice({ commit }, device) {
    commit('TOGGLE_DEVICE', device)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
} 