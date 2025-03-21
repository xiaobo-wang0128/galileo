<template>
  <el-container>

    <el-header style=" font-size: 30px; display: flex; justify-content: space-between; ">
      <div><span>国际化配置中心</span></div>
      <div>
        <el-button v-if="unfinishedDictionarys.length > 0" @click="queryUnfinishedDictionarys" type="text"
          style="color:red;text-align: center; font-size: 15px; background-color: yellow; padding: 8px 8px; ">
          {{
              unfinishedDictionarys.length
          }}条词条需要处理
        </el-button>
      </div>

    </el-header>

    <el-menu :default-active=defaultActive class="el-menu-demo" mode="horizontal" @select="handleSelect"
      background-color="#ffffff" text-color="#000000" active-text-color="#033cae">
      <el-menu-item v-for="(itme) in i18nApps" :index="itme.id + ''" :key="itme.id">{{ itme.appName }}</el-menu-item>
      <el-menu-item class="el-icon-plus" index="9999">添加应用</el-menu-item>
    </el-menu>
    <el-main>

      <div style="height: 120px ">
        <el-form :model="appForm" class="demo-form-inline" :inline="true">
          <el-form-item label="应用名称 :">
            <el-input v-model.trim="appForm.appName" :disabled="disabled" style="width: 100px" maxlength="50"
              show-word-limit clearable placeholder="请输入应用名称"></el-input>
            <el-form-item label="应用编码 :">
              <el-input v-model.trim="appForm.appCode" :disabled="disabled" style="width: 150px" maxlength="50"
                show-word-limit clearable placeholder="请输入应用编码"></el-input>
            </el-form-item>
          </el-form-item>

          <el-form-item label="国际化语言">
            <el-select v-model="appForm.locales" style="width: 220px" :disabled="disabled" filterable multiple
              collapse-tags placeholder="请选择语言">
              <el-option v-for="item in locales" :key="item.value" :label="item.label" :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>

          <el-button v-if=showUpdate type="primary" size="medium" @click="update">编辑应用</el-button>
          <el-button v-if=updateDisabled type="warning" @click="cancel" size="medium">取 消</el-button>
          <el-button v-if=updateDisabled type="success" @click="commit" size="medium" :loading=enableLoading>确 定
          </el-button>
          <el-button v-if=!updateDisabled type="primary" @click="showAddDictionary" size="medium">添加词条
          </el-button>
          <span v-if=!updateDisabled> 搜索词条：</span>
          <el-input v-if=!updateDisabled v-model.trim="searchValue.dictionaryKey" style="width: 150px"
            placeholder="输入词条编码" clearable></el-input>
          <el-button v-if=!updateDisabled type="primary" icon="el-icon-search" @click="getTableData()" size="medium">搜索
          </el-button>
          <el-button class="el-icon-upload2" type="primary" @click="showExport">导入</el-button>
          <a :href='exportUrl' target="_self">
            <el-button class="el-icon-download" type="primary" size="small">导出 excel</el-button>
          </a>
          <ui v-for="(v, k) in branchMap" :key="k">
            <el-form-item :label=k>
              <el-select v-model="branchSet[k]" style="width: 250px" filterable clearable multiple collapse-tags
                placeholder="请选择分支">
                <el-option v-for="item in v" :key=item :label=item :value=item>
                </el-option>
              </el-select>
            </el-form-item>
          </ui>
        </el-form>
      </div>
      <div v-if="defaultActive !== '9999'">
        <ex-table-auto ref="gridPanelEl" class="el-table-nowwarp" :url=queryUrl autoFill autoPage border
          :autoLoad="true">
          <el-table-column type="index" label="#" width="43">
            <template slot-scope="scope">
              {{
                  ($refs['gridPanelEl'].getLastRequestParam().pageIndex - 1) *
                  $refs['gridPanelEl'].getLastRequestParam().pageSize + scope.$index + 1
              }}
            </template>
          </el-table-column>
          <el-table-column label="词条编码" property="dictionaryKey" align="center"></el-table-column>
          <el-table-column show-overflow-tooltip v-for="item in getItem()" align="center" :prop="item.label"
            :label="item.label" :key="item.value">
            <template slot-scope="scope">
              <span>{{ scope.row.i18nValueMap[item.value] }}</span>
            </template>
          </el-table-column>
          <el-table-column label="编辑词条" align="center">
            <template slot-scope="scope">
              <el-button @click="showUpdateDictionary(scope.row)" type="primary" icon="el-icon-edit" size="medium">
              </el-button>
            </template>
          </el-table-column>
        </ex-table-auto>
      </div>
      <el-dialog :visible.sync="logInVisible">
        <el-form :model="loginForm" style="padding-top: 0px; padding-bottom: 0px;">
          <el-form-item label="输入密码">
            <el-input v-model="loginForm.password" placeholder="输入密码"></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="login()">确 定</el-button>
        </div>
      </el-dialog>


      <el-dialog :visible.sync="dialogFormVisible">
        <el-form :model="addDictionaryForm" style="padding-top: 0px; padding-bottom: 0px;">
          应用名称: <P>{{ appForm.appName }}</P>
          <el-form-item label="词条编码">
            <el-input v-model="addDictionaryForm.dictionaryKey" placeholder="请输入词条编码"></el-input>
          </el-form-item>
          <el-form-item show-overflow-tooltip v-for="(item, index) in getItem()" align="center" :prop="item.value"
            :label="item.label" :key="index">
            <el-input v-model="i18nValueMap[item.value]" placeholder="请输入词条对应语言值"></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="cancelAddDictionary()">取 消</el-button>
          <el-button type="primary" @click="createOrUpdateDictionary()" :disabled="saveButtonDisabled">确 定</el-button>
        </div>
      </el-dialog>


      <el-dialog title="导入词条" :visible.sync="exportVisible" width="400px">
        <i class="el-icon-download  el-icon--right"></i><a :href='exportTemplateUrl' target="_self">下载应用模版</a>
        <el-upload class="upload-demo" :drag=true :action="importUrl" :multiple=false :on-success="uploadSuccess">
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
          <div class="el-upload__tip" slot="tip">只能上传jpg/png文件，且不超过500kb</div>
        </el-upload>
      </el-dialog>


    </el-main>
  </el-container>
