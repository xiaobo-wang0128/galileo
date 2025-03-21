<template>
  <div class="api-doc-left-wrapper" :style="{height: apiWindowHeight + 'px'}" :id="randomDivid">
    <div style="font-size: 16px; font-weight: bold;  position: sticky; top: 0px; z-index: 200; background-color: #ffffff; ">
      {{apiData.apiName}}
      <font v-if="apiData.isDeprecated">（已废弃）</font>
    </div>

    <!-- <div v-if="apiData.apiDesc" class="api-desc-div">
      {{apiData.apiDesc}}
    </div> -->

    <div style="margin-top: 10px; ">
      调用方向： <font v-if="apiData.type=='customer_to_haiq'">客户 → HAIQ</font><font v-else>HAIQ → 客户</font>
    </div>

    <div v-if="apiData.type==='customer_to_haiq'">
      接口地址： <a target="_blank">http://{server_url}{{$apiHead}}/{{apiData.apiUrl}}</a>
    </div>

    <div v-if="apiData.type==='haiq_to_customer'"  style="display: flex; ">

      <div style="flex: 1; display: flex; padding-right: 24px; ">
        <div style="width: 72px;">
          <font v-if="apiData.outputType==='void'">回传地址：</font><font v-else>调用地址：</font>
        </div>
        <el-input
          v-model="formValue.httpUrl"
          style="flex: 1; font-size: 14px; font-family: Monaco,monospace,serif; ">
        </el-input>
      </div>

      <div style="width: 400px;">
      </div>

    </div>


    <div  style="display: flex; ">

      <el-form ref="formEl" label-width="68px" class="float_form_panel_div" style="padding-top: 0px; padding-left: 0px; flex: 1">

        <div style="line-height: 32px;">
          <font v-if="apiData.type=='customer_to_haiq'" >请求参数转换（请将客户的入参转换成 haiq-iwms 的标准入参）</font>
          <font v-else >请求参数转换（请将 haiq-iwms 的回传参数转换成客户要求的格式）</font>
        </div>
        <code-editor v-model="formValue.input" height="400px"></code-editor>

        <template v-if="apiData.outputType!=='void'">
          <div style="line-height: 32px; margin-top: 8px;">
            <font v-if="apiData.type=='customer_to_haiq'" >响应结果转换（请将 haiq-iwms 的响应结果转换成客户要求的格式）</font>
            <font v-else >响应结果转换（请将客户回传信息转换成 haiq-iwms 的标准格式）</font>
          </div>
          <code-editor v-model="formValue.output" height="300px"></code-editor>
        </template>

        <div style="height: 40px;"></div>

      </el-form>


      <div style="width: 400px;">
        <div style="line-height: 32px; height: 32px;">
        </div>

        <template v-if="apiData.inputParams && apiData.inputParams.length > 0" >
          <!-- <div style="padding-left: 0px;padding-top: 0px; padding-bottom: 4px;">输入参数（业务参数）：</div> -->
          <table cellpadding="1" cellspacing="1" class="comment_table_inner">

            <template v-for="(input, inputIndex) in apiData.inputParams">
              <tr :key="inputIndex">
                <td >
                  {{input.name}}
                </td>
                <td >
                  {{input.description}}
                </td>
              </tr>
              <!-- {{input.subParams}} -->
              <template v-if="input.subParams && input.subParams.length > 0">
                <tr v-for="(subP, subpIndex) in input.subParams">
                  <td >&nbsp;&nbsp;&nbsp;&nbsp;{{subP.name}}</td>
                  <td >{{subP.description}}</td>
                </tr>
              </template>
            </template>
          </table>
        </template>

        <template v-if="apiData.outputParams && apiData.outputParams.length > 0" >
          <div style="padding-left: 0px;padding-top: 6px; padding-bottom: 4px;">输出参数：</div>
          <table cellpadding="1" cellspacing="1" class="comment_table_inner">
            <template v-for="(input, inputIndex) in apiData.outputParams">
              <tr>
                <td >{{input.name}}</td>
                <td >{{input.description}}</td>
              </tr>
              <template v-if="input.subParams && input.subParams.length > 0" v-for="(subP, subpIndex) in  input.subParams">
                <tr>
                  <td style="padding-left: 45px;">{{subP.name}}</td>
                  <td >{{subP.description}}</td>
                </tr>
                <template v-if="subP.subParams && subP.subParams.length > 0">
                  <tr v-for="(subP2, subP2Index) in subP.subParams" :key="subP2Index">
                    <td style="padding-left: 90px;">{{subP2.name}}</td>
                    <td >{{subP2.description}}</td>
                  </tr>
                </template>
              </template>
            </template>
          </table>
        </template>

      </div>
    </div>

    <div class="float_form_panel_btn" style="padding-left: 68px; width: 100%;">

      <el-button type="primary" @click="doRequest()" size="mini" icon="el-icon-check">保 存 设 置</el-button>

    </div>

    <div style="height: 30px;"></div>
  </div>
</template>
<script>

import CodeEditor from '@/component/code-editor.vue'

export default {

  components:{ CodeEditor },

  data() {
    return {

      /* tree */


      defaultActive: '', //  menuGroup[0].children[0].path,
      defaultOpeneds: [],

      apiWindowHeight: 0,
      uriMap: {},

      randomDivid: 'id_' + Math.random(),

      formValue: {
        input: '\n\n\n',
        output: '\n\n\n',
        httpUrl: ''
      }
    }
  },

  mounted() {
    document.getElementById(this.randomDivid).style.height = (this.windowHeight - 130) + 'px'
  },

  created() {

    this.loadData()

  },

  computed: {

  },

  props: {
    'apiData': {
      type: Object,
      default: {}
    },

    'windowHeight': {
      type: Number,
      default: -1
    }
  },
  watch: {

    'windowHeight'(val, old) {
      document.getElementById(this.randomDivid).style.height = (val - 130) + 'px'
    },

    'apiData'(val, old) {

    }

  },
  methods: {

    loadData(){

      let _this = this

      _this.$ajax({
        url: $portal_context + '/document/AutoDocumentRpc/getApiTransferConfig.json',
        data: { "apiUrl": _this.apiData.apiUrl },
        success: function(res){

          if(res && res.data){
            let tmp = res.data

            for(let key in tmp){
              _this.formValue[key] = tmp[key]
            }
          }

          if( !_this.formValue.httpUrl || _this.formValue.httpUrl ===''){
            _this.formValue.httpUrl = 'http://{server_url}' + _this.$apiHead + '/' + _this.apiData.apiUrl;
          }

        }
      })

    },

    doRequest(){

      let _this = this

      _this.formValue.apiUrl = _this.apiData.apiUrl
      _this.formValue.apiName = _this.apiData.apiName
      _this.formValue.type = _this.apiData.type

      _this.$ajax({
        url: $portal_context + '/document/AutoDocumentRpc/updateApiTransferConfig.json',
        data: _this.formValue,
        type: 'json',
        success: function(res){

          _this.$message({
            type:'success',
            message: '请求成功'
          })

        }
      })

    }

  }

}

</script>

<style>

.comment_table_inner {
  background-color: #ccc;
  width: 100%;
  table-layout:fixed;
}

.comment_table_inner td {
  padding: 5px 5px 5px 10px;
  background-color: #fff;
  font-size: 14px;
  line-height: 24px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}

.comment_table_inner .table_head td {
  background-color: #eaeaea;
  font-size: 14px;
  line-height: 24px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}

</style>
