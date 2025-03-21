<template>
  <el-container style="overflow: hidden; height: 100%; ">

    <el-header
      style="text-align: right; font-size: 12px; overflow: hidden; background-color: #1a3652;  line-height: 40px; height: 40px; ">
      <table style="width: 100%; height: 100%;" cellpadding="0" cellspacing="0">
        <tr>
          <td style="text-align:left; width: 300px;">
            <div style="font-size: 18px; padding-left: 6px;  font-weight:bold; color: #eaeaea;">Galaxy - 接口文档 &nbsp;</div>
          </td>
          <td>
          </td>
          <td style="display: flex;">

            <div v-for="item, index in apiRoot" type="text" @click="e => clickVersion(index)"
              style="  font-size: 18px; margin: 0px 8px; cursor: pointer; "
              :class="item.name == apiGroupShow.name ? 'head-highlight-div' : 'head-default-div'">
              {{ item.name }} ({{ item.showCount }})
            </div>

            <!--            <el-button type="text" @click="openDownload()" style="color: #eaeaea; ">-->
            <!--            </el-button>-->

            <!--            <el-button type="text" @click="addCmpTab({'label': '接口对接配置', uri: 'api-gloabel-config'  })" style="color: #eaeaea; ">-->
            <!--            </el-button>-->

            <!--            <el-button type="text" @click="addCmpTab({'label': '接口日志', uri: 'api-log'  })" style="color: #eaeaea; ">-->
            <!--            </el-button>-->


            <font style="font-size: 14px; color: #eaeaea;">{{ apiVersion }}</font>
          </td>
          <td>
            <el-input v-model="filterPath" placeholder="搜索内容" />
          </td>
        </tr>
      </table>
    </el-header>

    <el-container class="appi-body-contaniner">

      <el-aside width="280px" class="api-main-container-menu" v-show="showMainMenuPanel">

        <div class="app-menu-spliter-div"
          style="height: 10px; width: 264px; position: absolute; top: 40px; z-index: 100;"></div>

        <nav-menu ref="navMenu" v-on:treeNodeClick="handleNodeClick" :menuData="apiGroupShow.show"
          :hasOpen="globalTableArrayNodes"></nav-menu>

      </el-aside>

      <el-main class="api-main-container">

        <div class="focus-panel-expand" @click="changeExpandStatus">
          <i :class="showMainMenuPanelIcon"></i>
        </div>

        <el-tabs v-model="selectedTabId" @tab-remove="removeTab" @tab-click="tabClick" @edit="tabAdd" type="card">

          <!-- v-bind:style="{ height: tabPanelContainerHeight+ 'px'}" -->
          <el-tab-pane style="overflow:auto;" label="Home" :name="homeTabId">
            <api-portal></api-portal>
          </el-tab-pane>

          <!--          {{JSON.stringify(apiCacheMap)}}-->

          <el-tab-pane v-bind:style="{ height: tabPanelContainerHeight + 'px' }"
            v-for="(item, index) in globalTableArrayNodes" :key="item.tabId" :label="item.title" :name="item.tabId"
            closable>
            <api-item v-if="item.type === 'api-doc'" :apiData="apiCacheMap[item.uri]" :apiHead="apiHead"></api-item>

            <!--            <api-config v-else-if="item.uri === 'api-gloabel-config'" ></api-config>-->

            <!--            <api-log v-else-if="item.uri === 'api-log'" :apiDataList="apiDataList"></api-log>-->

          </el-tab-pane>

        </el-tabs>

      </el-main>

    </el-container>

    <div class="tabCloseMenuDiv" :style="{
      left: tabCloseMenu.left,
      top: tabCloseMenu.top,
      display: tabCloseMenu.display
    }">
      <div @click="scrollIntoView">滚动到当前</div>
      <div @click="tabClose('current')">关闭当前</div>
      <div @click="tabClose('other')">关闭其他</div>
      <div @click="tabClose('all')">关闭所有</div>
    </div>

  </el-container>
</template>


<script>

import Vue from 'vue'

import NavMenu from './api_sub/api-menu.vue'

import ApiItem from './api_sub/api-item.vue'

import ApiPortal from './api_sub/api-portal.vue'

import ApiConfig from './api_sub/api-config.vue'

import ApiLog from './api_sub/api-log.vue'

import ApiJson from './api_sub/api.json'

