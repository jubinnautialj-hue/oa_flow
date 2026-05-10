import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    username: (state) => state.userInfo.username || ''
  },

  actions: {
    async login(username, password) {
      const response = await request.post('/auth/login', {
        username,
        password
      })
      this.token = response.token
      this.userInfo = {
        username: response.username,
        name: response.name
      }
      localStorage.setItem('token', this.token)
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      return response
    },

    async getCurrentUser() {
      try {
        const response = await request.get('/auth/current')
        this.userInfo = response
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        return response
      } catch (error) {
        return null
      }
    },

    logout() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})
