<template>
  <div class="api-doc-wrapper">
    <el-tabs tab-position="right" class="api-doc-item"  v-bind:style="{ height: (windowHeight - 130) + 'px'}" > <!-- style="height: calc(100%-100px); "  -->

      <el-tab-pane>
        <span slot="label"><i class="el-icon-notebook-1"></i> 概要</span>

        <div :style="{ 'height': (windowHeight - 142) + 'px', 'overflow': 'auto', 'display': 'flex', 'box-sizing': 'border-box','flex-direction': 'column'}">

          <div style="border:1px solid #ccc; margin:10px 10px 10px 15px;  background-color: #FAF3E0; padding: 8px 10px; font-size: 16px;">接口数据返回格式说明</div>

          <div style="display: flex; flex: 1; overflow: auto; ">

            <div style="flex:1 ;">
<pre style="font-size: 14px;">
	请求成功：
	1、无返回值  <font color="blue">void</font><a name="1"></a>
	{
		"code": 0
	}

	2、简单类型返回  <font color="blue">string/float/double/integer</font><a name="2"></a>
	{
		"code": 0,
		"data": "string/int/float"
	}

	3、单条记录返回  <font color="blue">object</font><a name="3"></a>
	{
		"code": 0,
		"data":  {
			"id": 123,
			"name": "张三",
			.....
		}
	}

	4、集合（不带分页） <font color="blue">list&lt;object&gt;或set&lt;object&gt;</font><a name="4"></a>
	{
		"code" : 0,
		"data" : {
			"rows" : [{
				"cityId" : 51,
				"cityName" : "大连",
				"commonText" : "666",
				"createTime" : "2015-08-14 12:10:58",
				"createUserId" : 1
			}, {
				"commonText" : "777",
				"createTime" : "2015-08-14 14:00:48",
				"createUserId" : 1,
				"id" : 7
			}]
		}
	}

	5、集合（带分页） <font color="blue">list&lt;object&gt;或set&lt;object&gt;</font><a name="5"></a>
	{
		"code" : 0,
		"data" : {
			"currentPage" : 1,
			"hasNext" : true,
			"hasPre" : false,
			"pageSize" : 20,
			"totalPage" : 15,
			"totalSize" : 300,
			"rows" : [{
				"cityId" : 51,
				"cityName" : "大连",
				"commonText" : "666",
				"createTime" : "2015-08-14 12:10:58",
				"createUserId" : 1
			}, {
				"commonText" : "777",
				"createTime" : "2015-08-14 14:00:48",
				"createUserId" : 1,
				"id" : 7
			}]
		}
	}

	请求失败：<a name="6"></a>
	{
		"code":  错误编码（非0）,
		"message": "error message",
		"data": {

		}
	}

	<font color=red>code 错误码：</font>	<a name="7"></a>
        0		操作成功
        404		接口不存在
        500		后台业务异常（已捕获）
        503		后台系统异常（未捕获）
        401		未登陆
        403		权限异常，没有权限访问


</pre>
            </div>

            <div style="width: 430px; line-height: 32px;">
              请求成功<br/>
              <a href="#1">1、无返回值(void)</a><br/>
              <a href="#2">2、简单类型返回(string) </a><br/>
              <a href="#3">3、单条记录返回(object)</a><br/>
              <a href="#4">4、集合（不带分页）(collection) </a><br/>
              <a href="#5">5、集合（带分页）(collection_page) </a><br/>
              请求失败<br/>
              <a href="#6">失败错误信息 </a><br/>
              <a href="#7">errorCode错误码 </a><br/>

            </div>
          </div>

        </div>

      </el-tab-pane>
<!--
      <el-tab-pane>
        <span slot="label"><i class="el-icon-cpu"></i> 接口测试</span>
      </el-tab-pane>

      <el-tab-pane>
        <span slot="label"><i class="el-icon-setting"></i> 联调适配</span>
      </el-tab-pane>

      <el-tab-pane>
        <span slot="label"><i class="el-icon-document"></i> 接口日志</span>
      </el-tab-pane>       -->


    </el-tabs>
  </div>
</template>
<script>
import ApiDebug from './api-debug.vue'
import ApiDoc from './api-doc.vue'

export default {

  components: { ApiDebug, ApiDoc },

  data() {
    return {
      /* tree */
      defaultActive: '', //  menuGroup[0].children[0].path,
      defaultOpeneds: [],
      uriMap: {},
      windowHeight: -1
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
    // 'apiData': {
    //   type: Object,
    //   default: {}
    // }
  },
  watch: {

  },
  methods: {

    autoAdjustHeight(){
      this.windowHeight = window.innerHeight
    }

  }
}

</script>
<style>




</style>