export default {

  components: { ApiLog, ApiPortal, NavMenu, ApiItem, ApiConfig },

  data() {

    let portalUrl = 'portal';
    let portalTabId = 'tab--' + portalUrl.split('/').join('___') + '--{}'

    return {
      apiActive: 0,
      apiRoot: [],
      filterPath: "",
      apiGroupShow: {
        name: '',
        groups: [],
        show: [],
      },

      apiVersion: '',

      /* container */
      mainHeaderHeight: 40,
      mainContainerHeiht: window.innerHeight - 40,
      tabPanelContainerHeight: window.innerHeight - 94,

      /* main */
      selectedTabId: portalTabId,

      homeTabId: portalTabId,

      globalTableArrayNodes: [],

      tabCloseMenu: {
        top: '100px',
        left: '100px',
        display: 'none'
      },

      menuClickRemoveTabId: '',

      tabListStack: [],

      userBtnRights: [],

      showMainMenuPanel: true,

      showMainMenuPanelIcon: 'el-icon-arrow-right',

      // 接口请求前缀
      apiHead: '',

      // 所有接口的缓存
      apiDataList: [],

      // 菜单数据缓存
      menuDataList: [],

      // 接口 map 缓存
      apiCacheMap: {}

    }
  },

  created() {



    // 关闭的快捷键
    document.onkeydown = (event) => {
      var e = event || window.event || arguments.callee.caller.arguments[0]

      if (e.keyCode === 87 && e.altKey) {
        this.$closeCurrentApiPanel()
        return
      }

      if (e.keyCode === 87 && e.ctrlKey) {
        this.$closeCurrentApiPanel()
      }
    }

    // 右链菜单
    document.oncontextmenu = (e) => {

      if (e.target && e.target.id && e.target.id.indexOf('tab--') !== -1) {
        this.tabCloseMenu.top = e.clientY + 10 + 'px'
        this.tabCloseMenu.left = e.clientX + 10 + 'px'
        this.tabCloseMenu.display = ''

        this.menuClickRemoveTabId = e.target.id

        e.preventDefault()
      } else {
        this.tabCloseMenu.display = 'none'
      }
    }
    document.onclick = (e) => {
      if (e.button == 0) {
        this.tabCloseMenu.display = 'none'
      }
    }

    Vue.prototype.$apiHead = ''

    if (process.env.NODE_ENV == 'development') {
      this.apiRoot = ApiJson
    }
    else {
      this.apiRoot = _API_JSON_
    }

    //
    // console.log(process.env.NODE_ENV)
    this.apiRoot.forEach(apiGroup => {
      apiGroup.groups.forEach(group => {
        group.children.forEach(c => {
          this.apiCacheMap[c.apiUrl] = c
        })
      })
    })

    this.initIndexPage()

  },

  mounted() {

    window.onresize = () => {
      this.mainContainerHeiht = window.innerHeight - 40
      this.tabPanelContainerHeight = window.innerHeight - 90
    }

    var _this = this

    Vue.prototype.$addTabApiPanel = function (tn) {

      _this.handleNodeClick(tn)
    }

    Vue.prototype.$closeCurrentApiPanel = function () {
      if (_this.selectedTabId === _this.homeTabId) {
        return
      }
      _this.removeTab(_this.selectedTabId)
    }
    this.refreshApi()


  },
  watch: {

    'filterPath'(val, old) {
      this.refreshApi()
      // this.clickVersion(this.apiActive)
    }
  },
  methods: {

    addCmpTab(cmpObj) {
      cmpObj.type = 'cmp'
      this.handleNodeClick(cmpObj)
    },
    clickVersion(index) {
      // this.apiGroupShow = this.getCurrentApi(this.apiRoot[index])
      this.apiGroupShow = this.apiRoot[index]
      this.apiActive = index
    },
    initIndexPage() {

      let _this = this

      // 初始加载
      let currentPathname = window.location.pathname


      let param = {}
      let search = decodeURIComponent(window.location.search)

      // console.log(decodeURIComponent(search))
      // if()
      if (search) {

        if (search.indexOf('?') === 0) {
          search = search.substring(1)
        }

        let tmpsParams = search.split('&')

        let tmpArr = tmpsParams[0].split('=')

        if (tmpArr && tmpArr.length == 2) {

          let currentApi = tmpArr[1]



          let tmpApiObj = _this.apiCacheMap[currentApi]
          // console.log(tmpApiObj)
          if (tmpApiObj) {
            this.handleNodeClick({
              label: tmpApiObj.apiName,
              uri: currentApi,
              params: {}
            });
          }
        }
      }
      else {
        this.clickVersion(0)
      }

    },
    refreshApi() {
      this.apiRoot.forEach(root => {
        const groups = this.getCurrentApi(root).groups
        this.$set(root, 'show', groups)
        this.$set(root, 'showCount', _.reduce(groups, (sum, it) => it.children.length + sum, 0))

        // root.show = this.getCurrentApi(root)
      })
    },

    getCurrentApi(root) {
      const obj = _.cloneDeep(root)
      if (!_.isEmpty(this.filterPath)) {
        obj.groups = obj.groups.filter(it => {
          if (it.name.includes(this.filterPath)) return true
          if (it.children) {
            const checklist = it.children.filter(api => {
              if ((api.apiUrl && api.apiUrl.includes(this.filterPath)) || (api.apiName && api.apiName.includes(this.filterPath))) {
                this.$set(api, "check", true)
                return true
              } else {
                return false
              }
            })
            // it.children = it.children.filter(api => (api.apiUrl && api.apiUrl.includes(this.filterPath)) || (api.apiName && api.apiName.includes(this.filterPath)))
            return !_.isEmpty(checklist)
          }
          return true
        })
      }
      return obj
    },


    initPageView(openUrlCfg) {
      let _this = this
      _this.$ajax({
        url: $portal_context + '/document/AutoDocumentRpc/getAllDocs.json',
        success: function (res) {

          if (res && res.data && res.data.list) {

            _this.apiDataList = res.data.list

            _this.apiVersion = 'v' + res.data.version

            Vue.prototype.$apiHead = res.data.apiHead

            _this.menuDataList = _this.apiDataList.map(e => {

              return {
                "label": e.group,
                "iconCls": "el-icon-paperclip",
                "children": e.docList.filter(k => {
                  if (!openUrlCfg.hideUnOpen) {
                    return true
                  }
                  if (openUrlCfg.hideUnOpen && openUrlCfg.openUrls.indexOf(k.apiUrl) != -1) {
                    return true
                  }
                  return false

                }).map(f => {
                  return {
                    "label": f.apiName,
                    "uri": f.apiUrl,
                    "isDeprecated": f.isDeprecated
                  }
                })
              };
            })

            _this.apiCacheMap = {}

            _this.apiDataList.forEach(e => {

              e.docList.forEach(f => {
                _this.apiCacheMap[f.apiUrl] = f
              })

            })

            _this.initIndexPage()

          }

        }
      })

    },

    changeExpandStatus() {

      this.showMainMenuPanel = !this.showMainMenuPanel

      if (this.showMainMenuPanel) {
        this.showMainMenuPanelIcon = 'el-icon-arrow-right'
      } else {
        this.showMainMenuPanelIcon = 'el-icon-arrow-left'
      }

    },

    // 缓存路径堆栈
    pushLinkStack(tabId) {
      if (this.tabListStack.length > 0) {
        var delIndex = -1;
        for (var i = 0; i < this.tabListStack.length; i++) {
          if (this.tabListStack[i] === tabId) {
            delIndex = i;
            break;
          }
        }
        this.tabListStack.splice(i, 1);
      }

      this.tabListStack.push(tabId)

    },
    scrollIntoView() {
      const targetId = this.menuClickRemoveTabId.substring('tab-'.length)
      this.tabClickTabId(targetId)
      const item = _.find(this.globalTableArrayNodes, it => it.tabId == targetId)
      if (item) {
        this.$nextTick(() => {
          this.$refs['navMenu'].setScrollIntoView(item.uri)
        })
      }
    },
    tabClose(type) {
      let _this = this

      var targetId = _this.menuClickRemoveTabId.substring('tab-'.length)

      if (type == 'current') {
        _this.removeTab(targetId)
      } else if (type == 'other') {
        _this.selectedTabId = targetId

        let tabs = _this.globalTableArrayNodes

        for (var i = _this.globalTableArrayNodes.length - 1; i >= 0; i--) {
          if (_this.globalTableArrayNodes[i].tabId && _this.globalTableArrayNodes[i].tabId != targetId && _this.globalTableArrayNodes[i].tabId != _this.homeTabId) {
            _this.globalTableArrayNodes.splice(i, 1)
          }
        }

        this.tabListStack = []
      } else if (type == 'all') {
        _this.selectedTabId = _this.homeTabId

        let tabs = _this.globalTableArrayNodes

        for (var i = _this.globalTableArrayNodes.length - 1; i >= 0; i--) {

          if (_this.globalTableArrayNodes[i].tabId && _this.globalTableArrayNodes[i].tabId != _this.homeTabId) {

            _this.globalTableArrayNodes.splice(i, 1)

          }

        }
        this.tabListStack = []
      }
    },

    setTabSelected(tabId) {
      this.selectedTabId = tabId
    },


    handleNodeClick(treeNode) {

      //

      if (!treeNode.uri) {
        return
      }

      this.switchTabAndActive(treeNode.uri)

      if (!treeNode.params) {
        treeNode.params = {}
      }

      let tabId = 'tab--' + treeNode.uri.split('/').join('___') + '--' + JSON.stringify(treeNode.params)

      var label = treeNode.label
      var tabObj = {
        title: label,
        tabId: tabId,
        uri: treeNode.uri,
        type: treeNode.type ? treeNode.type : 'api-doc'
      }

      // 点击菜单时，如果对应tag已存在，则直接显示该 tab
      for (var i = 0; i < this.globalTableArrayNodes.length; i++) {
        var tmp = this.globalTableArrayNodes[i]
        if (tmp.tabId === tabId) {
          this.selectedTabId = tabId
          this.apiRouteReplace({
            path: treeNode.uri,
            query: treeNode.params
          })
          this.pushLinkStack(tabId)
          return
        }
      }

      this.globalTableArrayNodes.push(tabObj)
      this.selectedTabId = tabId
      this.apiRouteReplace({
        path: treeNode.uri,
        query: treeNode.params
      })

      this.pushLinkStack(tabId)

    },
    tabAdd(data) {

    },

    tabClickTabId(tabId) {
      if (!tabId) {
        return
      }

      // 还原 url
      let tmpIndex1 = tabId.indexOf('tab--')
      let tmpIndex2 = tabId.lastIndexOf('--')

      if (tmpIndex1 != -1 && tmpIndex2 != -1 && tmpIndex1 < tmpIndex2) {
        let uri = tabId.substring(tmpIndex1 + 5, tmpIndex2)

        uri = uri.split('___').join('/')

        let treeNodeId = tabId.substring(tmpIndex2 + 2)

        if (treeNodeId && treeNodeId != '') {
          let tmpIndex = treeNodeId.indexOf('{')

          if (tmpIndex != -1) {
            if (tmpIndex == 0) {
              let params = JSON.parse(treeNodeId)
              this.apiRouteReplace({
                path: uri,
                query: params
              })
            } else {
              treeNodeId = treeNodeId.substring(0, tmpIndex)

              this.$refs.navMenu.selectTreeId(treeNodeId)
              this.apiRouteReplace({
                path: uri
              })
            }
          }
        }
        this.switchTabAndActive(uri)

      }

      let tabIdNew = tabId
      if (tmpIndex1 != 0) {
        tabIdNew = tabId.substring(tmpIndex1, tabId.length)
      }

      this.selectedTabId = tabIdNew

      this.pushLinkStack(tabIdNew)
    },

    switchTabAndActive(uri) {
      const rootActiveIndex = this.apiRoot.findIndex(it => it.groups.some(g => g.children.some(c => c.apiUrl == uri)))
      if (rootActiveIndex >= 0) {
        this.clickVersion(rootActiveIndex)
        this.$nextTick(() => {
          this.$refs['navMenu'].setActive(uri)
        })
      } else {
        this.$nextTick(() => {
          this.$refs['navMenu'].setActive(null)
        })
      }
    },

    tabClick(tabPane) {

      var tabId = tabPane.$el.id

      this.tabClickTabId(tabId)

    },

    removeTab(tabId) {

      let tabs = this.globalTableArrayNodes

      var delIndex = -1;
      for (var i = tabs.length - 1; i >= 0; i--) {
        if (tabs[i].tabId == tabId && tabs[i].tabId != this.homeTabId) {
          delIndex = i
          break
        }
      }
      this.globalTableArrayNodes.splice(delIndex, 1)

      // 删除缓存堆栈中的记录， 并设计新的激活窗口
      if (this.tabListStack.length > 0) {
        for (var k = 0; k < this.tabListStack.length; k++) {
          if (this.tabListStack[k] == tabId) {
            delIndex = k
            break;
          }
        }

        var newSelectedTabId

        if (delIndex == this.tabListStack.length - 1) {
          newSelectedTabId = this.tabListStack[delIndex - 1]
        }

        this.tabListStack.splice(delIndex, 1)

        if (newSelectedTabId) {
          this.tabClickTabId(newSelectedTabId)
        }
      }
    },

    apiRouteReplace(pageObj) {
      let url = window.location.pathname
      this.$router.replace({
        path: url + '?m=' + pageObj.path
      })

    },

    handleCommand(command) {
      if (!command) {
        return
      }
    },

    openNewUrl(url) {

      window.open(url)
    },

    openDownload() {
      window.open($portal_context + '/document/AutoDocumentRpc/export.json')
    }



  }
}

