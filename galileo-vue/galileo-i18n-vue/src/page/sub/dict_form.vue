<template>

  <div>

    <el-dialog :title="'词条' + (dictVO.dictId>0?'编辑':'新增') " :visible.sync="dialogVisible" :close-on-click-modal="false"
               class="windowForm" width="800px" @closed="afterDialogClose">

      <el-form   label-width="68px" >
        <el-form-item label="词条编码">
          <el-input v-model="dictVO.dictKey"></el-input>
        </el-form-item>

        <el-form-item v-for="key in appInfo.locales" :label="$getLanCN(key)" :key="key">
          <el-input v-model="dictVO.dictValues[key]"></el-input>
        </el-form-item>
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
        dictVO:{},
        appInfo:{},

        dialogVisible: false,
        saveButtonDisabled: false,
        operateType: '',
        formValue: {},
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

      showAppEditDialog(dictVO, appInfo){
        this.dialogVisible = true
        this.dictVO = dictVO
        this.appInfo = appInfo
      },

      submitForm() {

        var _this = this;
        _this.saveButtonDisabled = true;

        _this.dictVO.appId = _this.appInfo.id
        _this.dictVO.appCode = _this.appInfo.appCode

        _this.$ajax({
          url: '/i18n/i18n_server/DictRpc/saveUpdate.json',
          method: 'post',
          type: 'json',
          data: _this.dictVO,
          success: function (responseData) {

            _this.dialogVisible = false
            _this.$message({
              showClose: true,
              message: '提交成功',
              type: 'success'
            });

            _this.$emit('afterSave')

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
