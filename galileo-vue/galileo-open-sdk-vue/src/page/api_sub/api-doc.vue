<template>
  <div class="api-doc-wrapper" style="" :id="randomDivid">

    <div style="font-size: 18px; font-weight: bold;  background-color: #ffffff; height: 42px; line-height: 42px; ">
      {{apiData.apiName}}
      <font v-if="apiData.isDeprecated">（已废弃）</font>
    </div>

    <div class="api-doc-body">

      <div v-if="apiData.apiDesc" class="api-desc-div api-doc-line">
        {{apiData.apiDesc}}
      </div>

      <div class="api-doc-line">
        <b>请求方式：</b> POST (HTTPS)
      </div>

      <div class="api-doc-line">
        <b>{{apiData.apiType=='RequestApi' ? '请求地址' : '响应地址'}}：</b> <a target="_blank">http://{server_url}{{apiData.apiUrl}}</a>
      </div>

      <div class="api-doc-line">
        <b>请求包体:</b>
        <pre
          class="language-css"
          v-html="inputMockPre"
        ></pre>
      </div>

      <div v-if="apiData.inputParams && apiData.inputParams.length > 0"  class="api-doc-line">

        <div style="padding-left: 0px;padding-top: 0px; margin-bottom: 6px;"><b>参数说明：</b></div>

        <table cellpadding="1" cellspacing="1" class="comment_table">
          <tr class="table_head">
            <td style="width: 210px">参数名</td>
            <td style="width: 100px">必填项</td>
            <td style="width: 100px">类型(长度)</td>
            <td>说明</td>
          </tr>
          <template v-for="(input, inputIndex) in apiData.inputParams">
            <tr :key="inputIndex">
              <td>
                {{input.name}}
              </td>
              <td style="width: 100px;">
                <font v-if="input.notNull">Y</font>
              </td>
              <td>
                {{ showParamType(input) }}
              </td>
              <td>
                {{input.description}}
              </td>
            </tr>

<!--             {{input.subParams}}-->
            <template v-if="input.subParams && input.subParams.length > 0"  v-for="(subP, subIndex) in input.subParams">

              <tr >
                <td style="padding-left: 28px;">{{subP.name}}</td>
                <td><font v-if="subP.notNull">Y</font></td>
                <td>{{ showParamType(subP) }}</td>
                <td>{{subP.description}}</td>
              </tr>

              <template v-if="subP.subParams && subP.subParams.length > 0"  v-for="(subP2, sub2Index) in subP.subParams" >
                <tr :key=" 'input_sub2_'  + subIndex + '_' + sub2Index ">
                  <td style="padding-left: 56px;">{{subP2.name}}</td>
                   <td><font v-if="subP2.notNull">Y</font></td>
                  <td>{{ showParamType(subP2) }}</td>
                  <td>{{subP2.description}}</td>
                </tr>


                <template v-if="subP2.subParams && subP2.subParams.length > 0" v-for="(subP3, sub3Index) in subP2.subParams">
                  <tr :key=" 'input_sub3_' + subIndex + '_' + sub2Index + '_' + sub3Index ">
                    <td style="padding-left: 84px;">{{subP3.name}}</td>
                     <td><font v-if="subP3.notNull">Y</font></td>
                    <td>{{ showParamType(subP3)}}</td>
                    <td>{{subP3.description}}</td>
                  </tr>


                  <template v-if="subP3.subParams && subP3.subParams.length > 0" v-for="(subP4, sub4Index) in subP3.subParams">
                    <tr :key=" 'input_sub4_' + subIndex + '_' + sub2Index + '_' + sub3Index + '_' + sub4Index">
                      <td style="padding-left: 112px;">{{subP4.name}}</td>
                       <td><font v-if="subP4.notNull">Y</font></td>
                      <td>{{ showParamType(subP4) }}</td>
                      <td>{{subP4.description}}</td>
                    </tr>
                  </template>

                </template>


              </template>

            </template>
          </template>
        </table>
      </div>



      <div style="padding-left: 0px;padding-top: 12px; margin-bottom: 6px;"  class="api-doc-line">
        <b>返回结果：</b>
        <pre
          class="language-css"
          v-html="outputMockPre"
        >
      </pre>
      </div>


      <div v-if="apiData.outputParams && apiData.outputParams.length > 0"  class="api-doc-line">
        <div style="padding-left: 0px;padding-top: 6px; margin-bottom: 6px;"><b>参数说明：</b></div>
        <table cellpadding="1" cellspacing="1" class="comment_table">
          <tr class="table_head">
            <td style="width: 210px">参数名</td>
            <td style="width: 100px">参数类型</td>
            <td>说明</td>
          </tr>
          <template v-for="(input, inputIndex) in apiData.outputParams">
            <tr>
              <td>{{input.name}}</td>
              <td>{{ showParamType(input) }}</td>
              <td>{{input.description}}</td>
            </tr>
            <template v-if="input.subParams && input.subParams.length > 0" v-for="(subP, subIndex) in  input.subParams">
              <tr>
                <td style="padding-left: 28px;">{{subP.name}}</td>
                <td>{{ showParamType(subP) }}</td>
                <td>{{subP.description}}</td>
              </tr>

              <template v-if="subP.subParams && subP.subParams.length > 0">
                <tr v-for="(subP2, subP2Index) in subP.subParams" :key="subP2Index">
                  <td style="padding-left: 56px;">{{subP2.name}}</td>
                  <td>{{ showParamType (subP2) }}</td>
                  <td>{{subP2.description}}</td>
                </tr>
              </template>
            </template>
          </template>
        </table>
      </div>



    </div>


  </div>