</script>
<style>
.focus-panel-expand {
  position: absolute;
  top: 47%;
  left: 0px;
  width: 10px;
  padding: 10px 0;
  background-color: #151b25;
  cursor: pointer;
  -webkit-transition: right .3s;
  -o-transition: right .3s;
  -moz-transition: right .3s;
  transition: right .3s;
  color: #fff;
  -webkit-transform: rotate(180deg);
  -moz-transform: rotate(180deg);
  -o-transform: rotate(180deg);
  transform: rotate(180deg);
  z-index: 10;
}

.focus-panel-expand:before {
  border-top: 12px solid #151b25;
  border-left: 10px solid transparent;
  bottom: -16px;
  box-sizing: border-box;
  content: "";
  position: absolute;
  display: block;
  height: 16px;
  width: 10px;
  right: 0;
}

.focus-panel-expand:after {
  content: "";
  box-sizing: border-box;
  position: absolute;
  display: block;
  height: 16px;
  width: 10px;
  border-left: 10px solid transparent;
  border-bottom: 12px solid #151b25;
  top: -16px;
  right: 0;
}


/* api-main-container start */

.appi-body-contaniner {
  background-color: #fff;
  overflow: hidden;
}

.api-main-container {
  padding: 8px 0px 0px 0px;
  overflow: hidden;
}

