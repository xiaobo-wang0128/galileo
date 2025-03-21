<template>

  <div>

    <el-dialog :title="operateType + '应用配置'" :visible.sync="dialogVisible" :close-on-click-modal="false"
               class="windowForm" width="800px" @closed="afterDialogClose">


      <el-form   label-width="68px" >

        <el-form-item label="语种选择">
          <ex-select-auto
            v-model="globalLocales"
            :data="$allLanOptions"
            optionKey="value"
            optionLabel="label"
            optionValue="value"
            multiple
            placeholder="请选择语种">
          </ex-select-auto>
        </el-form-item>

        <el-form-item label="应用管理">
          <ex-table-auto class="el-table-nowwarp" ref="gridPanelEl" :data="appOptions" border>
            <el-table-column prop="appCode" label="应用code" min-width="6">
              <template slot-scope="scope">
                <el-input v-model="scope.row.appCode"></el-input>
              </template>
            </el-table-column>
            <el-table-column prop="appName" label="应用名称" min-width="6">
              <template slot-scope="scope">
                <el-input v-model="scope.row.appName"></el-input>
              </template>
            </el-table-column>

            <el-table-column prop="appName" label="资源文件类型" min-width="6">
              <template slot-scope="scope">
                <ex-select-auto
                  v-model="scope.row.exportType"
                  :data="exportTypeOptions"
                  optionKey="value"
                  optionLabel="label"
                  optionValue="value"
                  placeholder="请选择语种">
                </ex-select-auto>
              </template>
            </el-table-column>



            <el-table-column prop="appName" label="" width="40px" align="center">
              <template slot-scope="scope">
                <el-button icon="el-icon-close" type="text" @click="appOptions.splice(scope.$index, 1)"></el-button>
              </template>
            </el-table-column>

          </ex-table-auto>

        </el-form-item>

      </el-form>

      <!-- grid panel  -->


      <div slot="footer" class="dialog-footer">
        <el-button icon="el-icon-plus" @click="appOptions.push({'locales': []})" style="margin-right: 10px;">新增应用</el-button>

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
        globalLocales:[],
        appOptions:[],

        dialogVisible: false,
        saveButtonDisabled: false,
        operateType: '',
        formValue: {},
        searchValue: {},
        rules: {
        },

        exportTypeOptions:[{
          'label': '前端js',
          'value': 'js'
        },{
          'label': 'java',
          'value': 'java'
        },{
          'label': 'android',
          'value': 'android'
        }]

      };
    },

    created() {
      var _this = this;

      _this.$ajax({
        url: '/i18n/i18n_server/AppRpc/selectAll.json',
        method: 'post',
        data: _this.formValue,
        success: function (responseData) {
          if(responseData && responseData.data && responseData.data.rows){
            _this.appOptions = responseData.data.rows

            _this.globalLocales = _this.appOptions[0].locales
          }
        }
      });
    },

    watch:{

      'globalLocales'(val, old){
        this.appOptions.forEach(e=> e.locales = val)
      }

    },

    destroyed() {
    },

    methods: {

      showAppEditDialog(){
        this.dialogVisible = true
      },

      submitForm() {

        var _this = this;
        _this.saveButtonDisabled = true;

        let tmpArr = []
        _this.$allLanOptions.forEach(e=>{
          if(_this.globalLocales.contains(e.value)){
            tmpArr.push(e.value)
          }
        })
        _this.appOptions.forEach(e=> e.locales = tmpArr)

        _this.$ajax({
          url: '/i18n/i18n_server/AppRpc/saveUpdate.json',
          method: 'post',
          type: 'json',
          data: _this.appOptions,
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
      },
      //列表操作按钮
      handleClick(scope, type) {
        var _this = this;
        // 修改
        if (type == 'edit') {
          _this.operateType = '修改'
          _this.$ajax({
            url: '/i18n/i18n_server/AppRpc/detail.json',
            method: 'post',
            data: {
              securityId: scope.securityId
            },
            success: function (responseData) {
              var obj = responseData.data;
              _this.formValue = obj;
              _this.dialogVisible = true;
            }
          });
        }
        // 删除
        else if (type == 'del') {
          this.$confirm('确定删除该应用?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            this.$ajax({
              url: '/i18n/i18n_server/AppRpc/remove.json',
              method: 'post',
              data: {
                securityId: scope.securityId
              },
              success: function () {
                _this.$message({
                  showClose: true,
                  message: '删除成功',
                  type: 'success'
                });
                _this.$refs['gridPanelEl'].loadData();
              }
            });

          }).catch(() => {
          });
        }

      }
    }
  }

</script>
<style>
</style>
