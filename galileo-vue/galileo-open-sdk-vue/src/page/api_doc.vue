<template>

  <el-container style="overflow: hidden; height: 100%; width: 100%; ">

    <el-header style=" border-bottom: 1px solid #060f1a12; ">
      <api-header></api-header>
    </el-header>

    <el-container class="appi-body-contaniner"
                  style="width: 1200px; margin-left: 50%; left: -600px; position: relative; ">

      <!-- 接口-->

      <el-aside width="280px" class="api-main-container-menu" >

        <!-- <div>开发指南</div>-->
        <nav-menu ref="navMenuDoc" v-on:treeNodeClick="handleNodeClick" :menuData="docMenuList"></nav-menu>

        <template v-for="apiGroup,groupIndex in apiRoot">
          <div style="padding: 10px 10px 0px 10px; border-top: 1px solid #eaeaea; width: 88%; color: #666;">{{apiGroup.name}} - 接口清单  </div>
          <nav-menu :ref="'navMenu_' + groupIndex" v-on:treeNodeClick="handleNodeClick" :menuData="apiGroup.groups" :menuIndex="groupIndex"></nav-menu>
        </template>

      </el-aside>

      <el-main class="api-main-container">

        <doc-readme v-if="chooseDocUrl == '/api_doc/readme'" type="readme"></doc-readme>
        <doc-sign v-else-if="chooseDocUrl == '/api_doc/sign'" type="sign"></doc-sign>
        <api-doc v-else :apiData="chooseApiData"></api-doc>

      </el-main>

    </el-container>

  </el-container>

</template>