.api-main-container .el-tabs__content {
  background-color: #f0f2f5;
  padding: 0px;
}

.api-main-container .el-tabs__header {
  padding: 0;
  position: relative;
  margin: 0px 0px 0px 0px;
  background-color: #fff;
}

.api-main-container .el-tab-pane {
  background-color: #fff;
  padding: 0px 0px 0px 0px;
}

/* menu */
.api-main-container-menu,
.app-menu-spliter-div,
.api-main-container-menu .el-menu,
.api-main-container-menu .el-menu .el-submenu__title {
  background-color: #efefef;
}

.api-main-container-menu .el-menu-item-group__title {
  padding: 0px;
}

.api-main-container-menu .api-system-menu {
  border-right: 1px solid #E4E7ED;
}


/* api item*/



.api-doc-wrapper {
  padding: 20px 20px 20px 20px;
  height: 100%;
  box-sizing: border-box;
}

.api-doc-wrapper div {
  box-sizing: border-box;
}

.api-doc-item .el-tabs__content {
  background-color: #ffffff;
  padding: 0px 0px 0px 10px;
  box-sizing: border-box;
}

.api-doc-item {
  box-sizing: border-box;
}

.api-doc-item .el-tabs__nav-scroll {
  padding-left: 0px;
}


.api-doc-left-wrapper {
  box-sizing: border-box;
  overflow: auto;
  padding: 0px 20px 20px 0px;
}

.api-doc-wrapper .api-doc-left-wrapper:first-child {
  font-size: 14px;
  line-height: 32px;
}

.comment_table {
  background-color: #ccc;
  width: 100%;
}

.comment_table td {
  padding: 5px 5px 5px 10px;
  background-color: #fff;
  font-size: 14px;
  line-height: 24px;
}

.comment_table .table_head td {
  background-color: #eaeaea;
  font-size: 14px;
  line-height: 24px;
}

.api-desc-div {
  border: 1px solid #e4ce85;
  margin: 10px 0px 0px 0px;
  font-size: 14px;
  background-color: #fff9e5;
  padding: 0px 14px 0px 14px;
}

.head-highlight-div {
  background-color: #2ed7e4;
  color: #0c2c63;
  padding: 0px 20px;

  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
  /*border-top-radius: 25px;*/
  /*background: #73AD21;*/
  /*padding: 20px;*/
  /*width: 200px;*/
  /*height: 150px;*/
}

.head-default-div {
  color: #eaeaea;
  padding: 0px 20px;
}
</style>
