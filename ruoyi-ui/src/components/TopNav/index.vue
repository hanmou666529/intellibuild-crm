<template>
  <div class="topnav-pills">
    <span
      v-for="(item, index) in topMenus"
      :key="index"
      v-if="index < visibleNumber"
      class="topnav-pill"
      :class="{'is-active': activeMenu === item.path}"
      @click="handleSelect(item.path)"
    >
      {{ item.meta.title }}
    </span>
    <el-dropdown v-if="topMenus.length > visibleNumber" trigger="click" class="topnav-more">
      <span class="topnav-pill topnav-pill-more">更多</span>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item
          v-for="(item, index) in topMenus"
          :key="index"
          v-if="index >= visibleNumber"
          @click.native="handleSelect(item.path)"
        >{{ item.meta.title }}</el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </div>
</template>

<script>
import { constantRoutes } from "@/router"
import { isHttp } from "@/utils/validate"

// 隐藏侧边栏路由
const hideList = ['/index', '/user/profile']

export default {
  data() {
    return {
      // 顶部栏初始数
      visibleNumber: 5,
      // 当前激活菜单的 index
      currentIndex: undefined
    }
  },
  computed: {
    theme() {
      return this.$store.state.settings.theme
    },
    // 顶部显示菜单
    topMenus() {
      let topMenus = []
      this.routers.map((menu) => {
        if (menu.hidden !== true) {
          // 兼容顶部栏一级菜单内部跳转
          if (menu.path === '/' && menu.children) {
            topMenus.push(menu.children[0])
          } else {
            topMenus.push(menu)
          }
        }
      })
      return topMenus
    },
    // 所有的路由信息
    routers() {
      return this.$store.state.permission.topbarRouters
    },
    // 设置子路由
    childrenMenus() {
      var childrenMenus = []
      this.routers.map((router) => {
        for (var item in router.children) {
          if (router.children[item].parentPath === undefined) {
            if(router.path === "/") {
              router.children[item].path = "/" + router.children[item].path
            } else {
              if(!isHttp(router.children[item].path)) {
                router.children[item].path = router.path + "/" + router.children[item].path
              }
            }
            router.children[item].parentPath = router.path
          }
          childrenMenus.push(router.children[item])
        }
      })
      return constantRoutes.concat(childrenMenus)
    },
    // 默认激活的菜单
    activeMenu() {
      const path = this.$route.path
      let activePath = path
      if (path !== undefined && path.lastIndexOf("/") > 0 && hideList.indexOf(path) === -1) {
        const tmpPath = path.substring(1, path.length)
        if (!this.$route.meta.link) {
          activePath = "/" + tmpPath.substring(0, tmpPath.indexOf("/"))
          this.$store.dispatch('app/toggleSideBarHide', false)
        }
      } else if(!this.$route.children) {
        activePath = path
        this.$store.dispatch('app/toggleSideBarHide', true)
      }
      this.activeRoutes(activePath)
      return activePath
    },
  },
  beforeMount() {
    window.addEventListener('resize', this.setVisibleNumber)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.setVisibleNumber)
  },
  mounted() {
    this.setVisibleNumber()
  },
  methods: {
    // 根据宽度计算设置显示栏数
    setVisibleNumber() {
      const width = document.body.getBoundingClientRect().width / 3
      this.visibleNumber = parseInt(width / 85)
    },
    // 菜单选择事件
    handleSelect(key, keyPath) {
      this.currentIndex = key
      const route = this.routers.find(item => item.path === key)
      if (isHttp(key)) {
        // http(s):// 路径新窗口打开
        window.open(key, "_blank")
      } else if (!route || !route.children) {
        // 没有子路由路径内部打开
        const routeMenu = this.childrenMenus.find(item => item.path === key)
        if (routeMenu && routeMenu.query) {
          let query = JSON.parse(routeMenu.query)
          this.$router.push({ path: key, query: query })
        } else {
          this.$router.push({ path: key })
        }
        this.$store.dispatch('app/toggleSideBarHide', true)
      } else {
        // 显示左侧联动菜单
        this.activeRoutes(key)
        this.$store.dispatch('app/toggleSideBarHide', false)
      }
    },
    // 当前激活的路由
    activeRoutes(key) {
      var routes = []
      if (this.childrenMenus && this.childrenMenus.length > 0) {
        this.childrenMenus.map((item) => {
          if (key == item.parentPath || (key == "index" && "" == item.path)) {
            routes.push(item)
          }
        })
      }
      if(routes.length > 0) {
        this.$store.commit("SET_SIDEBAR_ROUTERS", routes)
      } else {
        this.$store.dispatch('app/toggleSideBarHide', true)
      }
    }
  },
}
</script>

<style lang="scss">
.topnav-pills {
  display: flex;
  align-items: center;
  gap: 4px;
}

.topnav-pill {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 500;
  color: rgba(0,0,0,0.55);
  border-radius: 9999px;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.topnav-pill:hover {
  color: #000;
  background: #f4f4f6;
}

.topnav-pill.is-active {
  color: #000;
  background: #f0f0f0;
  font-weight: 600;
}

.topnav-pill-more {
  border: 1px solid rgba(0,0,0,0.12);
  font-size: 11px;
}

.topnav-more {
  margin-left: 4px;
}

@media (max-width: 767px) {
  .topnav-pills { display: none; }
}
</style>
