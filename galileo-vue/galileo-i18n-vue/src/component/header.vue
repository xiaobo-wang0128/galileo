<template>
  <div style="  background-color: #393622; display: flex; justify-content: space-between; ">

    <div style="
        display: flex;
        align-items: center;
        line-height: 46px; color: #fff;
        padding: 0px 18px; font-size: 24px ; cursor: pointer;  " @click="$router.push('/')" >
<!--      <img src="//kyb-bronze-public-oss.oss-cn-shenzhen.aliyuncs.com/default_template/kyb_logo.png" style="height: 32px; margin-right: 20px;"/>-->

      <span style="font-size: 30px; margin-left: 10px; color:#f5712f; ">OKIOS</span>
      <span style="font-size: 22px; margin-left: 10px;">多语言国际化配置中心</span>
    </div>

    <div style="font-size: 14px;  color: #fff; line-height: 46px; padding: 0px 18px;">

      <el-button size="medium" type="text" @click="$router.push('/')" style=" color: #ccc;">词条管理</el-button>

      <el-button size="medium" type="text" @click="$router.push('/util')" style="margin-right: 40px; color: #ccc;">翻译工具</el-button>

      <span v-if="loginUser.userId">{{ loginUser.name }}</span>
      <span v-else>
          您还没有登陆，
          <el-button @click="goToLogin" type="text" style="  font-size: 14px;">前往登陆页</el-button>
        </span>
    </div>
  </div>

</template>
<script>

  import Vue from 'vue'

  export default {

    components: {   },

    data() {
      return {
        loginUser: {
        }
      }
    },

    props:{
    },

    watch:{



    },

    created(){

      let _this = this

      Vue.prototype.$loginUser = {}
      _this.$ajax({
        url: '/i18n/i18n_server/LoginRpc/getLogin.json',
        success: function (responseData) {
          if(responseData && responseData.data){
            Vue.prototype.$loginUser = responseData.data
            _this.loginUser = responseData.data
          }
        }
      });
    },
    methods: {
      goToLogin(){
        window.location = this.$ex_default_login_url
      },
    }
  }

</script>
<style>

</style>
