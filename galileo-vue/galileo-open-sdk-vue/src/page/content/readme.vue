<template>

  <div>


    <pre class="readme-pre">
所有的接口需使用HTTPS协议、JSON数据格式、UTF8编码。接口说明格式如下：

请求方式：GET/POST（HTTPS）

请求地址：https://open-api.kyb.com/
<br>请求包体：
...
<br>参数说明：
...
<br>返回结果：
...
<br>&ZeroWidthSpace;&ZeroWidthSpace;&ZeroWidthSpace;&ZeroWidthSpace;&ZeroWidthSpace;&ZeroWidthSpace;&ZeroWidthSpace;参数说明：
...

1) 请求方式，标明接口调用的HTTP方法，区分HttpGet/HttpPost请求。所有的请求都为https协议。

2) 请求地址，每个接口都有自己的接口地址，接口相对路径参见接口文档。

3) 请求包体/参数说明，标明请求参数示例及说明，参数说明包括字段含义、取值范围，开发者在设计数据结构时，应参考该定义范围。

4) 返回结果/参数说明，标明返回参数示例及说明。特别留意，所有接口返回包里都有 code。开发者需根据 code 否为0判断是否调用成功（ code 意义请见全局错误码）,code 非0时会同时返回 errorMsg， 而 errorMsg 仅作参考，后续可能会有变动，因此不可作为是否调用成功的判据。
</pre>



  </div>

</template>

<script>


  export default {
    components: {
    },
    data() {
      return {

      }
    },
    props: {

      type: {
        type: String,
        default: ''
      }
    },

    watch:{

    },

    created(){

    }
  }
</script>

<style>

  .readme-pre{
    white-space: pre-wrap;
    word-wrap: break-word;
    font-size: 14px; color: #0e131acc;
    padding: 0px 10px;
  }

</style>

