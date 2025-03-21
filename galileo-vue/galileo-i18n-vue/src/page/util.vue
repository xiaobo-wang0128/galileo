<template>

  <div class="page-content">

    <header-cmp></header-cmp>

    <div  style=" padding: 0px 20px;  margin-top: 20px; ">
      <el-select v-model="jobType" placeholder="请选择" style="width: 200px;">
        <el-option  label="中文 -> 英文" value="en"></el-option>
        <el-option  label="中文 -> 俄文" value="ru"></el-option>
      </el-select>

      <el-button style="margin-left: 20px;"
                 :icon="loading? 'el-icon-loading' : 'el-icon-s-promotion'"
                 @click="startTranslate">开始翻译</el-button>

      <el-button  style="position: absolute; right: 20px; " icon="el-icon-setting" @click="$refs['sysCfgEdit'].showAppEditDialog()">讯飞 api 配置</el-button>

    </div>

    <div style="padding: 20px 20px; display: flex; height: 100%; box-sizing: border-box; gap: 20px; ">

      <div style="flex: 1; height: 900px; " >
        <code-editor v-model="leftContext" ></code-editor>
      </div>

      <div style="flex: 1;">
        <code-editor v-model="rightContext"></code-editor>
      </div>

    </div>



    <system-cfg-edit ref="sysCfgEdit"></system-cfg-edit>



  </div>

</template>
<script>
  import Vue from 'vue'

  import HeaderCmp from '@/component/header.vue'


  import SystemCfgEdit from './sub/cfg_edit.vue'


  import CodeEditor from '@/component/code-editor.vue'

  export default {

    components: { HeaderCmp, CodeEditor , SystemCfgEdit },
    data() {
      return {

        "leftContext": "",

        "rightContext": "",

        "jobType": "ru",

        "loading": false
      };
    },

    created() {
      var _this = this;
    },

    watch: {
    },

    destroyed() {
    },

    methods: {

      startTranslate() {

        let _this = this

        if(_this.loading){
          return;
        }

        _this.loading = true

        let msg = this.$message({
          type: 'success',
          customClass: 'doing-cls-name',
          iconClass: 'el-icon-loading',
          message: '    正在翻译中，请稍后！',
          center: false,
          duration: 0
        });

        _this.$ajax({
          url: '/i18n/i18n_server/TranslateRpc/doTranslate.json',
          data: {
            input: _this.leftContext,
            jobType: _this.jobType
          },
          success: function (responseData) {

            _this.$message({
              type: 'success',
              message: '调用成功'
            })

            _this.rightContext = responseData.data
          },

          afterAjax(){
            _this.loading = false

            msg.close()
          }

        });
      },
    }
  }

</script>
<style>

  .box-h-split {
    display: flex;
  }
  .f14btn, .f14btn .el-button--small {
    font-size: 14px;
  }
  .doing-cls-name .el-message__content{
    padding-left: 10px;
  }
</style>
