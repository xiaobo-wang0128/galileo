<template>
  <div class="api-doc-left-wrapper" :style="{height: apiWindowHeight + 'px'}" :id="randomDivid">
    <div style="font-size: 16px; font-weight: bold;  position: sticky; top: 0px; z-index: 200; background-color: #ffffff; ">
      {{apiData.apiName}}
      <font v-if="apiData.isDeprecated">（已废弃）</font>
    </div>

<!--    <div style="margin-top: 10px; ">-->
<!--      调用方向： <font v-if="apiData.type=='customer_to_haiq'">客户 → HAIQ</font><font v-else>HAIQ → 客户</font>-->
<!--    </div>-->

    <div>
      接口地址： <a target="_blank">http://{server_url}{{apiData.apiUrl}}</a>
    </div>

    <div style="display: flex; ">

      <el-form ref="formEl" label-width="68px" class="float_form_panel_div" style="padding-top: 0px; padding-left: 0px; flex: 1; max-width: 1400px;">

<!--        {{apiData.inputIsJson}}-->

        <template v-if="apiData.inputParams.length > 0">

          <div style="line-height: 32px;">
            请求参数
          </div>

          <code-editor v-if="apiData.inputIsJson"  v-model="formValue.input" height="auto"></code-editor>

          <template v-else>

            <table style="width: 100%;" class="api-debug-table" cellpadding="0" cellspacing="1">
              <tr>
                <td style="width: 30%;">字段名</td>
                <td>值</td>
                <td style="width: 30%;">描述</td>
              </tr>

              <tr v-for="(input, inputIndex) in apiData.inputParams">
                <td >{{input.name}}</td>
                <td> <el-input v-model="submitValue[input.name]"></el-input> </td>
                <td>{{input.description}}</td>
              </tr>

            </table>

          </template>

        </template>


        <div style="line-height: 32px; margin-top: 8px;">
          响应结果
        </div>
        <code-editor v-model="formValue.output" height="auto"></code-editor>
        <div style="height: 40px;"></div>

      </el-form>


      <div style="width: 400px">


        <template v-if="apiData.inputIsJson && apiData.inputParams && apiData.inputParams.length > 0" >
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
          <div style="padding-left: 0px;  padding-bottom: 0px;">输出参数：</div>
          <table cellpadding="1" cellspacing="1" class="comment_table_inner" style="margin-top: 10px; ">
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
      <el-button type="primary" @click="doRequest()" size="mini" icon="el-icon-position">发 送 请 求</el-button>

      &nbsp;&nbsp;
      <el-button @click="resetInput()" size="mini">重 置</el-button>
          <!-- <el-button @click=" goback() " size="mini">返 回</el-button> -->
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

      submitValue:{

      },

      formValue: {
        input: '',
        output: '',
      },

      cacheKey: ''
    }
  },

  mounted() {
    document.getElementById(this.randomDivid).style.height = (this.windowHeight - 130) + 'px'
  },

  created() {
    let _this = this


    _this.cacheKey = "open_api_debug_" + _this.apiData.apiUrl
    var cacheInput = window.localStorage.getItem(_this.cacheKey);

    if(cacheInput){
      _this.formValue['input'] = cacheInput
    }
    else{
      _this.formValue['input'] = _this.apiData.inputMock + '\n\n\n'
    }

    _this.formValue['output'] = _this.apiData.outputMock + '\n\n\n'

    // window.localStorage.setItem("open_api_debug", {"a": "bb" });

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
    'formValue.input'(val, old){

      let _this = this

      if(_this.cacheKey){
        window.localStorage.setItem(_this.cacheKey, val);
      }

    },

    'windowHeight'(val, old) {
      document.getElementById(this.randomDivid).style.height = (val - 130) + 'px'
    },

    'apiData'(val, old) {

    }

  },
  methods: {

    resetInput(){
      let _this = this
      _this.formValue['input'] = _this.apiData.inputMock + '\n\n\n'
    },

    doRequest(){

      let _this = this

      let requestObj

      if(_this.formValue.input && _this.apiData.inputParams.length > 0 && _this.apiData.inputIsJson){

        try{
          requestObj = JSON.parse(_this.formValue.input)
        } catch(e) {
          _this.$message({
            type:'error',
            message: '请求数据格式不正确'
          })
          return
        }
      }
      else{
        requestObj = _this.submitValue
      }

      console.log('_this.apiData.inputIsJson: ' + _this.apiData.inputIsJson)

      _this.$ajax({
        url: _this.apiData.apiUrl, //$portal_context + '/document/AutoDocumentRpc/mockRequest.json',
        headers: {
          'X-Haiq-Api-Method': _this.apiData.apiUrl,
          'X-Haiq-App-Id': 'appId',
          'X-Haiq-Api-Sign': 'appSign'
        },
        type: _this.apiData.inputIsJson ? 'json' : 'form',
        data: requestObj,
        success: function(res){
          _this.$message({
            type:'success',
            message: '请求成功'
          })
          _this.formValue.output = JSON.stringify(res)
        },
        fail(res){

        },
        afterAjax(res){
          if(Object.prototype.toString.call(res) === '[object Object]'){
            _this.formValue.output = _this.formatJson(res) + '\n\n\n\n'
          }
          else{
            _this.formValue.output = res + '\n\n\n\n'
          }

        }
      })

    },

    formatJson(jsonObj) {
      //  console.log(jsonObj)
      //  console.log(callback)
      // 正则表达式匹配规则变量
      let reg = null;
      // 转换后的字符串变量
      let formatted = '';
      // 换行缩进位数
      let pad = 0;
      // 一个tab对应空格位数
      let PADDING = '\t';
      // json对象转换为字符串变量
      let jsonString = JSON.stringify(jsonObj);
      if (!jsonString) {
        return jsonString;
      }
      // 存储需要特殊处理的字符串段
      let _index = [];
      // 存储需要特殊处理的“再数组中的开始位置变量索引
      let _indexStart = null;
      // 存储需要特殊处理的“再数组中的结束位置变量索引
      let _indexEnd = null;
      // 将jsonString字符串内容通过\r\n符分割成数组
      let jsonArray = [];
      // 正则匹配到{,}符号则在两边添加回车换行
      jsonString = jsonString.replace(/([\{\}])/g, '\r\n$1\r\n');
      // 正则匹配到[,]符号则在两边添加回车换行
      jsonString = jsonString.replace(/([\[\]])/g, '\r\n$1\r\n');
      // 正则匹配到,符号则在两边添加回车换行
      jsonString = jsonString.replace(/(\,)/g, '$1\r\n');
      // 正则匹配到要超过一行的换行需要改为一行
      jsonString = jsonString.replace(/(\r\n\r\n)/g, '\r\n');
      // 正则匹配到单独处于一行的,符号时需要去掉换行，将,置于同行
      jsonString = jsonString.replace(/\r\n\,/g, ',');
      // 特殊处理双引号中的内容
      jsonArray = jsonString.split('\r\n');
      jsonArray.forEach(function(node, index) {
        // 获取当前字符串段中"的数量
        let num = node.match(/\"/g) ? node.match(/\"/g).length : 0;
        // 判断num是否为奇数来确定是否需要特殊处理
        if (num % 2 && !_indexStart) {
          _indexStart = index;
        }
        if (num % 2 && _indexStart && _indexStart != index) {
          _indexEnd = index;
        }
        // 将需要特殊处理的字符串段的其实位置和结束位置信息存入，并对应重置开始时和结束变量
        if (_indexStart && _indexEnd) {
          _index.push({
            start: _indexStart,
            end: _indexEnd,
          });
          _indexStart = null;
          _indexEnd = null;
        }
      });
      // 开始处理双引号中的内容，将多余的"去除
      _index.reverse().forEach(function(item, index) {
        let newArray = jsonArray.slice(item.start, item.end + 1);
        jsonArray.splice(item.start, item.end + 1 - item.start, newArray.join(''));
      });
      // 将处理后的数组通过\r\n连接符重组为字符串
      jsonString = jsonArray.join('\r\n');
      // 将匹配到:后为回车换行加大括号替换为冒号加大括号
      jsonString = jsonString.replace(/\:\r\n\{/g, ':{');
      // 将匹配到:后为回车换行加中括号替换为冒号加中括号
      jsonString = jsonString.replace(/\:\r\n\[/g, ':[');
      // 将上述转换后的字符串再次以\r\n分割成数组
      jsonArray = jsonString.split('\r\n');
      // 将转换完成的字符串根据PADDING值来组合成最终的形态
      jsonArray.forEach(function(item, index) {
        // console.log(item);
        let i = 0;
        // 表示缩进的位数，以tab作为计数单位
        let indent = 0;
        // 表示缩进的位数，以空格作为计数单位
        let padding = '';
        if (item.match(/\{$/) || item.match(/\[$/)) {
          // 匹配到以{和[结尾的时候indent加1
          indent += 1;
        } else if (
          item.match(/\}$/) ||
          item.match(/\]$/) ||
          item.match(/\},$/) ||
          item.match(/\],$/)
        ) {
          // 匹配到以}和]结尾的时候indent减1
          if (pad !== 0) {
            pad -= 1;
          }
        } else {
          indent = 0;
        }
        for (i = 0; i < pad; i++) {
          padding += PADDING;
        }
        formatted += padding + item + '\r\n';
        pad += indent;
      });
      // 返回的数据需要去除两边的空格和换行
      return formatted
        .trim()
        .replace(new RegExp('^\\' + '<br />' + '+|\\' + '<br />' + '+$', 'g'), '');
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

.api-debug-table{
  width: 100%;
  background-color: #e1e1e1;
}

.api-debug-table td{
  background-color: #fff;
  padding: 0 5px;

}

</style>
