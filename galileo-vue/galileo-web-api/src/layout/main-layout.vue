<template>
  <el-container style="overflow: hidden; height: 100vh; ">
    <el-header style="text-align: right; font-size: 12px; overflow: hidden; background-color: #0c2c63; color: #333; line-height: 40px; height: 40px; ">
      <table style="width: 100%; height: 100%;" cellpadding="0" cellspacing="0">
        <tr>
          <td style="text-align:left; ">
            <div style="font-size: 20px; padding-left: 6px;  font-weight:bold; color: #eaeaea;">配置中心 </div>
          </td>
          <td>
          </td>
          <td>
          </td>
        </tr>
      </table>
    </el-header>
    <el-container class="app-el-contaniner" >
      <el-aside width="200px" class="app-main-menu" v-show="showMainMenuPanel">
        <nav-menu ref="navMenu" v-on:treeNodeClick="handleNodeClick"></nav-menu>
      </el-aside>
      <el-main class="app-main">
        <div class="focus-panel-expand" @click="changeExpandStatus">
          <i :class="showMainMenuPanelIcon"></i>
        </div>

        <el-tabs v-model="selectedTabId" @tab-remove="removeTab" @tab-click="tabClick" @edit="tabAdd">
          <el-tab-pane style="overflow:auto;" 
            v-bind:style="{ height: tabPanelContainerHeight+ 'px'}" 
            v-for="(item, index) in globalTableArrayNodes" 
            :key="item.tabId" 
            :label="item.title" 
            :name="item.tabId" 
            closable
            >
            <page-cmp-register :uri="item.uri" ></page-cmp-register>
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </el-container>

    <div class="tabCloseMenuDiv" :style="{
                left: tabCloseMenu.left,
                top:  tabCloseMenu.top,
                display: tabCloseMenu.display
            }">
      <div @click="tabClose('current')">关闭当前</div>
      <div @click="tabClose('other')">关闭其他</div>
      <div @click="tabClose('all')">关闭所有</div>
    </div>

  </el-container>
</template>
<script>
  
import Vue from 'vue'

import NavMenu from './ex-menu.vue'

import PageCmpRegister from '../lib/page-cmp-register.js'

