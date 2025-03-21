<template>
  <el-menu :default-active="defaultActive" :default-openeds="defaultOpeneds" class="api-system-menu" @open="handleOpen"
    @close="handleClose" @select="menuSelected" style="padding: 10px 0px 14px 0px;">
    <el-submenu v-for="(item, index) in menuData" :index="'main_' + index" :key="'main_' + index"
      style="padding-left: 0px;">
      <template slot="title" style="padding-left: 10px;">
        <i style="font-size: 16px; margin-top: 0px; margin-right: 2px;" class="el-icon-coin"></i>
        <span :style="{ 'color': item.children && item.children.some(it => it.check) ? '#409EFF' : 'inherit' }">{{
          item.name
        }}</span>
      </template>
      <el-menu-item-group>
        <el-menu-item v-for="(subitem, subindex) in item.children" :index="subitem.apiUrl" :key="subitem.apiUrl"
          :ref="'item=' + subitem.apiUrl"
          :style="{ 'padding-left': '43px', 'text-decoration': subitem.isDeprecated ? 'line-through' : 'none', }">
          <span :style="{ 'color': subitem.check ? '#409EFF' : 'inherit' }">{{ subitem.apiName }}</span>
          <i class="el-icon-paperclip" style="color:#67C23A" v-show="checkHasOpen(subitem)"></i>
        </el-menu-item>
      </el-menu-item-group>
    </el-submenu>
  </el-menu>
</template>
<script>
export default {

  data() {
    return {
      /* tree */

      defaultActive: '', //  menuGroup[0].children[0].path,

      defaultOpeneds: [],

      uriMap: {}

    }

  },
  getRoutes() {
    return this.routes
  },
  mounted() {

    this.loadMenu()
  },
  created() { },
  computed: {
    allTreeNode() {
      return this.map
    }
  },
  props: {

    selectedTreeNodeId: {
      type: String,
      default: ''
    },
    hasOpen: {
      type: Array,
      default: ()=>[]
    },
    menuData: {
      type: Array,
      default: ()=>[]
    },

  },

  watch: {
    'defaultActive'(val, old) { },

    'menuData'(val, old) {

      this.loadMenu()
    }
  },
  methods: {
    checkHasOpen(subitem) {
      return this.hasOpen.some(it => it.uri == subitem.apiUrl)
    },
    menuSelected(index, indexPath) {



      // console.log({
      //   label: this.uriMap[index],
      //   uri: index,
      // })

      this.$emit('treeNodeClick', {
        label: this.uriMap[index],
        uri: index,
      })

      this.defaultActive = index

    },

    handleOpen(key, keyPath) { },
    handleClose(key, keyPath) { },

    releaseHold() {
      this.hold = false
    },
    loadMenu() {

      var _this = this



      let tmpArray = _this.menuData

      for (var i = 0; i < tmpArray.length; i++) {

        _this.defaultOpeneds.push('main_' + i)

        for (var j = 0; j < tmpArray[i].children.length; j++) {

          tmpArray[i].children.forEach(e => {
            _this.uriMap[e.apiUrl] = e.apiName
          })

        }

      }

    },
    isVisible(dom) {
      const scrTop = document.documentElement.scrollHeight || document.body.scrollHeight;
      return !(scrTop > (dom.offsetTop + dom.offsetHeight) || (scrTop + window.innerHeight) < dom.offsetTop);
    },
    setActive(uri) {
      this.defaultActive = uri
      let dom = this.$refs['item=' + uri]
      if (dom) {

        // if (dom instanceof Array) {
        //   dom = dom[0]
        // }
        // if (!this.isVisible(dom.$el)) {
        //   dom.$el.scrollIntoView(false)
        // }
      }
    },
    setScrollIntoView(uri) {
      let dom = this.$refs['item=' + uri]
      if (dom) {
        if (dom instanceof Array) {
          dom = dom[0]
        }
        if (!this.isVisible(dom.$el)) {
          dom.$el.scrollIntoView(false)
        }
      }
    },


    selectTreeId(treeNodeId) {
      this.$refs.tree.setCurrentKey(treeNodeId)
    },

    selectNodeByUri(uri) {
      return this.allTreeNode[uri]
    },


  }
}

</script>
<style>
.api-system-menu {}

.api-system-menu div {
  padding: 0px;
}

.api-system-menu .el-submenu__title {
  height: 34px;
  line-height: 34px;
  margin-left: -8px;
}

.api-system-menu .el-menu-item {}

.api-system-menu .el-menu-item-group .is-active {
  color: #1c72de;
  background-color: #d6eaee;
  font-weight: bold;
}

.el-submenu__title {
  position: sticky;
  top: 10px;
  z-index: 2;
  background-color: #fff;
}

.el-submenu__title span {
  font-weight: bold;
}
</style>
