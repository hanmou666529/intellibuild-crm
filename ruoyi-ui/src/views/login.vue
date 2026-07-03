<template>
  <div class="login-root">
    <!-- Background Video -->
    <div class="login-video-wrap" :class="{'mobile-video': isMobile}">
      <video
        class="login-video"
        src="https://d8j0ntlcm91z4.cloudfront.net/user_38xzZboKViGWJOttwIXH07lWA1P/hf_20260508_215831_c6a8989c-d716-4d8d-8745-e972a2eec711.mp4"
        autoplay
        muted
        loop
        playsinline
      />
    </div>

    <!-- Gradient light orbs -->
    <div class="login-orbs">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
    </div>

    <!-- Decorative geometric shapes -->
    <div class="login-deco" @mousemove="onDecoMouseMove" @mouseleave="onDecoMouseLeave">
      <span class="deco-shape deco-1" :style="deco1Style"></span>
      <span class="deco-shape deco-2" :style="deco2Style"></span>
      <span class="deco-shape deco-3" :style="deco3Style"></span>
    </div>

    <!-- Navbar -->
    <div class="login-nav">
      <div class="login-nav-inner">
        <router-link to="/" class="login-logo-link">
          <svg viewBox="0 0 28 28" width="22" height="22" class="login-logo-svg">
            <rect x="2" y="2" width="20" height="20" rx="4" ry="4" fill="none" stroke="#000" stroke-width="2.5" transform="rotate(-35, 12, 12)"/>
            <rect x="4" y="4" width="16" height="16" rx="3" ry="3" fill="none" stroke="#000" stroke-width="2" transform="rotate(-35, 12, 12)"/>
          </svg>
          <span class="login-brand-text">{{ title }}</span>
        </router-link>
        <div class="login-nav-tags mobile-hidden">
          <span class="pill pill-light">CRM</span>
          <span class="pill pill-light">AI-Driven</span>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="login-body">
      <div class="login-card">
        <div class="login-card-accent"></div>
        <div class="login-card-inner">
          <div class="login-header">
            <h1 class="login-greeting">Welcome Back</h1>
            <p class="login-hint">立即登录，开启智能客户管理</p>
          </div>
          <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                type="text"
                auto-complete="off"
                placeholder="账号"
                prefix-icon="el-icon-user"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                auto-complete="off"
                placeholder="密码"
                show-password
                prefix-icon="el-icon-lock"
                @keyup.enter.native="handleLogin"
              />
            </el-form-item>
            <el-form-item prop="code" v-if="captchaEnabled">
              <div class="code-row">
                <el-input
                  v-model="loginForm.code"
                  auto-complete="off"
                  placeholder="验证码"
                  prefix-icon="el-icon-key"
                  class="code-input"
                  @keyup.enter.native="handleLogin"
                />
                <img :src="codeUrl" @click="getCode" class="code-img"/>
              </div>
            </el-form-item>
            <div class="login-options">
              <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
              <span class="forgot-pwd" @click="showForgotPwd">忘记密码？</span>
            </div>
            <el-form-item style="width:100%; margin-bottom:0">
              <el-button
                :loading="loading"
                size="medium"
                type="primary"
                class="login-btn"
                style="width:100%;"
                @click.native.prevent="handleLogin"
              >
                <span v-if="!loading">登 录</span>
                <span v-else>登 录 中...</span>
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>

    <!-- Third-party Login -->
    <div class="login-third-party">
      <span class="third-label">第三方账号登录</span>
      <div class="third-icons">
        <span class="third-icon disabled" title="即将上线">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 11a3 3 0 1 0 0-6 3 3 0 0 0 0 6z"/>
            <path d="M17 15a2 2 0 1 0 0-4 2 2 0 0 0 0 4z"/>
            <path d="M21 17a2 2 0 1 0 0-4 2 2 0 0 0 0 4z"/>
            <path d="M3 17a2 2 0 1 0 0-4 2 2 0 0 0 0 4z"/>
            <path d="M7 21a2 2 0 1 0 0-4 2 2 0 0 0 0 4z"/>
            <path d="M13 21c2 0 4-1 5-2"/>
            <path d="M7 9c0 3 2 6 5 8"/>
          </svg>
        </span>
        <span class="third-icon disabled" title="即将上线">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 3L3 10l3 8h12l3-8-9-7z"/>
            <path d="M12 10v6"/>
            <path d="M9 13l3 3 3-3"/>
          </svg>
        </span>
        <span class="third-icon disabled" title="即将上线">
          <svg viewBox="0 0 24 24" width="20" height="20">
            <circle cx="12" cy="12" r="10" fill="none" stroke="currentColor" stroke-width="1.5"/>
            <text x="12" y="15" text-anchor="middle" font-size="10" font-weight="700" fill="currentColor">G</text>
          </svg>
        </span>
      </div>
    </div>

    <!-- Footer -->
    <div class="login-footer-wrap">
      <div class="login-footer-inner">
        <div class="login-footer-left">
          <div class="login-footer-subtitle">
            <span class="dot-black"></span>
            <span>AI驱动的智能客户关系管理平台</span>
          </div>
          <div class="login-footer-tags">
            <span class="pill pill-border">CRM</span>
            <span class="pill pill-border">AI-Driven</span>
            <span class="pill pill-border">Enterprise</span>
          </div>
        </div>
        <div class="login-footer-right">
          <span>©2026–2027 智营CRM | v2.7.3 | AI-Driven</span>
        </div>
      </div>
    </div>
    <!-- Accessibility -->
    <div class="login-a11y" @click="handleA11yClick" title="无障碍辅助">
      <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="5" r="2"/>
        <path d="M7 11l-3 2"/>
        <path d="M17 11l3 2"/>
        <path d="M12 7v5l-2 5"/>
        <path d="M12 12l2 5"/>
        <path d="M9 20l3-3 3 3"/>
      </svg>
    </div>
  </div>
