<template>
  <!-- Form -->
  <div class="page-content" style="padding-top: 0px; height: 100%; box-sizing: border-box; ">

    <el-form :model="formValue" label-width="160px" label-align="right" style="height: 100%; overflow: auto;">

      <field-title title="对接方式配置" style="width: 100%; "></field-title>

      <!-- 客户 → HAIQ -->
      <el-form-item label="客户 → HAIQ " style="margin-top: 20px; ">
        <el-select v-model="formValue.customerToHaiq.callType" placeholder="" style="width: 260px;">
          <el-option v-for="(item, index) in apiType" :label="item.label" :value="item.value" :key="'index_'+index"></el-option>
        </el-select>
        <span style="color: #999; padding-left: 8px;">

          <i  v-if="formValue.customerToHaiq.callType" class="el-icon-info"></i>
          <font v-if="formValue.customerToHaiq.callType==='sdk'">客户系统将使用 sdk 调用 haiq 系统的接口</font>
          <font v-if="formValue.customerToHaiq.callType==='http'">客户系统将使用 http 形式‘自定义接口’对接 haiq， haqi 端需要完成接口的适配 </font>
          <font v-if="formValue.customerToHaiq.callType==='ftp'">haiq 将使用 ftp 轮询方式与客户系统交互</font>
          <font v-if="formValue.customerToHaiq.callType==='db'">haiq 将使用'数据库'轮询方式与客户系统交互</font>

          <font v-if="formValue.customerToHaiq.callType==='socket'">haiq 将使用 tcp 长连接方式与客户系统交互</font>

        </span>
      </el-form-item>
      <el-form-item label="Haiq 应用 ID" v-if="formValue.customerToHaiq.callType === 'sdk'">
        <el-input v-model="formValue.customerToHaiq.appId" placeholder="请输入 appId" style="width: 260px;"></el-input>
      </el-form-item>
      <el-form-item label="Haiq 应用密钥" v-if="formValue.customerToHaiq.callType === 'sdk'">
        <el-input v-model="formValue.customerToHaiq.appSecret" placeholder="请输入 appSecret" style="width: 260px;"></el-input>

      </el-form-item>
      <el-form-item label="Haiq 接口地址" v-if="formValue.customerToHaiq.callType === 'sdk'">
        <el-input v-model="formValue.customerToHaiq.appAddress" placeholder="请输入接口地址（相对地址）" style="width: 260px;"></el-input>
        <el-button @click="updateIntercetporConfig(formValue.customerToHaiq)">保存</el-button>
      </el-form-item>

      <el-form-item label="" v-if="formValue.customerToHaiq.callType && formValue.customerToHaiq.callType != 'sdk' ">
        <el-button v-if="formValue.customerToHaiq.callType === 'http'" icon="el-icon-setting" @click="customerToHaiqCodeShow()">拦截器层代码配置</el-button>
        <el-button v-else icon="el-icon-setting" @click="customerToHaiqCodeShow()">代理层代码配置</el-button>
        <span v-if="formValue.customerToHaiq.transferCodeStatus==='fail' " style="color: red; padding-left: 8px;">
          <i class="el-icon-info" ></i>
          <font >代码编译失败</font>
        </span>

      </el-form-item>
      <div style="height: 1px; width: 100%;  margin-bottom: 16px;   border-bottom: 1px dashed #dcdfe6; "></div>

      <!-- HAIQ → 客户 -->
      <el-form-item label="HAIQ → 客户" style="margin-top: 20px; ">
        <el-select v-model="formValue.haiqToCustomer.callType" placeholder="" style="width: 260px;">
          <el-option v-for="(item, index) in apiType" :label="item.label" :value="item.value" :key="'index_'+index"></el-option>
        </el-select>
        <span style="color: #999; padding-left: 8px;">
          <i class="el-icon-info" v-if="formValue.haiqToCustomer.callType"></i>
          <font v-if="formValue.haiqToCustomer.callType==='sdk'">客户系统将使用 sdk 接收 haiq 的接口请求</font>
          <font v-if="formValue.haiqToCustomer.callType==='http'">客户系统将使用‘自定义接口’接收 haiq 的调用，haqi 端需要完成接口的适配 </font>
          <font v-if="formValue.haiqToCustomer.callType==='ftp'">haiq 将使用写 ftp 文件的形式去调用客户系统</font>
          <font v-if="formValue.haiqToCustomer.callType==='db'">haiq 将使用回写数据库的形式去调用客户系统 </font>
          <font v-if="formValue.customerToHaiq.callType==='socket'">haiq 将使用 tcp 长连接方式与客户系统交互</font>
        </span>
      </el-form-item>
      <el-form-item label="客户应用 ID" v-if="formValue.haiqToCustomer.callType === 'sdk'">
        <el-input v-model="formValue.haiqToCustomer.appId" placeholder="请输入 appId" style="width: 260px;"></el-input>
      </el-form-item>
      <el-form-item label="客户应用密钥" v-if="formValue.haiqToCustomer.callType === 'sdk'">
        <el-input v-model="formValue.haiqToCustomer.appSecret" placeholder="请输入 appSecret" style="width: 260px;"></el-input>
      </el-form-item>
      <el-form-item label="客户接口地址" v-if="formValue.haiqToCustomer.callType === 'sdk'">
        <el-input v-model="formValue.haiqToCustomer.appAddress" placeholder="请输入 appAddress" style="width: 260px;"></el-input>

        <el-button @click="updateIntercetporConfig(formValue.haiqToCustomer)">保存</el-button>
      </el-form-item>


      <el-form-item label="" v-if="formValue.haiqToCustomer.callType && formValue.haiqToCustomer.callType!='sdk' ">
        <el-button v-if="formValue.haiqToCustomer.callType === 'http' "icon="el-icon-setting" @click="haiqToCustomerCodeShow()">拦截器层代码配置</el-button>
        <el-button v-else icon="el-icon-setting" @click="haiqToCustomerCodeShow()">代理层代码配置</el-button>

        <span v-if="formValue.haiqToCustomer.transferCodeStatus==='fail' " style="color: red; padding-left: 8px;">
          <i class="el-icon-info" ></i>
          <font >代码编译失败</font>
        </span>

      </el-form-item>



      <div style="height: 4px;"></div>
      <field-title title="接口清单配置（请勾选需要开启的接口）" style="width: 100%;"></field-title>


      <el-form-item label="菜单栏显示" >
        <el-checkbox  class="api-config-checkbox" v-model="globalConfigForm.hideUnOpen">隐藏菜单栏未开启的接口</el-checkbox>
      </el-form-item>

      <el-form-item label="客户 → HAIQ 异步接口" >

        <el-select v-model="globalConfigForm.customerToHaiq.asyncApis" placeholder="" style="width: 260px;" multiple>
          <el-option v-for="(item, index) in customerToHaiqOptions" :label="item.apiName" :value="item.apiUrl" :key="'index_'+index"></el-option>
        </el-select>

        <span style="color: #999; padding-left: 8px;">
          <i class="el-icon-info" ></i>
          <font>异步接口请求后会立即返回请求成功</font>
        </span>

      </el-form-item>

      <div style="display: flex; width: 700px;">
        <el-form-item label="客户 → HAIQ" style="flex: 1">
          <el-checkbox-group v-model="globalConfigForm.customerToHaiq.apiInterface">
            <el-checkbox class="api-config-checkbox" v-for="(item, index) in customerToHaiqOptions" :label="item.apiUrl" :key="index">{{item.apiName}}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="HAIQ → 客户" style="flex: 1">
          <el-checkbox-group v-model="globalConfigForm.haiqToCustomer.apiInterface">
            <el-checkbox class="api-config-checkbox" v-for="(item, index) in haiqToCustomerOptions" :label="item.apiUrl" :key="index">{{item.apiName}}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </div>

      <div style="height: 60px;"></div>
      <div class="float_form_panel_btn" style="padding-left: 140px;">
        <el-button type="primary" @click="submitForm">保存配置</el-button>
        <el-button @click="$closeCurrentApiPanel()">取 消</el-button>

        <el-checkbox v-model="chooseAll" style="width: 50px; margin-left: 20px;">全选</el-checkbox>

      </div>

    </el-form>




    <!-- dialog 1 -->
    <el-dialog title="[客户 → HAIQ] 代理层代码配置"
    :visible.sync="customerToHaiqTransferCodeVisible"
    width="900px"
    append-to-body
    fullscreen
    :close-on-press-escape="false"
    :close-on-click-modal="false"


    >
      <el-form style="padding: 0px 20px 0px 20px;">
        <div style="padding-bottom: 10px;">
          客户请求的前置处理（如：接口签名处理）（java代码）
        </div>
        <code-editor v-model="formValue.customerToHaiq.codeContent" height="500px"></code-editor>
        <div style="padding-bottom: 10px;">
          代码示例下载：
          <el-button type="text" size="medium">http 接口代码示例</el-button>
          <el-button type="text" size="medium">ftp 接口代码示例</el-button>
          <el-button type="text" size="medium">数据接口对接代码示例</el-button>
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="updateIntercetporConfig(formValue.customerToHaiq)" :disabled="formSaving" :icon=" formSaving ? 'el-icon-loading':'el-icon-finished' ">
        {{formSaving ? '保存并编译中':'确 定'}}
        </el-button>
      </div>
    </el-dialog>


    <!-- dialog 2 -->
    <el-dialog title="[HAIQ → 客户] 代理层代码配置"
    :visible.sync="haiqToCustomerTransferCodeVisible"
    width="900px"
    append-to-body
    fullscreen
    :close-on-press-escape="false"
    :close-on-click-modal="false">

      <el-form style="padding: 0px 20px 0px 20px;">
        <div style="padding-bottom: 10px;">
          Haiq回传客户系统的处理（如：接口签名、统一错误信息处理）（java代码）
        </div>
        <code-editor v-model="formValue.haiqToCustomer.codeContent" height="500px"></code-editor>
        <div style="padding-bottom: 10px;">
          代码示例下载：
          <el-button type="text" size="medium">http 接口代码示例</el-button>
          <el-button type="text" size="medium">ftp 接口代码示例</el-button>
          <el-button type="text" size="medium">数据接口对接代码示例</el-button>
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="updateIntercetporConfig(formValue.haiqToCustomer)" :disabled="formSaving" :icon=" formSaving ? 'el-icon-loading':'el-icon-finished' ">
          {{formSaving ? '保存并编译中':'确 定'}}
        </el-button>
      </div>
    </el-dialog>

  </div>
