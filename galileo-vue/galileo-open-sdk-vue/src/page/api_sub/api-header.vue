<template>
<!--  <el-header-->
<!--    style="text-align: right; font-size: 12px; overflow: hidden; background-color: #fff;  border-bottom: 1px solid #060f1a12; line-height: 62px; height: 62px; ">-->

    <div style="display: flex; justify-content: space-between; width: 1200px; margin-left: 50%; left: -600px; position: relative; ">


      <div style="
      display: flex;align-items: center;
      font-size: 22px; padding-left: 6px;  font-weight: normal; color: #0082EF; line-height: 62px;"><i class="el-icon-truck" style="font-size: 32px;  margin-right: 8px; "></i>  跨运宝 | 开放平台 &nbsp;</div>

      <div class="main-lyaout-top-menu">
        <div v-for="menu, index in headMenus" :id="'view-' + menu.id" :class="currentIndex == menu.id ?  'is-checked' : 'un-checked'"
          @click="switchPage(menu.uri)">
          {{menu.name}}
        </div>
      </div>
    </div>
    </div>
<!--  </el-header>-->

</template>
<script>
  export default {

    data() {
      return {
        currentIndex: '',
        headMenus:[{
          id: 'home',
          name: '首页',
          uri: '/'
        },{
          id: 'app_info',
          name: '应用管理',
          uri: '/app_info'
        }, {
          id: 'api_doc',
          name: '接口文档',
          uri: '/api_doc/readme'
        }, {
          id: 'api_log',
          name: '接口日志',
          uri: '/api_log'
        }]
      }
    },

    created() {

      let pathname = window.location.pathname

      if(pathname == '/' ){
        this.currentIndex = 'home'
      }
      if(pathname.indexOf('/app')!=-1){
        this.currentIndex = 'app_info'
      }
      else if( pathname.indexOf('/api_doc')!=-1){
        this.currentIndex = 'api_doc'
      }
      else if(pathname.indexOf('/api_log')!=-1){
        this.currentIndex = 'api_log'
      }
    },

    methods:{
      switchPage(uri){
        this.$jump(uri)
      }
    },

    computed: {
      allTreeNode() {
        return this.map
      }
    }
  }

</script>
<style>

  .main-lyaout-top-menu{
    display: flex;
  }

  .main-lyaout-top-menu div {
    height: 59px;
    line-height: 60px;
    outline: 0;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
    padding: 3px 20px 2px 20px;
    -webkit-box-sizing: border-box;
    box-sizing: border-box;
    display: inline-block;
    list-style: none;
    font-size: 16px;
    font-weight: 400;
    color: #606266;
    position: relative;
    cursor: pointer;
  }

  .main-lyaout-top-menu .is-checked {
    border-bottom: 2px solid #248df9;
  }

  .main-lyaout-top-menu .un-checked {
    border-bottom: 2px solid #fff;
  }


</style>
