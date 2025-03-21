<template>

  <el-container style="overflow: hidden; height: 100%; width: 100%; ">

    <el-header style=" border-bottom: 1px solid #060f1a12; ">
      <api-header></api-header>
    </el-header>

    <el-container class="appi-body-contaniner" style="overflow-y: scroll; overflow-x: hidden;">

      <div style="width: 1200px; margin-left: 50%; left: -600px; position: relative; box-sizing: border-box;
         text-align: right; position: absolute; top: 60px; z-index: 100; background-color: #fff; height: 60px; line-height: 60px;  padding: 0px 20px 0px 20px; ">

        <submit-button icon="el-icon-check" type="primary"
                       @beforeSubmit="beforeSubmit"
                       :actionUrl="submitUrl"
                       :actionData="formValue"
                       actionTitle="确定要保存吗？" :simple="false" @submitDone="$jump('/app_info')"
        >保存配置
        </submit-button>
        <el-button @click="$jump('/app_info')" style="margin-left: 10px;">返 回</el-button>
      </div>

      <div
        style="width: 1200px; margin-left: 50%; left: -600px; position: relative; box-sizing: border-box; z-index: 1; ">

        <el-form ref="formEl" :rules="rules" :model="formValue"
                 label-width="140px"
                 style="width: 1200px; box-sizing: border-box; padding: 60px 20px 20px 20px; position: relative;">

          <field-title title="应用参数信息"></field-title>
          <el-row>
            <el-col style="width: 480px;">
              <el-form-item label="应用名称" prop="appName">
                <el-input v-model="formValue.appName"></el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col style="width: 480px;">
              <el-form-item label="appId">
                <!--                <el-input v-model="" readonly></el-input>-->
                <span style="padding-left: 0px; color: #606266">{{formValue.appId}}</span>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col style="width: 420px;">
              <el-form-item label="appSecret">
                <!--                <el-input v-model="formValue.appSecret"></el-input>-->
                <span style="padding-left: 0px; color: #606266">{{formValue.appSecret}}</span>

              </el-form-item>
            </el-col>
            <el-col style="width: 200px; padding-left: 8px;">
              <el-button type="text" @click="$copyToClip(formValue.appSecret, '密码已复制至剪切板')" style="margin-left: 20px;">
                复制
              </el-button>
              <el-button type="text" @click="generateRandomSecret" style="margin-left: 10px;">重新生成</el-button>
              <!--              <el-button type="text" @click="generateRandomSecret">重新生成</el-button>-->
            </el-col>
          </el-row>

          <el-row>
            <el-col style="width: 480px;">
              <el-form-item label="回传地址" prop="callbackUrl">
                <el-input v-model="formValue.callbackUrl"></el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col>
              <el-form-item label="">

                <el-button icon="el-icon-setting" @click="requestInterceptorVisible = true">代理层代码配置</el-button>

                <el-button icon="el-icon-setting" @click="responseInterceptorVisible = true">代理层代码配置</el-button>

              </el-form-item>
            </el-col>
          </el-row>

          <field-title title="应用接口配置"></field-title>
          <template v-for="model,modelIndex in apiRoot">
            <el-row>
              <el-col style="width: 800px;">
                <el-form-item :label="model.name" class="api-choose-label">

                  <table class="api-choose-table" cellspacing="1" cellpadding="1">

                    <template v-for="group,groupIndex in model.groups">
                      <template v-for="doc,docIndex in group.children">
                        <tr>
                          <td v-if="docIndex==0" :rowspan="group.children.length" class="choose-table-group-td">
                            {{group.name}}
                          </td>
                          <td class="choose-table-api-td">
                            <div>
                              {{doc.apiName}}
                              <span v-if="doc.apiType=='ResponseApi'" class="api-notify-tag">回传</span>
                            </div>
                            <div>
                              <el-switch
                                v-model="apiChoose[doc.apiUrl]"
                              >
                              </el-switch>
                            </div>
                          </td>
                        </tr>
                      </template>
                    </template>

                  </table>

                </el-form-item>
              </el-col>
            </el-row>
          </template>
        </el-form>

      </div>

    </el-container>


    <!-- dialog 1 -->
    <el-dialog title="接口请求拦截器配置"
               :visible.sync="requestInterceptorVisible"
               width="900px"
               append-to-body
               fullscreen
               :close-on-press-escape="false"
               :close-on-click-modal="false"

    >
      <el-form style="padding: 0px 20px 0px 20px;">
        <div style="padding-bottom: 10px;">
          客户请求的前置处理（如：接口签名处理、通一的参数校验... ）（java代码）
        </div>
        <code-editor v-model="codeForm.requestCodeContent" height="800px"></code-editor>
      </el-form>
      <div slot="footer" class="dialog-footer">

        <el-button type="primary" @click="updateIntercetporConfig(formValue.customerToHaiq)" :disabled="formSaving"
                   :icon=" formSaving ? 'el-icon-loading':'el-icon-finished' ">
          {{formSaving ? '保存并编译中':'确 定'}}
        </el-button>
        <el-button @click="requestInterceptorVisible = false">取 消</el-button>

      </div>
    </el-dialog>

    <!-- dialog 2 -->
    <el-dialog title="回传拦截器配置"
               :visible.sync="responseInterceptorVisible"
               width="900px"
               append-to-body
               fullscreen
               :close-on-press-escape="false"
               :close-on-click-modal="false">

      <el-form style="padding: 0px 20px 0px 20px;">
        <div style="padding-bottom: 10px;">
          Haiq回传客户系统的处理（如：接口签名、统一错误信息处理）（java代码）
        </div>
        <code-editor v-model="codeForm.responseCodeContent" height="800px"></code-editor>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="updateIntercetporConfig(formValue.haiqToCustomer)" :disabled="formSaving"
                   :icon=" formSaving ? 'el-icon-loading':'el-icon-finished' ">
          {{formSaving ? '保存并编译中':'确 定'}}
        </el-button>
        <el-button @click="responseInterceptorVisible = false">取 消</el-button>
      </div>
    </el-dialog>

  </el-container>