</template>
<script>
  import CodeEditor from '@/component/code-editor.vue'

export default {

  components:{ CodeEditor },

  data() {
    return {

      formSaving: false,

      customerToHaiqTransferCodeVisible: false,

      haiqToCustomerTransferCodeVisible: false,


      customerToHaiqTmpCode: '',

      haiqToCustomerTmpCode: '',



      formValue: {

        customerToHaiq:{

          apiType: 'customer_to_haiq',

          // 对接方式 sdk http ftp db
          callType: '',

          // sdk 对接模式 appid
          appId: '',

          // sdk 对接模式 appSecret
          appSecret: '',

          // sdk 调用地址
          appAddress: '',

          // http ftp db 模块转换层代码
          codeContent: ''

        },

        haiqToCustomer:{

          apiType: 'haiq_to_customer',

          // 对接方式 sdk http ftp db
          callType: '',

          // sdk 对接模式 appid
          appId: '',

          // sdk 对接模式 appSecret
          appSecret: '',

          // sdk 调用地址
          appAddress: '',

          // http ftp db 模块转换层代码
          codeContent: ''

        }

      },


      globalConfigForm: {

        hideUnOpen: false,

        customerToHaiq:{

          asyncApis: [],

          // 开启的接口
          apiInterface: []

        },

        haiqToCustomer:{

          // 开启的接口
          apiInterface: []
        }

      },


      customerToHaiqOptions: [],
      haiqToCustomerOptions: [],


      dialogFormVisible: true,

      apiType:[
        { label: 'Haiq-Sdk 对接', value: 'sdk'},
        { label: 'Http Restful 形式接口对接', value: 'http'},
        { label: 'Ftp 文件交换形式对接', value: 'ftp'},
        { label: '数据库方式对接', value: 'db'},
        { label: 'socket长连接方式', value: 'socket'},
      ],

      chooseAll: false



    }

  },
  getRoutes() {
    return this.routes
  },

  mounted() {

  },

  created() {
    this.initPageView()
  },

  props: {
  },

  watch: {
    'chooseAll'(val, old){

      this.globalConfigForm.customerToHaiq.apiInterface = []
      this.globalConfigForm.haiqToCustomer.apiInterface = []
      if(val){

        let tmp = this.customerToHaiqOptions.map(e=>e.apiUrl)

        this.globalConfigForm.customerToHaiq.apiInterface = tmp; //  this.globalConfigForm.customerToHaiq.apiInterface.concat(tmp)

        let tmp2 = this.haiqToCustomerOptions.map(e=>e.apiUrl)
        this.globalConfigForm.haiqToCustomer.apiInterface = tmp2; // this.globalConfigForm.haiqToCustomer.apiInterface.concat(tmp2)

      }

    }
  },

  methods: {

    customerToHaiqCodeShow(){
      this.customerToHaiqTmpCode = this.formValue.customerToHaiq.transferCode ? this.formValue.customerToHaiq.transferCode + '' : '';
      this.customerToHaiqTransferCodeVisible = true ;
    },
    customerToHaiqCodeConfirm(){

      let _this = this

      _this.customerToHaiqSaving = true

      _this.checkCompile(
        _this.customerToHaiqTmpCode,
        _this.formValue.customerToHaiq.callType,
        "customerToHaiq" ,
        function(){
          _this.formValue.customerToHaiq.transferCode = _this.customerToHaiqTmpCode + ''
          _this.customerToHaiqTransferCodeVisible = false ;
        },
        function(){
          _this.customerToHaiqSaving = false
        }
      )

    },

    haiqToCustomerCodeShow(){
      this.haiqToCustomerTmpCode = this.formValue.haiqToCustomer.transferCode ? this.formValue.haiqToCustomer.transferCode + '': '' ;
      this.haiqToCustomerTransferCodeVisible = true ;
    },
    haiqToCustomerCodeConfirm(){

      let _this = this
      _this.haiqToCustomerSaving = true

      _this.checkCompile(
        _this.haiqToCustomerTmpCode,
        _this.formValue.haiqToCustomer.callType,
        "haiqToCustomer" ,
        function(){
          _this.formValue.haiqToCustomer.transferCode = _this.haiqToCustomerTmpCode + ''
          _this.haiqToCustomerTransferCodeVisible = false ;
        },
        function(){
          _this.haiqToCustomerSaving = false
        }
      )
    },

    showDialog() {
      this.dialogFormVisible = true
    },

    checkCompile(javaContent, callType, apiType, callback, final){

      let _this = this

      _this.$ajax({
        url: $portal_context + '/document/AutoDocumentRpc/testCompile.json',
        data: {
          "javaContent": javaContent,
          "callType": callType,
          "apiType": apiType
        },
        success: function(res){
          if(callback){
            callback.call()
          }
        },
        afterAjax(){
          if(final){
            final.call()
          }
        }
      })

    },

    submitForm(){

      let _this = this

      _this.globalConfigForm.customerToHaiq.callType = _this.formValue.customerToHaiq.callType
      _this.globalConfigForm.haiqToCustomer.callType = _this.formValue.haiqToCustomer.callType

      _this.$ajax({
        url: $portal_context + '/document/AutoDocumentRpc/saveConfig.json',
        type: 'json',
        data: _this.globalConfigForm,
        success: function(res){
          _this.$message({
            type: 'success',
            message: '保存成功'
          })
        }
      })

    },

    initPageView() {

      let _this = this

      _this.$ajax({

        url: $portal_context + '/document/AutoDocumentRpc/getAllDocs.json',

        success: function(res) {

          if (res && res.data && res.data.list) {

            _this.apiDataList = res.data.list

            _this.apiDataList.forEach(e=>{

              if(e.docList && e.docList.length > 0){

                if(e.docList[0].type==='customer_to_haiq'){
                  _this.customerToHaiqOptions = _this.customerToHaiqOptions.concat(e.docList);
                }
                else if(e.docList[0].type==='haiq_to_customer'){
                  _this.haiqToCustomerOptions = _this.haiqToCustomerOptions.concat(e.docList);
                }
              }

            })

            // 加载已有配置
            _this.$ajax({
              url: $portal_context + '/document/AutoDocumentRpc/getConfig.json',
              success: function(res2){

                if(res2.data){
                  _this.globalConfigForm = res2.data
                }

              }

            })

          }

        }
      })

      // -----

      _this.$ajax({

        url: $portal_context + '/document/AutoDocumentRpc/getSdkInterceptorConfig.json',

        success: function(res) {

          if (res && res.data && res.data.rows) {

            res.data.rows.forEach(e=>{

              if(e.apiType === 'customer_to_haiq'){
                _this.formValue.customerToHaiq = e
              }
              else if(e.apiType === 'haiq_to_customer'){
                _this.formValue.haiqToCustomer = e
              }

            })

          }

        }

      })

      // ------

    },


    updateIntercetporConfig(obj){

      let _this = this

      _this.formSaving = true

      _this.$ajax({
        url: $portal_context + '/document/AutoDocumentRpc/saveUpdateSdkInterceptorConfig.json',
        type: 'json',
        data: obj,
        success: function(res) {

          if(obj.callType == 'http' || obj.callType == 'ftp' || obj.callType == 'db'){
            _this.$message({
              type: 'success',
              message: '保存并编译成功'
            })
          }
          else{
            _this.$message({
              type: 'success',
              message: '修改成功'
            })
          }

          if(obj.apiType === 'customer_to_haiq'){
            _this.customerToHaiqTransferCodeVisible = false
          }
          else{
            _this.haiqToCustomerTransferCodeVisible = false
          }

        },

        fail(res){

          _this.$message({
            type: 'error',
            dangerouslyUseHTMLString: true,
            message: res ? res.message: '出错了'
          })
        },

        afterAjax(){
          _this.formSaving = false
        }

      })

    }

  }



}

</script>
<style type="text/css">
.api-config-checkbox {
  width: 300px;
}

.api-config-title {

  font-size: 14px;
  font-weight: bold;
  line-height: 28px;
  /*color: #409EFF; */
  margin-bottom: 10px;
  border-bottom: 1px dashed rgb(220, 223, 230);
}

</style>
