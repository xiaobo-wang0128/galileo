<!--
  接口日志查询
-->
<template>

  <div style="width: 100%;">

    <el-form ref="form" :inline="true" :model="searchValue" style="width: 100%; " class="search-panel">

      <el-form-item label="关键字">
        <el-input v-model="searchValue.keyword" clearable></el-input>
      </el-form-item>

      <el-form-item label="状态">
        <ex-select-auto
          style="width: 90px;"
          v-model="searchValue.status"
          :data="[{label:'成功' , value:'SUCCESS'}, {label: '失败', value:'FAIL'} , {label: '处理中', value:'DOING'} ]"
          clearable
          optionKey="value"
          optionLabel="label"
          optionValue="value"
          placeholder="状态"
        >
        </ex-select-auto>
      </el-form-item>

      <el-form-item label="接口">
        <el-select v-model="searchValue.apiUrl" clearable placeholder="" >
          <template v-for="module,moduleIndex in apiDataList">

            <el-option-group
              v-for="group,groupIndex in module.groups"
              :key="moduleIndex + '_' + groupIndex"
              :label="group.name">
              <el-option
                v-for="item in group.children"
                :key="item.apiUrl"
                :label="item.apiName"
                :value="item.apiUrl">
              </el-option>
            </el-option-group>

          </template>
        </el-select>
      </el-form-item>


      <el-form-item label="时间" >
        <el-date-picker
          value-format="timestamp" v-model="searchValue.happenTime1" type="date" placeholder="开始时间"
                        style="max-width: 128px;" :clearable="false"></el-date-picker>
        <el-date-picker value-format="timestamp" v-model="searchValue.happenTime2" type="date" placeholder="结束时间"
                        style="max-width: 128px;" :clearable="false" ></el-date-picker>

      </el-form-item>


      <el-button class="queryBtn" type="primary" icon="el-icon-search"
                 @click="$refs['gridPanelEl'].loadData(searchValue)">查询</el-button>
      <el-button class="queryBtn" @click="searchValue = defaultSearchValue">重置</el-button>
<!--      <el-button class="queryBtn" @click="batchRetry()">批量重试</el-button>-->

    </el-form>

    <!-- grid panel  -->
    <ex-table-auto
      ref="gridPanelEl"
      class="el-table-nowwarp"
      :url="queryUrl"
      autoFill
      autoPage
      border
      @selection-change="handleSelectionChange"
      :autoLoad="false"
    >

<!--      <el-table-column type="selection" width="40" align="center"></el-table-column>-->

      <el-table-column type="index" label="#" width="52" align="center">
        <template slot-scope="scope">
          {{
          ($refs['gridPanelEl'].getLastRequestParam().pageIndex - 1) *
          $refs['gridPanelEl'].getLastRequestParam().pageSize + scope.$index + 1
          }}
        </template>
      </el-table-column>

      <el-table-column prop="happenTime" label="发生时间" min-width="14">
        <template slot-scope="scope">
          {{ $dateFormat(scope.row.happenTime) }}
        </template>
      </el-table-column>


<!--      <el-table-column prop="transferType" label="调用方向" min-width="10">-->
<!--        <template slot-scope="scope">-->
<!--          <font v-if="scope.row.transferType==='haiq_to_customer'">haiq &gt; customer</font>-->
<!--          <font v-if="scope.row.transferType==='customer_to_haiq'">customer &gt; haiq</font>-->
<!--        </template>-->
<!--      </el-table-column>-->

      <el-table-column prop="apiName" label="接口名" min-width="16" show-overflow-tooltip>
        <template slot-scope="scope">
          {{ getApiName(scope.row.apiUrl) }}
        </template>
      </el-table-column>

      <el-table-column prop="timeCost" label="耗时 ms" min-width="8" align="center"></el-table-column>

      <el-table-column prop="status" label="状态" min-width="8" align="center">
        <template slot-scope="scope">
          <font v-if="scope.row.status==='SUCCESS'">成功</font>
          <font v-if="scope.row.status==='DOING'">处理中</font>
          <font style="color: red;" v-if="scope.row.status==='FAIL'">失败</font>
          <font v-if="scope.row.retryTime > 1">({{ scope.row.retryTime }})</font>
        </template>
      </el-table-column>

      <el-table-column prop="requestJson" label="请求" min-width="20">
        <template slot-scope="scope">
          <el-tooltip placement="left" effect="light">
            <div slot="content" style="line-height: 24px; width: 600px; font-size: 14px; ">
              <el-button @click="$copyToClip(scope.row.requestJson, '请求内容已复制到剪切板')" type="text"
                         style="padding-top: 0px; padding-bottom: 0px;">复制到剪切板
              </el-button>
              <br/>
              {{ maxLenghTrim(scope.row.requestJson, 1000) }}
            </div>
            <font>{{ maxLenghTrim(scope.row.requestJson, 50) }}</font>
          </el-tooltip>
        </template>
      </el-table-column>

      <el-table-column prop="errorMessage" label="响应" min-width="18">
        <template slot-scope="scope">
          <el-tooltip placement="left" effect="light">
            <div slot="content" style="line-height: 24px; width: 300px; font-size: 14px; ">
              {{ scope.row.errorMessage ? scope.row.errorMessage : scope.row.responseJson }}
            </div>
            <font>{{ scope.row.errorMessage ? scope.row.errorMessage : scope.row.responseJson }}</font>
          </el-tooltip>
        </template>
      </el-table-column>

      <el-table-column label="" align="center" width="100">
        <template slot-scope="scope">
          <el-button :disabled="scope.row.apiType==='RequestApi'"
                     @click="
                retryForm = { requestId: scope.row.requestId, requestJson: scope.row.requestJson};
                dialogFormVisible = true;
              "
                     type="text"
                     size="medium">
            重试
          </el-button>
        </template>
      </el-table-column>

    </ex-table-auto>

    <!-- Form -->

    <el-dialog title="回调重试" :visible.sync="dialogFormVisible">

      <el-form :model="retryForm" style="padding-top: 0px; padding-bottom: 0px;">
        <el-form-item label="请求参数">
          <code-editor v-model="retryForm.requestJson" height="400px"></code-editor>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="retryRequest()" :disabled="saveButtonDisabled">确 定</el-button>
      </div>

    </el-dialog>


  </div>