</template>

<script>

  import NavMenu from './api_sub/api-menu.vue'

  import ApiItem from './api_sub/api-item.vue'

  import ApiPortal from './api_sub/api-portal.vue'

  import ApiConfig from './api_sub/api-config.vue'

  import ApiLog from './api_sub/api-log.vue'

  import ApiDoc from './api_sub/api-doc.vue'

  import ApiHeader from './api_sub/api-header.vue'

  import CodeEditor from '@/component/code-editor.vue'

  export default {

    components: {ApiLog, ApiPortal, NavMenu, ApiItem, ApiConfig, ApiDoc, ApiHeader, CodeEditor},

    data() {
      return {

        formSaving: false,

        requestInterceptorVisible: false,
        responseInterceptorVisible: false,

        formValue: {},
        codeForm: {},

        apiRoot: [],
        apiChoose: {},

        rules: {
          appName: [
            {required: true, message: '请输入应用名称', trigger: 'blur'}
          ],
          callbackUrl: [
            {required: true, message: '请输入回传地址', trigger: 'callbackUrl'}
          ]
        }

      };
    },

    watch: {},

    created() {
      let _this = this

      _this.submitUrl = $portal_context + '/open/OpenApiConfigRpc/saveUpdateApp.json'

      let formId = this.$route.query.id

      _this.formId = formId

      if (!_this.formId) {
        _this.generateRandomId();
        _this.formValue.appSecret = ''
        _this.generateRandomSecret()
      }

      _this.loadAllApis()

    },


    mounted() {
      var _this = this;

    },

    destroyed() {
    },

    methods: {

      generateRandomId() {
        let _this = this
        _this.$ajax({
          url: $portal_context + '/open/OpenApiConfigRpc/getRandomId.json',
          success: function (res) {
            if (res && res.data) {
              _this.formValue.appId = res.data
            }
          }
        })
      },

      generateRandomSecret() {

        let tmps = '01234567890abcdefghijklmnopqrstuvwxyz=+-_*&^%$#@!'
        let len = 32
        let output = ''
        for (let i = 0; i < len; i++) {
          let index = parseInt(Math.random() * tmps.length)
          output += tmps.substring(index, index + 1)
        }

        this.formValue.appSecret = output

      },

      beforeSubmit() {
        let arr = []
        for (let key in this.apiChoose) {
          if (this.apiChoose[key]) {
            arr.push(key)
          }
        }
        this.formValue.apiUrls = arr
      },

      submitDone() {

      },

      loadAllApis() {

        let _this = this

        _this.$ajax({
          url: $portal_context + '/open/AutoDocumentRpc/getAllDocs.json',
          success: function (res) {
            _this.apiRoot = res.data.list

            //
            if (_this.formId) {
              _this.$ajax({
                url: $portal_context + '/open/OpenApiConfigRpc/getAppDetail.json?id=' + _this.formId,
                success: function (res) {
                  if (res && res.data) {
                    _this.formValue = res.data
                    if (_this.formValue.apiUrls) {
                      _this.formValue.apiUrls.forEach(tmpUri => {
                        _this.apiChoose[tmpUri] = true
                      })
                    }
                  }
                }
              })

            }
            //
          }
        })

      }

    }
  }

</script>
<style>
  .db_clear_list .el-checkbox {
    width: 600px;
    line-height: 30px;
  }

  .resource-div-block {
    display: flex;
    flex-flow: row wrap;
    justify-content: flex-start;
    align-items: center;
  }

  .resource-div-block div {
    padding: 14px 40px 14px 40px;
    margin-bottom: 14px;
    font-size: 28px;
    color: #409eff;
    background: #ecf5ff;
    border-color: #b3d8ff;
    margin-right: 20px;
    cursor: pointer;
    position: relative;
  }

  .api-choose-table {
    background-color: #ececec;
    width: 100%;
  }

  .api-choose-table td {
    background-color: #fff;
    padding: 0px 8px 0px 8px;
    line-height: 42px;
  }

  .api-choose-table .choose-table-group-td {
    vertical-align: top;
    width: 140px;
  }

  .api-choose-table .choose-table-api-td {
    display: flex;
    justify-content: space-between;
  }

  .api-notify-tag {
    background-color: #fff;
    color: #81b1ff;
    border: 1px solid #81b1ff;
    border-radius: 4px;
    padding: 0px 2px;
  }

  .api-choose-label .el-form-item__label {
    padding-top: 5px;
  }
</style>