</template>

<script>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from '@/utils/jsencrypt'

export default {
  name: "Login",
  data() {
    return {
      isMobile: false,
      title: process.env.VUE_APP_TITLE,
      codeUrl: "",
      loginForm: {
        username: "admin",
        password: "admin123",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      loginRules: {
        username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
        password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      loading: false,
      captchaEnabled: true,
      register: false,
      redirect: undefined,
      mouseX: 0,
      mouseY: 0,
      decoBase: [
        { x: 0, y: 0 },
        { x: 0, y: 0 },
        { x: 0, y: 0 }
      ]
    }
  },
  computed: {
    deco1Style() {
      const dx = (this.mouseX - 0.5) * 20
      const dy = (this.mouseY - 0.5) * 20
      return { transform: `translate(${dx}px, ${dy}px) rotate(-35deg)` }
    },
    deco2Style() {
      const dx = (this.mouseX - 0.5) * -30
      const dy = (this.mouseY - 0.5) * -30
      return { transform: `translate(${dx}px, ${dy}px) rotate(-35deg)` }
    },
    deco3Style() {
      const dx = (this.mouseX - 0.5) * 15
      const dy = (this.mouseY - 0.5) * 15
      return { transform: `translate(${dx}px, ${dy}px) rotate(-35deg)` }
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
    this.getCode()
    this.getCookie()
  },
  mounted() {
    this.checkMobile()
    window.addEventListener('resize', this.checkMobile)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.checkMobile)
  },
  methods: {
    checkMobile() {
      this.isMobile = window.innerWidth < 768
    },
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img
          this.loginForm.uuid = res.uuid
        }
      })
    },
    getCookie() {
      const username = Cookies.get("username")
      const password = Cookies.get("password")
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      }
    },
    onDecoMouseMove(e) {
      const rect = e.currentTarget.getBoundingClientRect()
      this.mouseX = (e.clientX - rect.left) / rect.width
      this.mouseY = (e.clientY - rect.top) / rect.height
    },
    onDecoMouseLeave() {
      this.mouseX = 0.5
      this.mouseY = 0.5
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 })
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 })
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 })
          } else {
            Cookies.remove("username")
            Cookies.remove("password")
            Cookies.remove('rememberMe')
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || "/" }).catch(()=>{})
          }).catch(() => {
            this.loading = false
            if (this.captchaEnabled) this.getCode()
          })
        }
      })
    },
    showForgotPwd() {
      this.$alert('请联系管理员重置密码', '忘记密码', {
        confirmButtonText: '知道了',
        type: 'info'
      })
    },
    handleA11yClick() {
      this.$message('无障碍辅助功能正在开发中')
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.login-root {
  position: relative;
  min-height: 100vh;
  background: #fff;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

/* ─── Video Background ─── */

/* ─── Gradient Light Orbs ─── */

.login-orbs {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
}

.orb-1 {
  width: 500px;
  height: 500px;
  top: -10%;
  right: -5%;
  background: radial-gradient(circle, #0052D9, transparent 70%);
}

.orb-2 {
  width: 400px;
  height: 400px;
  bottom: 0;
  left: -8%;
  background: radial-gradient(circle, #00A86B, transparent 70%);
}

.orb-3 {
  width: 300px;
  height: 300px;
  top: 50%;
  left: 40%;
  background: radial-gradient(circle, #7C3AED, transparent 70%);
}

@media (max-width: 767px) {
  .orb-1 { width: 250px; height: 250px; }
  .orb-2 { width: 200px; height: 200px; }
  .orb-3 { display: none; }
}

.login-video-wrap {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
}

.login-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.35;
  mix-blend-mode: luminosity;
}

@media (max-width: 767px) {
  .login-video-wrap.mobile-video {
    width: 80%;
    height: 80%;
    top: 10%;
    left: 10%;
    opacity: 0.2;
  }
}

/* ─── Decorative Geometric Shapes ─── */

.login-deco {
  position: fixed;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  overflow: hidden;
}

@keyframes float1 {
  0%, 100% { transform: translate(0, 0) rotate(-35deg); }
  50%      { transform: translate(0, -14px) rotate(-33deg); }
}
@keyframes float2 {
  0%, 100% { transform: translate(0, 0) rotate(-35deg); }
  50%      { transform: translate(0, 20px) rotate(-37deg); }
}
@keyframes float3 {
  0%, 100% { transform: translate(0, 0) rotate(-35deg); }
  50%      { transform: translate(0, -10px) rotate(-32deg); }
}

.deco-shape {
  position: absolute;
  border: 1.5px solid rgba(0,0,0,0.1);
  border-radius: 6px;
  background: rgba(255,255,255,0.15);
  backdrop-filter: blur(4px);
  transition: transform 0.15s ease-out;
}

.deco-1 {
  width: 60px;
  height: 60px;
  top: 15%;
  left: 8%;
  animation: float1 6s ease-in-out infinite;
}

.deco-2 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  right: 12%;
  border-color: rgba(0,0,0,0.06);
  animation: float2 8s ease-in-out infinite;
}

.deco-3 {
  width: 40px;
  height: 40px;
  top: 40%;
  left: 55%;
  animation: float3 5s ease-in-out infinite;
}

/* ─── Navbar ─── */

.login-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 50;
  padding: 14px 32px;
  pointer-events: none;
}

.login-nav > * { pointer-events: auto; }

.login-nav-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.login-logo-link {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
}

.login-logo-svg { flex-shrink: 0; }

.login-brand-text {
  font-size: 15px;
  font-weight: 600;
  color: #000;
  letter-spacing: -0.02em;
}

.login-nav-tags {
  display: flex;
  gap: 6px;
}

@media (max-width: 767px) {
  .login-nav { padding: 12px 16px; }
  .login-nav-tags { display: none; }
}

/* ─── Body / Login Card ─── */

.login-body {
  position: relative;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: 100px 20px 140px;
}

.login-card {
  width: 440px;
  background: rgba(255,255,255,0.55);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255,255,255,0.2);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 40px rgba(0,0,0,0.04), 0 0 80px rgba(0,82,217,0.06);
}

.login-card-accent {
  height: 3px;
  background: linear-gradient(90deg, #0052D9 0%, #7C3AED 50%, #00A86B 100%);
}

.login-card-inner {
  padding: 40px 40px 48px;
}

.login-header {
  margin-bottom: 32px;
}

.login-greeting {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 6px 0;
  color: #000;
  letter-spacing: -0.02em;
}

.login-hint {
  font-size: 13px;
  color: rgba(0,0,0,0.4);
  margin: 0;
}

.login-form {
  .el-form-item { margin-bottom: 16px; }

  .el-input__prefix {
    left: 14px !important;
  }

  .code-row {
    display: flex;
    gap: 12px;
    align-items: center;

    .code-input { flex: 1; }
    .code-img {
      width: 110px;
      height: 38px;
      border-radius: 9999px;
      cursor: pointer;
      object-fit: cover;
      border: 1px solid rgba(0,0,0,0.15);
    }
  }
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.forgot-pwd {
  font-size: 13px;
  color: #0052D9;
  cursor: pointer;
  user-select: none;
}
.forgot-pwd:hover {
  color: #003db3;
  text-decoration: underline;
}

.login-btn {
  background: rgba(0,82,217,0.85) !important;
  backdrop-filter: blur(12px) !important;
  -webkit-backdrop-filter: blur(12px) !important;
  border: 1px solid rgba(255,255,255,0.15) !important;
  height: 44px !important;
  line-height: 44px !important;
  font-weight: 600 !important;
  font-size: 15px !important;
  letter-spacing: 0.04em !important;
  border-radius: 12px !important;
  box-shadow: 0 2px 12px rgba(0,82,217,0.15) !important;
  transition: all 0.25s ease !important;
  color: #fff !important;
}
.login-btn:hover {
  background: rgba(38,109,240,0.9) !important;
  backdrop-filter: blur(16px) !important;
  border-color: rgba(255,255,255,0.3) !important;
  box-shadow: 0 6px 24px rgba(0,82,217,0.25) !important;
}
.login-btn:active {
  background: rgba(0,65,173,0.9) !important;
  box-shadow: 0 1px 6px rgba(0,82,217,0.15) !important;
}

/* ─── Third-party Login ─── */

.login-third-party {
  position: relative;
  z-index: 20;
  text-align: center;
  padding: 8px 0 20px;
}

.third-label {
  display: block;
  font-size: 12px;
  color: rgba(0,0,0,0.3);
  margin-bottom: 10px;
}

.third-icons {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.third-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(0,0,0,0.04);
  color: rgba(0,0,0,0.2);
  cursor: not-allowed;
  transition: all 0.2s;
}
.third-icon.disabled:hover {
  background: rgba(0,0,0,0.08);
  color: rgba(0,0,0,0.35);
}

/* ─── Accessibility Icon ─── */

.login-a11y {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 100;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255,255,255,0.85);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(0,0,0,0.08);
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: rgba(0,0,0,0.4);
  transition: all 0.2s;
}
.login-a11y:hover {
  background: #fff;
  color: #000;
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
}

/* ─── Footer ─── */

.login-footer-wrap {
  position: relative;
  z-index: 30;
  background: linear-gradient(to top, #ffffff 0%, rgba(255,255,255,0.8) 50%, transparent 100%);
  padding: 32px 32px 28px;
}

.login-footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
}

@media (max-width: 767px) {
  .login-footer-wrap { padding: 24px 16px 20px; }
  .login-footer-inner { flex-direction: column; align-items: flex-start; gap: 16px; }
}

.login-footer-subtitle {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: rgba(0,0,0,0.55);
  margin-bottom: 12px;
}

.dot-black {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #000;
  flex-shrink: 0;
}

.login-footer-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.login-footer-right {
  font-size: 11px;
  color: rgba(0,0,0,0.35);
  flex-shrink: 0;
  white-space: nowrap;
}

@media (max-width: 767px) {
  .login-footer-tags .pill-border { display: none; }
}
</style>
