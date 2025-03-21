<template>

  <div class="page-content">

    <header-cmp></header-cmp>

    <div style="padding: 0px 10px;">
      <!-- search panel -->
      <div style="display: flex; justify-content: space-between;">
        <div>
          <el-form :inline="true" :model="searchForm" label-suffix="" style="padding: 18px 0px 2px 14px;">
            <el-form-item label="应用">
              <ex-select-auto
                v-model="searchForm.appCode"
                :data="appOptions"
                optionKey="appCode"
                optionLabel="appName"
                optionValue="appCode"
                placeholder="请选择应用">
              </ex-select-auto>
            </el-form-item>

            <el-form-item label="语种">
              {{ $getLanCN(currentApp.locales) }} &nbsp;&nbsp;&nbsp;
            </el-form-item>

            <el-form-item label="关键字">
              <el-input v-model="searchForm.keyword" placeholder="请输入关键字" style="min-width: 200px;" clearable></el-input>
            </el-form-item>

            <el-form-item>
              <el-button @click="loadDictKeys()" icon="el-icon-search" type="primary">查询
              </el-button>
<!--              <el-button @click="searchForm.keyword = ''">清空</el-button>-->
              <el-button icon="el-icon-plus" @click="$refs['dictForm'].showAppEditDialog({'dictValues': {}}, currentApp)">新增词条</el-button>
            </el-form-item>
          </el-form>
        </div>
        <div>
          <el-form  label-suffix="" style="padding: 18px 14px 2px 0px;">

            <el-button style="margin-left: 8px;"  icon="el-icon-upload2" @click="$refs['importButton'].showEditDialog(currentApp)">导入excel</el-button>

            <el-dropdown style="margin-left: 8px;" @command="handleDownload">
              <el-button icon="el-icon-download">
                导出 excel<i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="all" style="line-height: 36px;">导出全部字条</el-dropdown-item>
                <el-dropdown-item command="un_finish"  style="line-height: 36px;">导出未完成字条</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>

            <el-button style="margin-left: 8px;" icon="el-icon-download" @click="exportResource()" :disabled="searchForm.appCode== '_all_'">导出资源文件</el-button>

            <el-button style="margin-left: 8px;" icon="el-icon-setting" @click="$refs['appEdit'].showAppEditDialog()">应用配置</el-button>

          </el-form>
        </div>
      </div>

      <!-- grid panel  -->
      <ex-table-auto
        ref="gridPanelEl"
        :url="'/i18n/i18n_server/DictRpc/query.json?appCode=' + currentApp.appCode"
        class="el-table-nowwarp"
        :autoFill="true"
        :autoPage="true"
        :autoLoad="false"
        :defaultPageSize="50"
        border>

        <el-table-column type="index" label="#" width="43"></el-table-column>

        <el-table-column label="应用" width="180">
          <template slot-scope="scope">
            {{appMap[scope.row.appCode].appName}}
          </template>
        </el-table-column>

        <el-table-column prop="dictKey" label="词条编码" min-width="12">
          <template slot-scope="scope">
            {{scope.row.dictKey}}
          </template>
        </el-table-column>

        <el-table-column v-for="(column, index) in columnData" :key="index" :label="column.label" min-width="10">
          <template slot-scope="scope">
            {{ scope.row.dictValues ? scope.row.dictValues[column.prop] : '' }}
          </template>
        </el-table-column>

        <el-table-column align="center" label="操作" width=120>
          <template slot-scope="scope">
            <el-button class="f14btn"  type="text"
                         @click="$refs['dictForm'].showAppEditDialog( JSON.parse(JSON.stringify(scope.row))  , currentApp)"
            >编辑</el-button>

            <ex-button-delete class="f14btn" deleteUrl="/i18n/i18n_server/DictRpc/remove.json" :rowId="scope.row.dictId + '' " @afterOperationDone="loadDictKeys()">删除
            </ex-button-delete>
          </template>
        </el-table-column>

      </ex-table-auto>

    </div>

    <app-edit ref="appEdit" @afterSave="loadAppInfos(); "></app-edit>

    <dict-form ref="dictForm" @afterSave="loadDictKeys(); "></dict-form>

    <import-button ref="importButton" @afterSave="loadDictKeys(); "></import-button>


  </div>

