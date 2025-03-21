<template>
  <el-menu :default-active="defaultActive" :default-openeds="defaultOpeneds" class="api-system-menu" @open="handleOpen" @close="handleClose" @select="menuSelected" style="padding: 10px 0px 14px 0px;">
    <el-submenu v-for="(item,index) in menuData" :index="'main_'+index" :key="'main_'+index" style="padding-left: 0px;">
      <template slot="title" style="padding-left: 10px;">
        <i style="font-size: 16px; margin-top: 0px; margin-right: 2px;" class="el-icon-folder"></i>
        <span style="font-weight: normal;">{{item.name}}</span>
      </template>
      <el-menu-item-group>
        <el-menu-item v-for="(subitem, subindex) in item.children" :index="subitem.apiUrl" :key="subitem.apiUrl"
          :style="{ 'padding-left': '43px', 'text-decoration': subitem.isDeprecated? 'line-through': 'none' }">
          {{subitem.apiName}}
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
  created() {},
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

    menuData: {
      type: Array,
      default: []
    },

    menuIndex: ''

  },

  watch: {
    'defaultActive'(val, old) {},

    'menuData'(val, old) {

      this.loadMenu()
    }
  },
  methods: {

    menuSelected(index, indexPath) {

      this.$emit('treeNodeClick', {
        label: this.uriMap[index],
        uri: index,
        menuIndex: this.menuIndex
      })

      this.defaultActive = index

    },

    handleOpen(key, keyPath) {},
    handleClose(key, keyPath) {},

    releaseHold() {
      this.hold = false
    },
    loadMenu() {

      var _this = this

      let tmpArray = _this.menuData

      for (var i = 0; i < tmpArray.length; i++){

        _this.defaultOpeneds.push('main_' + i)

        for (var j = 0; j < tmpArray[i].children.length; j++) {

          tmpArray[i].children.forEach(e => {
            _this.uriMap[e.apiUrl] = e.apiName
          })

        }

      }

    },

    setActive(uri) {
      this.defaultActive = uri
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
  background-color: #ebf6f8;
  font-weight: bold;
}

/*.el-submenu__title {*/
/*  position: sticky;*/
/*  top: 10px;*/
/*  z-index: 2;*/
/*  background-color: #fff;*/
/*}*/

.el-submenu__title span {
  font-weight: bold;
}

</style>