</template>
<script>

export default {
  data() {
    return {
      importUrl: '',
      exportUrl: '',
      exportZipUrl: '',
      exportTemplateUrl: '',
      exportVisible: false,
      tableTheadGroup: [],
      searchKey: '',
      queryUrl: '',
      disabled: true,
      showUpdate: true,
      updateDisabled: false,
      defaultActive: '0',
      enableLoading: false,
      tableData: [],
      dialogFormVisible: false,
      logInVisible: false,
      i18nValueMap: {},
      addDictionaryForm: {},
      loginForm: {},
      appForm: {},
      preAppForm: {},
      i18nApps: [],
      locales: [],
      searchValue: {},
      branchMap: {},
      branchSet: {},
      unfinishedDictionarys: [],
      saveButtonDisabled: false
    }
  },

  created() {
    var _this = this
    _this.$ajax({
      url: '/i18n/i18n_server/I18nAppRpc/getI18nAllLanguage.json',
      success(res) {
        _this.locales = res.data.rows
      }
    })
    _this.importUrl = '/i18n/i18n_server/I18nAppRpc/importAppDictionary.json'
    _this.getI18nAllApp()
  },

  mounted() {

  },
  methods: {
    login() {
      var _this = this
      _this.$ajax({
        url: '/i18n/i18n_server/I18nAppRpc/getWriteAbleCookie.json?password=' + _this.loginForm.password,
        success(res) {
          _this.logInVisible = false
          _this.$message({
            message: '操作成功',
            type: 'success'
          })
        }
      })
    },
    queryUnfinishedDictionarys() {
      var _this = this
      _this.searchValue.dictionaryKeyIds = _this.unfinishedDictionarys;
      _this.$refs['gridPanelEl'].loadData(_this.searchValue)
      _this.exportUrl = '/i18n/i18n_server/I18nAppRpc/exportAppDictionary.json?appId=' + _this.defaultActive + '&queryUnfinished=' + true
      _this.searchValue.dictionaryKeyIds = null;
    },
    getgetUnfinishedDictionary() {
      var _this = this
      _this.$ajax({
        url: '/i18n/i18n_server/I18nAppRpc/getUnfinishedDictionary.json',
        success(res) {
          if (res && res.code === 0 && res.data && res.data.rows && res.data.rows.length > 0) {
            _this.unfinishedDictionarys = res.data.rows
          }
        }
      })
    },
    getItem() {
      var _this = this
      var items = []
      if (_this.appForm.locales && _this.locales) {
        for (let i = 0; i < _this.appForm.locales.length; i++) {
          for (let j = 0; j < _this.locales.length; j++) {
            if (_this.locales[j].value === _this.appForm.locales[i]) {
              var obj = {}
              obj.label = _this.locales[j].label
              obj.value = _this.locales[j].value
              items.push(obj)
            }
          }
        }
      }
      return items;
    },
    uploadSuccess(response, file, fileList) {
      var _this = this
      _this.exportVisible = false
      if (response === '') {
        _this.$message({
          message: '操作成功',
          type: 'success'
        })
      } else {
        _this.$message({
          message: response.message,
          type: 'error'
        })
      }
      _this.getTableData()
    },
    showExport() {
      var _this = this
      _this.exportVisible = true
    }
    ,
    cancelAddDictionary() {
      var _this = this
      _this.addDictionaryForm = {}
      _this.dialogFormVisible = false
      _this.saveButtonDisabled = false
    },
    showUpdateDictionary(row) {
      var _this = this
      _this.addDictionaryForm.appId = row.appId
      _this.addDictionaryForm.id = row.id
      _this.i18nValueMap = row.i18nValueMap
      _this.addDictionaryForm.dictionaryKey = row.dictionaryKey
      _this.saveButtonDisabled = false
      _this.dialogFormVisible = true
    },
    showAddDictionary() {
      var _this = this
      _this.addDictionaryForm = {}
      _this.i18nValueMap = {}
      _this.saveButtonDisabled = false
      _this.dialogFormVisible = true
    },
    createOrUpdateDictionary() {
      var _this = this
      _this.saveButtonDisabled = true
      _this.addDictionaryForm.appId = _this.appForm.id
      _this.addDictionaryForm.i18nValueMap = _this.i18nValueMap
      _this.$ajax({
        url: '/i18n/i18n_server/I18nAppRpc/createOrUpdateDictionary.json',
        data: _this.addDictionaryForm,
        type: 'json',
        success(res) {
          _this.addDictionaryForm = {}
          _this.dialogFormVisible = false
          _this.getI18nAllApp()
          _this.$message({
            message: '操作成功',
            type: 'success'
          })
        }
      })
    },
    getI18nAllApp() {
      var _this = this
      _this.$ajax({
        url: '/i18n/i18n_server/I18nAppRpc/getI18nAllApp.json',
        success(res) {
          _this.i18nApps = res.data.rows
          if (_this.i18nApps && _this.i18nApps.length > 0) {
            if (_this.defaultActive === '0') {
              _this.appForm = _this.i18nApps[0]
              _this.defaultActive = _this.appForm.id + ''
            } else {
              for (let i = 0; i < _this.i18nApps.length; i++) {
                if (_this.defaultActive === _this.i18nApps[i].id + '') {
                  _this.appForm = _this.i18nApps[i]
                }
              }
            }
          }
          _this.getTableData()
        }
      })
    },
    getTableData() {
      var _this = this
      // 对页面用户选择的分支进行封装到查询参数中
      _this.convertBranchInfoToSearchValue()
      _this.exportUrl = '/i18n/i18n_server/I18nAppRpc/exportAppDictionary.json?appId=' + _this.defaultActive
      if (_this.searchValue.dictionaryKey) {
        _this.exportUrl = _this.exportUrl + '&dictionaryKey=' + _this.searchValue.dictionaryKey
      }
      if (_this.searchValue.branchSets) {
        _this.exportUrl = _this.exportUrl + '&branchSets=' + _this.searchValue.branchSets
      }
      _this.queryUrl = '/i18n/i18n_server/I18nAppRpc/pageI18nAllAppDictionary.json?appId=' + _this.defaultActive
      _this.exportTemplateUrl = '/i18n/i18n_server/I18nAppRpc/getImportTemplate.json?appId=' + _this.defaultActive
      _this.$refs['gridPanelEl'].loadData(_this.searchValue)
      _this.getgetUnfinishedDictionary()
      // 获取 分支下拉的数据
      _this.getAppBranchs()
    },
    convertBranchInfoToSearchValue() {
      var _this = this
      const map1 = new Map()
      Object.keys(_this.branchSet).forEach(k => {
        map1.set(k, _this.branchSet[k])
      })
      _this.searchValue.branchSets = JSON.stringify(map1)
    },
    getAppBranchs() {
      var _this = this
      _this.$ajax({
        url: '/i18n/i18n_server/I18nAppRpc/getAppBranchs.json?appId=' + _this.defaultActive,
        success(res) {
          _this.branchMap = res.data;
        }
      })
    },
    update() {
      var _this = this
      _this.updateDisabled = true
      _this.disabled = false
      _this.showUpdate = false
    },
    cancel() {
      var _this = this
      _this.updateDisabled = false
      _this.disabled = true
      _this.showUpdate = true
      if (_this.defaultActive === '9999') {
        _this.appForm = _this.preAppForm
        _this.defaultActive = _this.appForm.id + ''
      } else {
        _this.getI18nAllApp()
      }
    },
    validParam() {
      var _this = this
      var errorMsg = ''
      if (_this.appForm) {
        if (!_this.appForm.locales) {
          errorMsg = '应用语言不能为空'
        }
        if (!_this.appForm.appCode) {
          errorMsg = '应用编码不能为空'
        }
        if (!_this.appForm.appName) {
          errorMsg = '应用名称不能为空'
        }
      }
      return errorMsg
    },
    commit() {
      var _this = this
      var errorMsg = _this.validParam()
      if (errorMsg !== '') {
        _this.$message({
          message: errorMsg,
          type: 'error'
        })
        return
      }
      _this.enableLoading = true
      _this.$ajax({
        url: '/i18n/i18n_server/I18nAppRpc/createOrUpdateI18nApp.json',
        data: _this.appForm,
        type: 'json',
        success(res) {
          _this.defaultActive = res.data + ''
          _this.$message({
            message: '操作成功',
            type: 'success'
          })
          _this.enableLoading = false
        },
        afterAjax() {
          _this.enableLoading = false
          _this.updateDisabled = false
          _this.disabled = true
          _this.showUpdate = true
          _this.appForm = {}
          _this.getI18nAllApp()
        }
      })
    },
    handleSelect(key, keyPath) {
      var _this = this
      _this.defaultActive = key
      _this.searchValue = {}
      if (key === '9999') {
        _this.preAppForm = _this.appForm
        _this.appForm = {}
        _this.update()
        return
      }
      if (_this.i18nApps && _this.i18nApps.length > 0) {
        for (let i = 0; i < _this.i18nApps.length; i++) {
          if (_this.i18nApps[i].id + '' === key) {
            _this.appForm = _this.i18nApps[i]
          }
        }
      }
      _this.getTableData()
    }
  }
}

</script>
<style>
.el-header {
  background-color: #033cae;
  color: #ffffff;
  line-height: 60px;
}

.my-label {
  background: #E1F3D8;
}

.my-content {
  background: #FDE2E2;
}
</style>