</template>

<script>

// 引入插件
import Prism from "prismjs"
// 引入插件对应主题样式
import 'prismjs/themes/prism.css'

// prism.css
// prism-coy.css
// prism-dark.css
// prism-funky.css
// prism-okaidia.css
// prism-solarizedlight.css
// prism-tomorrow.css
// prism-twilight.css

export default {

  data() {
    return {

      /* tree */
      defaultActive: '', //  menuGroup[0].children[0].path,
      defaultOpeneds: [],

      apiWindowHeight: 0,
      uriMap: {},

      randomDivid: 'id_' + Math.random(),

      outputMockPre:'',

      inputMockPre: '',

      apiTypeMap:{
        "boolean":"bool",
        "string":"varchar","byte":"int","double":"decimal","short":"int","integer":"int","float":"decimal","bigdecimal":"decimal","int":"int","long":"int",
        "array[object]": "array[object]",
        "object": "object"
      }

    }
  },

  mounted() {

  },

  created() {
    if(this.apiData.inputMock){
      this.inputMockPre = Prism.highlight(this.apiData.inputMock, Prism.languages.javascript, 'javascript');
    }

    if(this.apiData.outputMock){
      this.outputMockPre = Prism.highlight(this.apiData.outputMock, Prism.languages.javascript, 'javascript');
    }
  },

  computed: {

  },
  props: {
    'apiData': {
      type: Object,
      default: {}
    },

    'windowHeight':{
      type: Number,
      default: -1
    }
  },
  watch: {
    'windowHeight'(val, old){
      document.getElementById(this.randomDivid).style.height = (val - 130) + 'px'
    },

    'apiData'(val, old){

      if(this.apiData.inputMock){
        this.inputMockPre = Prism.highlight(this.apiData.inputMock, Prism.languages.javascript, 'javascript');
      }

      if(this.apiData.outputMock){
        this.outputMockPre = Prism.highlight(this.apiData.outputMock, Prism.languages.javascript, 'javascript');
      }
    }

  },
  methods: {
    showParamType(paramType){

      console.log(paramType)

      let str = paramType.type
      if(this.apiTypeMap[paramType.type]){
        str = this.apiTypeMap[paramType.type]
      }

      if(paramType.length && paramType.length >0){
        str += '(' + paramType.length +')'
      }

      return str

    }
  }
}

</script>
<style>

  :not(pre) > code[class*="language-"], pre[class*="language-"] {
    background: #fafafa;
  }

  .api-doc-left-wrapper {
    color: #0e131acc;
  }

  .api-doc-body .api-doc-line{
    padding: 8px 0px;
    color: #0e131acc;
    font-size: 14px;
  }


</style>
