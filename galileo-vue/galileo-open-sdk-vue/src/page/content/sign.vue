<template>

  <div style="padding: 0px 10px 10px 10px;">

    <div style="padding: 14px 14px 14px 4px;">
      <b>http 头固定参数</b>
    </div>

    <el-table :data="tableData" style="width: 100%" class="el-table-nowwarp">
      <el-table-column prop="field" label="http 请求头" min-width="200"></el-table-column>
      <el-table-column prop="desc" label="描述" min-width="200"></el-table-column>
    </el-table>

    <div style="padding: 14px 14px 6px 4px;  margin: 12px 6px 0px 0px;" class="api-doc-line">
      <b>签名示例：</b>
    </div>

    <pre
      style="font-size: 14px;"
      class="language-javascript"
      v-html="signExamplePre"
    ></pre>

    <br/>

    appId、appSecret 取自应用管理

  </div>

</template>

<script>
  // 引入插件
  import Prism from "prismjs"
  // 引入插件对应主题样式
  import 'prismjs/themes/prism.css'


  import Sign from './sign.js'

  export default {

    data() {
      return {

        tableData: [
          {"field": "x-app-id", "desc": "appId"},
          {"field": "x-app-sign", "desc": "接口签名值"},
          {"field": "x-request-time", "desc": "当前时间戳"}
        ],

        signExamplePre: ''
      }
    },

    created() {

      let jsContent = Sign

      this.signExamplePre = Prism.highlight(jsContent, Prism.languages.javascript, 'javascript');
    }
  }
</script>
