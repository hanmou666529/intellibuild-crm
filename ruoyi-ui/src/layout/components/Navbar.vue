<template>
  <div class="navbar">
    <div class="nav-inner">
      <div class="nav-left">
        <router-link to="/index" class="nav-logo">
          <svg viewBox="0 0 28 28" width="22" height="22" class="logo-svg">
            <rect x="2" y="2" width="20" height="20" rx="4" ry="4" fill="none" stroke="#000" stroke-width="2.5" transform="rotate(-35, 12, 12)"/>
            <rect x="4" y="4" width="16" height="16" rx="3" ry="3" fill="none" stroke="#000" stroke-width="2" transform="rotate(-35, 12, 12)"/>
          </svg>
          <span class="brand-text">智营CRM</span>
        </router-link>

        <top-nav v-if="topNav" id="topmenu-container" class="topmenu-container" />

        <div class="nav-tags mobile-hidden">
          <span class="pill pill-light">CRM</span>
          <span class="pill pill-light">AI-Driven</span>
        </div>
      </div>

      <div class="nav-right">
        <div class="pill pill-black pill-btn menu-trigger" @click="toggleSideBar">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2">
            <line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/>
          </svg>
          <span>Menu</span>
        </div>

        <operation-guide/>

        <el-dropdown trigger="click" class="user-pill-wrap">
          <div class="user-pill pill pill-light pill-btn">
            <img :src="avatar" class="user-avatar">
            <span class="user-name">{{ nickName }}</span>
          </div>
          <el-dropdown-menu slot="dropdown">
            <router-link to="/user/profile">
              <el-dropdown-item icon="el-icon-user">个人中心</el-dropdown-item>
            </router-link>
            <el-dropdown-item divided @click.native="logout" icon="el-icon-switch-button">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import TopNav from '@/components/TopNav'
import OperationGuide from '@/components/OperationGuide'

export default {
  components: { TopNav, OperationGuide },
  computed: {
    ...mapGetters(['sidebar', 'avatar', 'device', 'nickName']),
    topNav: {
      get() { return this.$store.state.settings.topNav }
    }
  },
  methods: {
    toggleSideBar() {
      this.$store.dispatch('app/toggleSideBar')
    },
    logout() {
      this.$confirm('确定注销并退出系统吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('LogOut').then(() => {
          location.href = '/index'
        })
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 50;
  background: rgba(255,255,255,0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0,0,0,0.06);
  pointer-events: none;
  animation: fadeSlideDown 0.8s cubic-bezier(0.16, 1, 0.3, 1) both;

  > * { pointer-events: auto; }

  .nav-inner {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 24px;
    height: 50px;
  }

  .nav-left {
    display: flex;
    align-items: center;
    gap: 20px;
  }

  .nav-logo {
    display: flex;
    align-items: center;
    gap: 8px;
    text-decoration: none;

    .logo-svg { flex-shrink: 0; }
    .brand-text {
      font-size: 15px;
      font-weight: 600;
      color: #000;
      letter-spacing: -0.02em;
    }
    @media (max-width: 767px) {
      .brand-text { display: none; }
    }
  }

  .topmenu-container {
    margin: 0;
  }

  .nav-tags {
    display: flex;
    gap: 6px;
    @media (max-width: 767px) { display: none; }
  }

  .nav-right {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .menu-trigger {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 14px 6px 12px;
    font-size: 11px;
    cursor: pointer;
  }

  .user-pill-wrap {
    outline: none;
  }

  .user-pill {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 4px 12px 4px 4px;
    cursor: pointer;

    .user-avatar {
      width: 24px;
      height: 24px;
      border-radius: 50%;
      object-fit: cover;
    }
    .user-name {
      font-size: 12px;
      font-weight: 500;
      color: #000;
    }
  }
}
</style>
