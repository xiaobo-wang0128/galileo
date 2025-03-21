<template>

  <div>

    <el-dialog title="讯飞开放平台配置" :visible.sync="dialogVisible" :close-on-click-modal="false"
               class="windowForm" width="600px" @closed="afterDialogClose">

      <el-form   label-width="140px" >
        <el-form-item label="应用ID">
          <el-input v-model="formValue.appId"></el-input>
        </el-form-item>
        <el-form-item label="接口APISercet">
          <el-input v-model="formValue.appSecret"></el-input>
        </el-form-item>
        <el-form-item label="接口APIKey">
          <el-input v-model="formValue.apiKey"></el-input>
        </el-form-item>

        <a href="https://console.xfyun.cn/app/myapp" target="_blank" style="margin-left: 150px; margin-top: 20px;">请往讯飞开放平台</a>
      </el-form>


      <!-- grid panel  -->
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm" :disabled="saveButtonDisabled">确 定</el-button>
      </div>

    </el-dialog>

  </div>

</template>
<script>
  import Vue from 'vue'

  export default {
    data() {
      return {
        formValue:{

        },
        appInfo:{},
        dialogVisible: false,
        saveButtonDisabled: false,
        operateType: '',
        searchValue: {},
        rules: {
        }
      };
    },

    created() {
      var _this = this;

    },

    watch:{

      'globalLocales'(val, old){
        this.appOptions.forEach(e=> e.locales = val)
      }

    },

    destroyed() {
    },

    methods: {

      showAppEditDialog(formValue, appInfo){
        let _this = this

        _this.$ajax({
          url: '/i18n/i18n_server/ConfigRpc/checkXunFeiConfg.json',
          success: function (responseData) {
            if(responseData.data){
              _this.formValue = responseData.data
            }
            console.log(responseData)
            _this.dialogVisible = true
          }
        });

      },

      submitForm() {

        var _this = this;

        _this.saveButtonDisabled = true;

        _this.$ajax({
          url: '/i18n/i18n_server/ConfigRpc/updateXunFeiConfig.json',
          type: 'json',
          data: _this.formValue,
          success: function (responseData) {

            _this.dialogVisible = false
            _this.$message({
              showClose: true,
              message: '提交成功',
              type: 'success'
            });

          },
          afterAjax() {
            _this.saveButtonDisabled = false
          }
        });

      },

      // 在窗口关闭后恢复 保存按钮
      afterDialogClose() {
        var _this = this
        _this.saveButtonDisabled = false

      }

    }
  }

</script>
<style>
</style>
