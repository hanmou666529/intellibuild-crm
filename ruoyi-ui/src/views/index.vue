<template>
  <div class="hero-page">
    <div class="hero-video-wrap" :class="{'mobile-video': isMobile}">
      <video
        class="hero-video"
        src="https://d8j0ntlcm91z4.cloudfront.net/user_38xzZboKViGWJOttwIXH07lWA1P/hf_20260508_215831_c6a8989c-d716-4d8d-8745-e972a2eec711.mp4"
        autoplay
        muted
        loop
        playsinline
      />
    </div>

    <div class="hero-footer">
      <div class="hero-footer-row">
        <div class="hero-left">
          <div class="hero-subtitle anim-subtitle delay-1">
            <span class="dot-black"></span>
            <span>AI驱动的智能客户关系管理平台</span>
          </div>
          <h1 class="hero-heading anim-title delay-2">
            欢迎使用智营CRM<br>
            <span class="hero-sub-heading">AI驱动的智能客户关系管理平台</span>
          </h1>
          <div class="hero-buttons anim-content delay-3">
            <router-link to="/crm/dashboard" class="hero-btn hero-btn-primary">进入工作台</router-link>
            <router-link to="/crm/customer/index" class="hero-btn hero-btn-outline">客户管理</router-link>
          </div>
        </div>
        <div class="hero-right">
          <span class="pill pill-border">CRM</span>
          <span class="pill pill-border">AI-Driven</span>
          <span class="pill pill-border">Enterprise</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Index",
  data() {
    return {
      isMobile: false
    }
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
    }
  }
}
</script>

<style scoped>
.hero-page {
  position: relative;
  min-height: calc(100vh - 86px);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  background: #fff;
  margin: -20px;
  overflow: hidden;
}

.hero-video-wrap {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
  animation: scaleIn 1.8s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.hero-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

@media (max-width: 767px) {
  .hero-video-wrap.mobile-video {
    width: 80%;
    height: 80%;
    top: 10%;
    left: 10%;
  }
}

.hero-footer {
  position: relative;
  z-index: 30;
  padding: 40px 40px 48px;
  background: linear-gradient(to top, #ffffff 0%, rgba(255,255,255,0.8) 50%, transparent 100%);
  animation: fadeSlideUp 1s cubic-bezier(0.16, 1, 0.3, 1) 0.5s both;
}

.hero-footer-row {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 40px;
}

@media (max-width: 767px) {
  .hero-footer {
    padding: 24px 20px 32px;
  }
  .hero-footer-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 24px;
  }
}

.hero-left {
  flex: 1;
}

.hero-subtitle {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: rgba(0,0,0,0.55);
  margin-bottom: 16px;
  animation-delay: 0.6s;
}

.dot-black {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #000;
  flex-shrink: 0;
}

.hero-heading {
  font-weight: 300;
  font-size: clamp(2rem, 8vw, 4.5rem);
  letter-spacing: -0.03em;
  line-height: 1.1;
  color: #000;
  margin: 0 0 28px;
  animation-delay: 0.8s;
}

.hero-sub-heading {
  font-size: clamp(0.85rem, 2.5vw, 1.4rem);
  font-weight: 400;
  color: rgba(0,0,0,0.5);
  letter-spacing: 0;
}

@media (min-width: 768px) {
  .hero-heading {
    font-size: clamp(2.5rem, 5.5vw, 4.5rem);
  }
}

.hero-buttons {
  display: flex;
  gap: 12px;
  animation-delay: 1.0s;
}

.hero-btn {
  display: inline-flex;
  align-items: center;
  padding: 10px 24px;
  border-radius: 9999px;
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s ease;
}
.hero-btn-primary {
  background: #000;
  color: #fff;
  border: 1px solid #000;
}
.hero-btn-primary:hover {
  background: #333;
}
.hero-btn-outline {
  background: transparent;
  color: #000;
  border: 1px solid rgba(0,0,0,0.35);
}
.hero-btn-outline:hover {
  border-color: #000;
}

.hero-right {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

@media (max-width: 767px) {
  .hero-right { display: none; }
}
</style>
