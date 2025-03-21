<template>
  <div class="api-doc-wrapper">

    <!-- v-model="selectedTabId" -->
    <el-tabs  tab-position="right" class="api-doc-item"  v-bind:style="{ height: (windowHeight - 130) + 'px'}" @tab-click="tabClick" > <!-- style="height: calc(100%-100px); "  -->
      <el-tab-pane >
        <span slot="label"><i class="el-icon-notebook-1"></i> 接口文档</span>
        <api-doc :apiData="apiData" :windowHeight="windowHeight"></api-doc>
      </el-tab-pane>

      <el-tab-pane name="apiDebug">
        <span slot="label"><i class="el-icon-cpu"></i> 接口测试</span>
        <api-debug v-for="(item, index) in tmpArr1" :apiData="apiData" :windowHeight="windowHeight" :key="'apiDebug_' + index"></api-debug>
      </el-tab-pane>

<!--      <el-tab-pane name="apiCfg">-->
<!--        <span slot="label"><i class="el-icon-setting"></i> 接口适配</span>-->
<!--        <api-transfer v-for="(item, index) in tmpArr2" :apiData="apiData" :windowHeight="windowHeight" ref="apiCfgPane" :key="'apiCfg_' + index"></api-transfer>-->
<!--      </el-tab-pane>-->

<!--      <el-tab-pane name="apiLog">-->
<!--        <span slot="label"><i class="el-icon-document"></i> 接口日志</span>-->
<!--        <api-log ref="apiLogGridEl" :windowHeight="windowHeight" :apiDataList="[]" :apiUrl="apiData.apiUrl"></api-log>-->
<!--      </el-tab-pane>-->

    </el-tabs>
  </div>
</template>
<script>
import ApiTransfer from './api-transfer.vue'
import ApiDebug from './api-debug.vue'
import ApiDoc from './api-doc.vue'

import ApiLog from './api-log.vue'

export default {

  components: { ApiTransfer, ApiDebug, ApiDoc, ApiLog },

  data() {
    return {
      /* tree */
      defaultActive: '', //  menuGroup[0].children[0].path,
      defaultOpeneds: [],
      uriMap: {},
      windowHeight: -1,

      tmpArr1: [],
      tmpArr2: [],

      selectedTabId: 'apiDebug'

    }
  },
  mounted() {

    let _this = this

    window.onresize = () => {
      _this.autoAdjustHeight()
    }

    _this.autoAdjustHeight()

  },
  created() {

  },
  computed: {

  },
  props: {
    'apiData': {
      type: Object,
      default: {}
    }
  },
  watch: {
    'apiData'(val, old){

    }
  },
  methods: {

    tabClick(tabId){

      let tabName = tabId.name

      // 用for循环实现 codemirror 延时加载，解决样式错乱的问题
      if(tabName=== 'apiDebug'){
        if(this.tmpArr1.length == 0){
          this.tmpArr1.push('1')
        }
      }
      else if(tabName=== 'apiCfg'){
        if(this.tmpArr2.length == 0){
          this.tmpArr2.push('1')
        }
      }



      if(tabName === 'apiLog'){

        this.$refs['apiLogGridEl'].resetHeight2()

      }

    },

    autoAdjustHeight(){
      this.windowHeight = window.innerHeight
    }

  }
}

</script>
<style>



</style>