</template>
<script>
  import Vue from 'vue'

  import AppEdit from './sub/app_edit.vue'

  import DictForm from './sub/dict_form.vue'

  import ImportButton from './sub/import_button.vue'

  import HeaderCmp from '@/component/header.vue'

  export default {

    components: { HeaderCmp, AppEdit, DictForm, ImportButton },
    data() {
      return {

        appOptions:[],
        appMap: {},

        currentApp:{
          appCode: '_all_'
        },

        columnData:[],
        gridData:[],

        dialogVisible: false,
        saveButtonDisabled: false,
        operateType: '',
        formValue: {},
        searchForm: {
          'appCode': '',
          'keyword': ''
        },
        rules: {

        }
      };
    },

    created() {
      var _this = this;
      _this.loadAppInfos()
    },

    watch: {
      'searchForm.appCode'(val, old){
        let _this = this
        this.currentApp = this.appMap[val]

        let tmpArr = []; // [{'prop': 'dictKey', 'label': '词条编码', width: '10'}]
        _this.currentApp.locales.forEach(e=>{
          tmpArr.push({'prop': e, 'label': _this.$getLanCN(e), width: '10'})
        })
        _this.columnData = tmpArr

        // this.loadDictKeys()

      },
      'currentApp'(val, old){
        // this.loadDictKeys()
      },
      'gridData'(val,old){
      }

    },

    destroyed() {
    },

    methods: {


      keyValueChange(value){
        console.log(value)
      },

      exportResource(){
        let _this = this
        window.open('/i18n/i18n_server/I18nRpc/exportResource.json?appCode=' + _this.currentApp.appCode )
      },

      handleDownload(command, appCode){
        let _this = this
        window.open('/i18n/i18n_server/DictRpc/download_dict.json?appCode=' + '_all_' + '&type=' + command )
      },

      loadAppInfos(){
        var _this = this;

        // _this.$refs['gridPanelEl'].loadData()
        //return

        _this.$ajax({
          url: '/i18n/i18n_server/AppRpc/selectAll.json',
          method: 'post',
          data: _this.formValue,
          success: function (responseData) {
            if(responseData && responseData.data && responseData.data.rows){

              _this.appOptions = responseData.data.rows

              _this.appOptions.splice(0,0, {
                appCode: "_all_", appName: "全部应用",
                locales : responseData.data.rows[0].locales
              })

              _this.appMap = {}
              _this.appOptions.forEach(e=>{
                _this.appMap[e.appCode] = e
              })

              if(!_this.searchForm.appCode){
                _this.searchForm.appCode  = _this.appOptions[0].appCode
              }

              if(_this.searchForm.appCode){
                _this.currentApp = _this.appMap[_this.searchForm.appCode]
              }

              _this.loadDictKeys();

            }
          }
        });


      },

      loadDictKeys() {
        var _this = this;

        _this.$refs['gridPanelEl'].loadData(_this.searchForm)

        // return
        //
        // _this.$ajax({
        //   url: '/i18n/i18n_server/DictRpc/query.json',
        //   data: _this.searchForm,
        //   success: function (responseData) {
        //     if(responseData && responseData.data && responseData.data.rows){
        //
        //       let tmpArr = []; // [{'prop': 'dictKey', 'label': '词条编码', width: '10'}]
        //       _this.currentApp.locales.forEach(e=>{
        //         tmpArr.push({'prop': e, 'label': _this.$getLanCN(e), width: '10'})
        //       })
        //       _this.columnData = tmpArr
        //       _this.gridData = responseData.data.rows
        //     }
        //     else{
        //       _this.columnData = []
        //       _this.gridData = []
        //     }
        //   }
        // });

      },
      submitForm() {
        var _this = this;
        _this.saveButtonDisabled = true;

        _this['$refs']['formEl'].validate(function (valid) {
          if (valid) {
            _this.$ajax({
              url: '/apollo/app/DemandRpc/saveUpdate.json',
              method: 'post',
              data: _this.formValue,
              success: function (responseData) {
                _this.$refs['gridPanelEl'].loadData();
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
          } else {
            _this.saveButtonDisabled = false
            return false
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
            url: '/apollo/app/DemandRpc/detail.json',
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
          this.$confirm('确定删除该i18n?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            this.$ajax({
              url: '/apollo/app/DemandRpc/del.json',
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

  .box-h-split {
    display: flex;
  }
  .f14btn, .f14btn .el-button--small {
    font-size: 14px;
  }
</style>
