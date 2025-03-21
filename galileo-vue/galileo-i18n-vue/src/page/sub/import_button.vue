<template>

  <div>

    <el-dialog :title="operateType + '词条批量导入'" :visible.sync="dialogVisible" :close-on-click-modal="false"
               class="windowForm" width="500px" @closed="afterDialogClose" >
        <a :href="'/i18n/i18n_server/DictRpc/downloadSample.json?appCode=' +  appInfo.appCode" target="_blank">
        <el-button icon="el-icon-download" class="f14btn" type="text" style="margin-left:60px;">下载应用模版</el-button>
        </a>

        <el-upload :drag=true
                   :action="'/i18n/i18n_server/DictRpc/importExcel.json' "
                   :multiple=false
                   :on-success="uploadSuccess" style="padding-left: 60px;">
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
          <div class="el-upload__tip" slot="tip">只能上传 excel/xlsx 文件</div>
        </el-upload>
      <!-- grid panel  -->

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
      </div>

    </el-dialog>

  </div>

</template>
<script>
  import Vue from 'vue'

  export default {
    data() {
      return {
        dictVO: {},
        appInfo: {},

        importUrl: '',

        dialogVisible: false,
        saveButtonDisabled: false,
        operateType: '',
        formValue: {},
        searchValue: {},
        rules: {}
      };
    },

    created() {
      var _this = this;

    },

    watch: {

      'globalLocales'(val, old) {
        this.appOptions.forEach(e => e.locales = val)
      }

    },

    destroyed() {
    },

    methods: {

      showEditDialog( appInfo) {
        this.appInfo = appInfo
        this.dialogVisible = true

      },

      uploadSuccess(res) {

        console.log(res)


        let _this = this

        if(res.code == 401){
          window.location = this.$ex_default_login_url
        }
        else if ( res.code !=0){
          _this.$message({
            type: 'error',
            message: '导入失败: ' + res.message
          })
          return
        }


        _this.dialogVisible = false

        _this.$message({
          type: 'success',
          message: '批量导入成功'
        })

        _this.$emit('afterSave')

      },

      // 在窗口关闭后恢复 保存按钮
      afterDialogClose() {
        var _this = this
        _this.saveButtonDisabled = false

        // _this.$emit('afterSave')
      }

    }
  }

</script>
<style>
</style>