export default {

  components: { NavMenu, PageCmpRegister },

  data () {

    let portalUrl = '/page/operate/portal';
    let portalTabId = 'tab--' + portalUrl.split('/').join('___') + '--{}' 

    return {
      switchCompanyId: '',
      confirmSwitch: false,
      switchCompanyMsg: '',
      show: false,
      // 律师代理的企业列表
      lawyerCompanies: [],
      // 律师当前选择的企业
      currentCompanyId: '',
      currentCompanyName: '',
      showChangePwdDialog: false,

      pwdForm: {

      },
      // 配置数据表单
      globalConfig: {
        companyId: '',
        domain: '',
        indexLogo: '',
        title: '',
        indexTemplateId: '',
        theme: '',
        loginPageBanner: ''
      },

      /* container */
      mainHeaderHeight: 40,
      mainContainerHeiht: window.innerHeight - 40,
      tabPanelContainerHeight: window.innerHeight - 90,

      /* main */
      selectedTabId: portalTabId,

      homeTabId: portalTabId,

      // globalTableArrayNodes: [{
      //   title: '工作站',
      //   tabId: portalTabId,
      //   uri: portalUrl
      // }],

      globalTableArrayNodes:[],

      tabCloseMenu: {
        top: '100px',
        left: '100px',
        display: 'none'
      },

      menuClickRemoveTabId: '',

      tabListStack: [],

      userBtnRights: [],

      showMainMenuPanel: true,

      showMainMenuPanelIcon: 'el-icon-arrow-right'

    }
  },

  created () {
    var _this = this


     
    // 关闭的快捷键
    document.onkeydown = function (event) {
      var e = event || window.event || arguments.callee.caller.arguments[0]

      if (e.keyCode === 87 && e.altKey) {
        _this.$closeCurrentPanel()
        return
      }

      if (e.keyCode === 87 && e.ctrlKey) {
        _this.$closeCurrentPanel()
      }
    }

    // 右链菜单
    document.oncontextmenu = function (e) {

      if (e.target && e.target.id && e.target.id.indexOf('tab--') !== -1) {
        _this.tabCloseMenu.top = e.clientY + 10 + 'px'
        _this.tabCloseMenu.left = e.clientX + 10 + 'px'
        _this.tabCloseMenu.display = ''

        _this.menuClickRemoveTabId = e.target.id

        e.preventDefault()
      } else {
        _this.tabCloseMenu.display = 'none'
      }
    }
    document.onclick = function (e) {
      if (e.button == 0) {
        _this.tabCloseMenu.display = 'none'
      }
    }
  },

  mounted () {
    window.onresize = () => {
      this.mainContainerHeiht = window.innerHeight - 40
      this.tabPanelContainerHeight = window.innerHeight - 90

      // mainHeaderHeight: 40,
      // mainContainerHeiht: window.innerHeight - 40,
      // tabPanelContainerHeight: window.innerHeight - 90,      
    }

    var _this = this

    Vue.prototype.$addTabPanel = function (tn) {
      _this.handleNodeClick(tn)
    }

    Vue.prototype.$closeCurrentPanel = function () {
      if (_this.selectedTabId === _this.homeTabId) {
        return
      }
      _this.removeTab(_this.selectedTabId)
    }

    Vue.prototype.$checkUserBtnRight = function (btnCode) {
      // if(_this.loginUser.userType=='operator'){
      //     return true;
      // }

      if (_this.userBtnRights.contains(btnCode)) {
        return true
      }

      return false
    }


    // 初始加载
    let currentPathname = window.location.pathname

    if(currentPathname && currentPathname!='' && currentPathname!='/'){

      let param = {}
      let search = window.location.search

      if(search){
        if(search.indexOf('?')===0){
          search = search.substring(1)
        }

        let tmpsParams = search.split('&')

        tmpsParams.forEach(e=>{
          
          let tmpsArr = e.split('=')
          if(tmpsArr.length == 2){
            param[tmpsArr[0]] = tmpsArr[1]
          }
        })
      }

      let label = PageCmpRegister.getPageName(currentPathname) 

      if(label){

        this.handleNodeClick({
          label:  label,
          uri: currentPathname,
          params: param
        });

        this.$refs['navMenu'].setActive(currentPathname)

      }

    }  
  },

  methods: {
        
    changeExpandStatus(){

      this.showMainMenuPanel = !this.showMainMenuPanel

      if(this.showMainMenuPanel){
        this.showMainMenuPanelIcon = 'el-icon-arrow-right'
      }
      else{
        this.showMainMenuPanelIcon = 'el-icon-arrow-left'
      }
      
    },
    submitChagePwdFormData () {
      var _this = this

      if (_this.pwdForm.oldPwd == '') {
        _this.$message({
          type: 'error',
          message: '原始密码不能为空'
        })
        return
      }

      if (_this.pwdForm.newPwd == '') {
        _this.$message({
          type: 'error',
          message: '新密码不能为空'
        })
        return
      }

      if (_this.pwdForm.newPwd != _this.pwdForm.newPwdConfirm) {
        _this.$message({
          type: 'error',
          message: '两次密码不一致'
        })
        return
      }

      _this.$ajax({
        url: '/sys/LoginRpc/changePwd.json',
        method: 'post',
        data: _this.pwdForm,
        success: function (responseData) {
          _this.$message({
            type: 'success',
            message: '修改成功'
          })

          _this.pwdForm = {}
          _this.showChangePwdDialog = false
        }
      })
    },

    bindStore (key, value) {
      Vue.prototype.$store[key] = value
    },

    routeReplace (pageObj) {
      this.$router.replace(pageObj)
    },

    // 缓存路径堆栈
    pushLinkStack (tabId) {
      if (this.tabListStack.length > 0) {
        var delIndex = -1;
        for(var i=0; i< this.tabListStack.length; i++){
          if(this.tabListStack[i] === tabId){
            delIndex = i;
            break;
          }
        }
        this.tabListStack.splice(i, 1);
      }

      this.tabListStack.push(tabId)

    },

    tabClose (type) {
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

    setTabSelected (tabId) {
      this.selectedTabId = tabId
    },

   

    handleNodeClick (treeNode) {

      if (!treeNode.uri) {
        return
      }
      
      if(treeNode.openNew){
        window.open(treeNode.uri)
        return;
      }

      if (!treeNode.params) {
        treeNode.params = {}
      }

      let tabId = 'tab--' + treeNode.uri.split('/').join('___') + '--' + JSON.stringify(treeNode.params)

      var label = treeNode.label
      var tabObj = {
        title: label,
        tabId: tabId,
        uri: treeNode.uri
      }

      // 点击菜单时，如果对应tag已存在，则直接显示该tab
      for (var i = 0; i < this.globalTableArrayNodes.length; i++) {
        var tmp = this.globalTableArrayNodes[i]
        if (tmp.tabId === tabId) {
          this.selectedTabId = tabId
          this.routeReplace({
            path: treeNode.uri,
            query: treeNode.params
          })
          this.pushLinkStack(tabId)
          return
        }
      }

      this.globalTableArrayNodes.push(tabObj)
      this.selectedTabId = tabId
      this.routeReplace({
        path: treeNode.uri,
        query: treeNode.params
      })

      this.pushLinkStack(tabId)

    },
    tabAdd (data) {

    },

    tabClickTabId (tabId) {
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
              this.routeReplace({
                path: uri,
                query: params
              })
            } else {
              treeNodeId = treeNodeId.substring(0, tmpIndex)

              this.$refs.navMenu.selectTreeId(treeNodeId)
              this.routeReplace({
                path: uri
              })
            }
          }
        }

        this.$refs['navMenu'].setActive(uri)
      }

      let tabIdNew = tabId
      if (tmpIndex1 != 0) {
        tabIdNew = tabId.substring(tmpIndex1, tabId.length)
      }

      this.selectedTabId = tabIdNew

      this.pushLinkStack(tabIdNew)
    },
    tabClick (tabPane) {
      var tabId = tabPane.$el.id
      this.tabClickTabId(tabId)
    },
    removeTab (tabId) {

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
      if (this.tabListStack.length > 0){
        for(var k =0; k< this.tabListStack.length; k++){
          if(this.tabListStack[k] == tabId){
            delIndex = k
            break;
          }
        }

        var newSelectedTabId 

        if(delIndex == this.tabListStack.length - 1){
          newSelectedTabId = this.tabListStack[ delIndex-1 ]
        }

        this.tabListStack.splice(delIndex, 1)

        if(newSelectedTabId){
          this.tabClickTabId( newSelectedTabId )
        }
      }

    },

    handleCommand (command) {
      if (!command) {
        return
      }

      if (command == 'logout') {
        this.$confirm('确定要退出系统吗?', '提示', {

          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'

        }).then(() => {
          this.$ajax({
            url: '/sys/LoginRpc/logout.json',
            method: 'post',
            success: function (responseData) {
              window.location = '/login.htm'
            }
          })
        }).catch(() => {

        })
      } else if (command == 'resetpwd') {
        this.showChangePwdDialog = true
      }
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

</style>
