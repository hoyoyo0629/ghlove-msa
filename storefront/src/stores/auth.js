import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '../api/http'

export const useAuthStore = defineStore('auth', () => {
  const loggedIn = ref(false)
  const loading = ref(true)
  const me = ref(null)

  async function fetchMe() {
    loading.value = true
    try {
      const data = await api.get('member', '/api/auth/me')
      loggedIn.value = data.loggedIn
      me.value = data.loggedIn ? data : null
    } catch {
      loggedIn.value = false
      me.value = null
    } finally {
      loading.value = false
    }
  }

  async function login(loginId, password, target) {
    const data = await api.post('member', '/api/auth/login', { loginId, password, target })
    if (data.status === 'OK') await fetchMe()
    return data
  }

  async function verifyMfa(code) {
    const data = await api.post('member', '/api/auth/login/mfa-verify', { code })
    if (data.status === 'OK') await fetchMe()
    return data
  }

  async function logout() {
    await api.post('member', '/api/auth/logout')
    loggedIn.value = false
    me.value = null
  }

  async function signup(form) {
    const data = await api.post('member', '/api/auth/signup', form)
    if (data.status === 'OK') await fetchMe()
    return data
  }

  return { loggedIn, loading, me, fetchMe, login, verifyMfa, logout, signup }
})