</template>
<script>

  import CodeEditor from '@/component/code-editor.vue'

  export default {

    components: {CodeEditor},

    data() {
      let day1 = new Date().getTime()

      let day = new Date();

      day.setHours(0)
      day.setMinutes(0)
      day.setSeconds(0)
      day.setMilliseconds(0)

      let today = day.getTime()

      return {
        saveButtonDisabled: false,
        isShow: false,
        rowData: {},

        defaultSearchValue :{
          "apiUrl": "",
          "status": "",
          "happenTime1": today,
          "happenTime2": today
        },

        searchValue: {
          "happenTime1": today,
          "happenTime2": today
        },
        configValueTime: '',

        queryUrl: '',

        dialogFormVisible: false,
        retryForm: {
          requestId: '',
          requestJson: ''
        },
        multipleSelection: []
      };
    },

    created() {
      var _this = this

      if (this.apiUrl != '') {
        _this.queryUrl = $portal_context + '/open/OpenApiConfigRpc/queryOpenRequestMessage.json?apiUrl=' + this.apiUrl
      } else {
        _this.queryUrl = $portal_context + '/open/OpenApiConfigRpc/queryOpenRequestMessage.json'
      }



    },

    mounted() {

      let _this = this

      _this.$refs['gridPanelEl'].loadData(_this.searchValue)
      //this.resetHeight()
    },

    props: {
      'apiDataList': {
        type: Array,
        default: []
      },

      'windowHeight': {
        type: Number,
        default: -1
      },

      'apiUrl': {
        type: String,
        default: ''
      },
      'apiUrlCahce' :{}
    },

    destroyed() {
    },

    methods: {

      getApiName(apiUrl){
        // console.log(this.apiUrlCahce)
        return this.apiUrlCahce[apiUrl]
      },

      resetHeight2() {
        this.$refs['gridPanelEl'].setHeight(window.innerHeight - 229)
      },

      resetHeight() {
        //
        //this.$refs['gridPanelEl'].setHeight((window.innerHeight - 400) + 'px')
        //this.$refs['gridPanelEl'].autoResizeHeight()
      },

      maxLenghTrim(str, len) {
        if (!str) {
          return ''
        }
        if (str.length < len) {
          return str
        }
        return str.substring(0, len) + '...'
      },

      batchRetry() {
        let _this = this
        if (this.multipleSelection.length == 0) {
          _this.$message({
            type: 'error',
            message: '请选中要重试的记录'
          })
          return
        }


        let ids = this.multipleSelection.filter(e => e.transferType == 'haiq_to_customer' && e.status == 'fail').map(e => e.requestId)

        if (!ids || ids.length == 0) {
          _this.$message({
            type: 'error',
            message: '没有需要重试的记录，批量重试只针对调用方向为"haiq > customer" 状态为"失败"的记录'
          })
          return
        }

        this.$confirm('本操作只针对调用方向为"haiq > customer" 状态为"失败"的记录<br/>当前可重试记录数为 ' + ids.length + ', 是否继续?', '提示', {
          dangerouslyUseHTMLString: true,
          customClass: 'retry-dialog-message',
          width: 300,
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {

          _this.$ajax({
            url: $portal_context + '/document/AutoDocumentRpc/batchRetryRequest.json',
            data: {
              "ids": ids.join(',')
            },
            success(res) {
              _this.$message({
                showClose: true,
                message: '操作成功',
                type: 'success'
              })
              _this.$refs['gridPanelEl'].loadData()
            }
          })

        }).catch(() => {

        });

      },

      handleSelectionChange(val) {
        this.multipleSelection = val;
      },

      showData(data) {
        this.rowData = data.row
        this.isShow = true
      },

      retryRequest() {

        let _this = this

        _this.saveButtonDisabled = true

        _this.$ajax({
          url: $portal_context + '/document/AutoDocumentRpc/retryRequest.json',
          data: _this.retryForm,
          success(res) {
            _this.$message({
              showClose: true,
              message: '操作成功',
              type: 'success'
            })
            _this.$refs['gridPanelEl'].loadData()
            _this.dialogFormVisible = false
          },

          afterAjax() {
            _this.saveButtonDisabled = false
          }
        })

      }


    }
  };

</script>
<style>
  .retry-dialog-message {
    width: 500px;
  }
</style>
