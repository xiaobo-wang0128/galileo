<template>
  <div class="api-doc-left-wrapper" :style="{height: apiWindowHeight + 'px'}" :id="randomDivid">

    <div style="font-size: 16px; font-weight: bold;  position: sticky; top: 0px; z-index: 2; background-color: #ffffff; ">
      {{apiData.apiName}}
      <font v-if="apiData.isDeprecated">（已废弃）</font>
    </div>


    <div v-if="apiData.apiDesc" class="api-desc-div">
      {{apiData.apiDesc}}
    </div>

<!--    <div style="margin-top: 10px; ">-->
<!--      调用方向： <font v-if="apiData.type=='customer_to_haiq'">客户 → HAIQ</font><font v-else>HAIQ → 客户</font>-->
<!--    </div>-->

    <div>
      接口地址： <a target="_blank">http://{server_url}{{apiData.apiUrl}}</a>
    </div>

    <div>
      请求方式： {{apiData.inputIsJson? 'json': 'form'}}
    </div>

    <div>返回类型： {{apiData.outputType}} </div>

    <div v-if="apiData.isPage">是否分页：是</div>

    <div v-if="apiData.outputDesc">返回说明：{{apiData.outputDesc}}</div>

    <div v-if="apiData.inputParams && apiData.inputParams.length > 0">

      <!-- {{apiData.inputParams}} -->

      <div style="padding-left: 0px;padding-top: 0px; margin-bottom: 6px;">输入参数（业务参数）：</div>
      <table cellpadding="1" cellspacing="1" class="comment_table">
        <tr class="table_head">
          <td style="width: 210px">参数名</td>
          <td style="width: 100px">必填项</td>
          <td style="width: 100px">类型</td>
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
              {{input.type}}
            </td>
            <td>
              {{input.description}}
            </td>
          </tr>
          <!-- {{input.subParams}} -->
          <template v-if="input.subParams && input.subParams.length > 0"  v-for="(subP, subpIndex) in input.subParams">

            <tr >
              <td style="padding-left: 28px;">{{subP.name}}</td>
              <td><font v-if="subP.notNull">Y</font></td>
              <td>{{subP.type}}</td>
              <td>{{subP.description}}</td>
            </tr>

            <template v-if="subP.subParams && subP.subParams.length > 0"  v-for="(subP2, subP2Index) in subP.subParams" >
              <tr :key=" 'input_sub2_' + subP2Index">
                <td style="padding-left: 56px;">{{subP2.name}}</td>
                 <td><font v-if="subP2.notNull">Y</font></td>
                <td>{{subP2.type}}</td>
                <td>{{subP2.description}}</td>
              </tr>


              <template v-if="subP2.subParams && subP2.subParams.length > 0" v-for="(subP3, subP3Index) in subP2.subParams">
                <tr :key=" 'input_sub3_' + subP3Index">
                  <td style="padding-left: 84px;">{{subP3.name}}</td>
                   <td><font v-if="subP3.notNull">Y</font></td>
                  <td>{{subP3.type}}</td>
                  <td>{{subP3.description}}</td>
                </tr>


                <template v-if="subP3.subParams && subP3.subParams.length > 0" v-for="(subP4, subP4Index) in subP3.subParams">
                  <tr :key=" 'input_sub4_' + subP4Index">
                    <td style="padding-left: 112px;">{{subP4.name}}</td>
                     <td><font v-if="subP4.notNull">Y</font></td>
                    <td>{{subP4.type}}</td>
                    <td>{{subP4.description}}</td>
                  </tr>
                </template>

              </template>


            </template>

          </template>
        </template>
      </table>
    </div>

    <div v-if="apiData.outputParams && apiData.outputParams.length > 0">


      <div style="padding-left: 0px;padding-top: 6px; margin-bottom: 6px;">输出参数：</div>
      <table cellpadding="1" cellspacing="1" class="comment_table">
        <tr class="table_head">
          <td style="width: 210px">参数名</td>
          <td style="width: 100px">参数类型</td>
          <td>说明</td>
        </tr>
        <template v-for="(input, inputIndex) in apiData.outputParams">
          <tr>
            <td>{{input.name}}</td>
            <td>{{input.type}}</td>
            <td>{{input.description}}</td>
          </tr>
          <template v-if="input.subParams && input.subParams.length > 0" v-for="(subP, subpIndex) in  input.subParams">
            <tr>
              <td style="padding-left: 28px;">{{subP.name}}</td>
              <td>{{subP.type}}</td>
              <td>{{subP.description}}</td>
            </tr>

            <template v-if="subP.subParams && subP.subParams.length > 0">
              <tr v-for="(subP2, subP2Index) in subP.subParams" :key="subP2Index">
                <td style="padding-left: 56px;">{{subP2.name}}</td>
                <td>{{subP2.type}}</td>
                <td>{{subP2.description}}</td>
              </tr>
            </template>
          </template>
        </template>
      </table>
    </div>

  </div>
</template>

<script>
export default {

  data() {
    return {

      /* tree */
      defaultActive: '', //  menuGroup[0].children[0].path,
      defaultOpeneds: [],

      apiWindowHeight: 0,
      uriMap: {},

      randomDivid: 'id_' + Math.random()

    }
  },
  mounted() {

  },
  created() {


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

    }

  },
  methods: {

  }
}

</script>
<style>


</style>