<script>

  import Vue from 'vue'

  import NavMenu from './api_sub/api-menu.vue'

  import ApiItem from './api_sub/api-item.vue'

  import ApiPortal from './api_sub/api-portal.vue'

  import ApiConfig from './api_sub/api-config.vue'

  import ApiLog from './api_sub/api-log.vue'

  import ApiDoc from './api_sub/api-doc.vue'

  import ApiHeader from './api_sub/api-header.vue'

  import MarkdownView  from './content/readme.vue'


  import DocReadme  from './content/readme.vue'

  import DocSign  from './content/sign.vue'

  export default {

    components: {ApiLog, ApiPortal, NavMenu, ApiItem, ApiConfig, ApiDoc, ApiHeader, DocReadme, DocSign},

    data() {

      let portalUrl = 'portal';
      let portalTabId = 'tab--' + portalUrl.split('/').join('___') + '--{}'

      return {

        apiRoot: [],
        chooseApiData: {},

        apiVersion: '',

        showMainMenuPanelIcon: 'el-icon-arrow-right',

        docMenuList: [{
          "name": "开发指南",
          "children": [
            {
              "apiName": "开发前必读",
              "apiUrl": "/api_doc/readme"
            },
            {
              "apiName": "接口鉴权",
              "apiUrl": "/api_doc/sign"
            }]
        }],
        chooseDocUrl : '',

        // 接口 map 缓存
        apiCacheMap: {}

      }
    },

    created() {

      var _this = this

      _this.$ajax({
        url: $portal_context + '/open/AutoDocumentRpc/getAllDocs.json',
        success: function (res) {

          _this.apiRoot = res.data.list

          let groupIndex = 0
          _this.apiRoot.forEach(apiGroup => {
            apiGroup.groups.forEach(group => {
              group.children.forEach(c => {
                c.groupIndex = groupIndex
                _this.apiCacheMap[c.apiUrl] = c
              })
            })
            groupIndex ++
          })

          _this.initIndexPage()

        }
      })

      _this.docMenuMap = {}
      this.docMenuList.forEach(group=>{
        group.children.forEach(doc=>{
          _this.docMenuMap[doc.apiUrl] = true
        })
      })
    },

    mounted() {

    },

    methods: {

      addCmpTab(cmpObj) {
        cmpObj.type = 'cmp'
        this.handleNodeClick(cmpObj)
      },

      initIndexPage() {

        let _this = this

        // 初始加载
        let currentPathname = window.location.pathname

        if(currentPathname.indexOf('/api_doc')!=0){
          currentPathname = '/api_doc/readme'
          //return
        }

        _this.apiGroupShow = _this.apiRoot[0]

        if(_this.docMenuMap[currentPathname]){
          _this.chooseDocUrl =  currentPathname
          _this.chooseApiData = {}
          _this.$refs['navMenuDoc'].setActive(_this.chooseDocUrl)

          for(let i =0; i<_this.apiRoot.length; i++){
            // 'navMenu_' + gorupIndex
            // _this.$refs['navMenu_' + i].setActive('')
          }

          return
        }


        let exUri = currentPathname.substring('/api_doc'.length)
        if(exUri.length==0){
          return
        }

        let param = {}

        let currentApi = exUri

        let tmpApiObj = _this.apiCacheMap[currentApi]

        // console.log('tmpApiObj', tmpApiObj)

        if (tmpApiObj) {

          _this.handleNodeClick({
            label: tmpApiObj.apiName,
            uri: currentApi,
            params: {},
            menuIndex: tmpApiObj.groupIndex
          });

          // _this.$refs['navMenu'].setActive(currentApi)
          let currrentApiRoot = _this.apiRoot.filter(kk => currentApi.indexOf("/" + kk.name) != -1)

          if (currrentApiRoot && currrentApiRoot.length > 0) {
            _this.apiGroupShow = currrentApiRoot[0]
          }

          // console.log(tmpApiObj)

          window.setTimeout(() => {

            // console.log('tmpApiObj.groupIndex', tmpApiObj.groupIndex)

            // _this.$refs['navMenu'].setActive(currentApi)

            for(let i=0; i<_this.apiRoot.length; i++){
              if(tmpApiObj.groupIndex == i){
                if(_this.$refs[('navMenu_' + i)] ){
                  _this.$refs[('navMenu_' + i)][0].setActive(currentApi)
                }
              }
            }


          }, 50)

        }

      },


      filterMenu() {
        let _this = this
      },


      handleNodeClick(treeNode) {

        // console.log(treeNode)

        //
        let _this = this

        if (!treeNode.uri) {
          _this.chooseApiData = {}
          _this.chooseDocUrl = ''
          return
        }

        if(_this.docMenuMap && _this.docMenuMap[treeNode.uri]){
          _this.chooseDocUrl =  treeNode.uri
          _this.chooseApiData = {}
          // _this.$refs['navMenu'].setActive('')
          _this.$jump(treeNode.uri)

          for(let i=0; i<_this.apiRoot.length; i++){
            _this.$refs[('navMenu_' + i)][0].setActive('')
          }

          return
        }
        else{
          _this.chooseDocUrl = ''
          _this.chooseApiData = _this.apiCacheMap[treeNode.uri]
          _this.$refs['navMenuDoc'].setActive('')


          for(let i=0; i<_this.apiRoot.length; i++){
            if(treeNode.menuIndex != i){
              if(_this.$refs[('navMenu_' + i)] ){
                _this.$refs[('navMenu_' + i)][0].setActive('')
              }
            }
          }

        }

        _this.$jump('/api_doc' + treeNode.uri)

        // _this.apiRouteReplace({
        //   path: '/api_doc' + treeNode.uri,
        //   query: '' //treeNode.params
        // })

      },
      tabAdd(data) {

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
      },

      apiRouteReplace(pageObj) {
        let url = window.location.pathname
        this.$router.replace({
          path: url + '?m=' + pageObj.path
        })

      }


    }
  }

</script>
<style>

  .appi-body-contaniner {
    background-color: #fff;
    overflow: hidden;
  }

  /* menu */
  .api-main-container-menu {
    border-right: 1px solid #E4E7ED;
    /*padding: 10px 0px 10px 0px;*/
    margin-bottom: 10px;
    margin-top: 10px;
  }

  .api-main-container-menu, .app-menu-spliter-div, .api-main-container-menu .el-menu, .api-main-container-menu .el-menu .el-submenu__title {
    background-color: #fff;
  }

  .api-main-container-menu .el-menu-item-group__title {
    padding: 0px;
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
    background-color: #fcfbfb;
    font-size: 14px;
    line-height: 24px;
  }


  .api-doc-wrapper {
    display: flex;
    flex-direction: column;
  }

</style>
