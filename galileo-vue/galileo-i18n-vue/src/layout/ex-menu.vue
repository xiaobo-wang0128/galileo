<template>
    <el-menu
      :default-active="defaultActive"
      :default-openeds="defaultOpeneds"
      class="asama-system-menu"
      @open="handleOpen"
      @close="handleClose"
      @select="menuSelected"
      style="  padding: 10px 0px 10px 0px;"
    >
      <el-submenu v-for="(item,index) in treeData" :index="'main_'+index" :key="'main_'+index" style="padding-left: 0px;">
        <template slot="title" style="padding-left: 10px;">
          <i style="font-size: 16px; margin-top: 0px; margin-right: 2px;" :class="item.iconCls"></i>
          <span >{{item.label}}</span>
        </template>

        <el-menu-item-group>
          <el-menu-item v-for="(subitem, subindex) in item.children" :index="subitem.uri" :key="subitem.uri" style="padding-left: 43px;">{{subitem.label}}</el-menu-item>
        </el-menu-item-group>

      </el-submenu>
    </el-menu>

</template>
<script>


var treeJson = [{
  "label": "配置",
  "iconCls": "el-icon-coin",
  "children": [
  {
    "label": "系统参数配置",
    "uri": $portal_context + "/page/system/sys_config"
  },
  {
    "label": "流程相关配置",
    "uri": $portal_context + "/page/system/flow_config"
  },
  {
    "label": "插件配置",
    "uri": $portal_context + "/page/system/plugin_config"
  },
  {
    "label": "开放接口",
    "uri": $portal_context + "/page/system/api"
  },
    {
      "label": "国际化配置",
      "uri": $portal_context + "/page/system/i18n_config"
    }]
}];


export default {

  data () {
    return {
      /* tree */
      treeData: treeJson,

      defaultActive: '', //  menuGroup[0].children[0].path,
      defaultOpeneds: [],

      uriMap: {}
    }
  },
  getRoutes () {
    return this.routes
  },
  mounted () {

  },
  created () {
    this.loadMenu()
  },
  computed: {
    allTreeNode () {
      return this.map
    }
  },
  props: [
    'selectedTreeNodeId'
  ],
  watch: {
    'defaultActive'(val, old){
    }
  },
  methods: {

    menuSelected(index, indexPath) {


      this.$emit('treeNodeClick', {
        label:  this.uriMap[index],
        uri: index,
      })

      this.defaultActive = index

    },

    handleOpen(key, keyPath) {
    },
    handleClose(key, keyPath) {
    },

    releaseHold () {
      this.hold = false
    },
    loadMenu () {
      var _this = this

      let tmpArray =  _this.treeData

      for (var i = 0; i < tmpArray.length; i++) {
        for (var j = 0; j < tmpArray[i].children.length; j++) {

          _this.defaultOpeneds.push('main_' + j)

          tmpArray[i].children.forEach(e=>{
            _this.uriMap[ e.uri ] = e.label
          })

        }
      }



      // _this.$ajax({
      //   url: '/asama/user/MenuRpc/tree.json',
      //   method: 'post',
      //   data: {
      //     securityId: _this.securityId
      //   },
      //   success: function (responseData) {
      //     if (responseData.data && responseData.data.rows) {
      //       var tmpArray = responseData.data.rows

      //       // var resultArray = []

      //       for (var i = 0; i < tmpArray.length; i++) {
      //         for (var j = 0; j < tmpArray[i].children.length; j++) {

      //           _this.defaultOpeneds.push('main_' + j)

      //           tmpArray[i].children[j].children.forEach(e=>{
      //             _this.uriMap[ e.uri ] = e.label
      //           })

      //         }
      //       }

      //       _this.treeData = tmpArray[0].children

      //     }
      //   }
      // })
    },

    setActive(uri){
      this.defaultActive = uri
    },

    selectTreeId (treeNodeId) {
      this.$refs.tree.setCurrentKey(treeNodeId)
    },

    selectNodeByUri (uri) {
      return this.allTreeNode[uri]
    },


  }
}
</script>
<style>
.asama-system-menu{
  /*font-size: 12px;*/
}
.asama-system-menu div{
  padding: 0px;
}

.asama-system-menu .el-submenu__title{
  height: 34px;
  line-height: 34px;
  margin-left: -8px;
}

.asama-system-menu .el-menu-item{
  /*font-size: 12px;*/
}

.asama-system-menu .el-menu-item-group .is-active {
    background-color: #ecf5ff;
}
</style>
