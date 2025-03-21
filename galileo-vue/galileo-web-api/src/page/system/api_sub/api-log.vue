<!--
  接口日志查询
-->
<template>

  <div class="div-table-view-page">

    <el-form ref="form" :model="searchValue" style="width: 100%; " class="search-panel" label-width="80px" >

      <el-row>

        <el-col style="width:230px;">
          <el-form-item label="关键字">
            <el-input v-model="searchValue.keyword" clearable></el-input>
          </el-form-item>
        </el-col>

        <el-col style="width:180px;">
          <el-form-item label="状态"  label-width="60px">
            <ex-select-auto v-model="searchValue.status" :data="[{label:'成功' , value:'success'}, {label: '失败', value:'fail'} , {label: '处理中', value:'doing'} ]"
              clearable
              optionKey="value"
              optionLabel="label"
              optionValue="value"
              placeholder=""
            >
            </ex-select-auto>
          </el-form-item>
        </el-col>

        <el-col style="width:270px;" v-if="apiUrl==='' ">
          <el-form-item label="接口" label-width="60px">

            <el-select v-model="searchValue.apiUrl" clearable placeholder="" >

              <el-option-group
                v-for="(group,index) in apiDataList"
                :key="'g_' + index"
                :label="group.group">
                <el-option
                  v-for="item in group.docList"
                  :key="item.apiUrl"
                  :label="item.apiName"
                  :value="item.apiUrl">
                </el-option>
              </el-option-group>

            </el-select>
          </el-form-item>
        </el-col>


        <el-col style="width:370px;">
          <el-form-item label="时间" label-width="60px">
            <el-date-picker value-format="yyyy-MM-dd" v-model="searchValue.happenTime1" type="date" placeholder="开始时间" style="max-width: 148px; min-width: 128px;" clearable></el-date-picker>
            <el-date-picker value-format="yyyy-MM-dd" v-model="searchValue.happenTime2" type="date" placeholder="结束时间" style="max-width: 148px; min-width: 128px;" clearable></el-date-picker>
          </el-form-item>
        </el-col>


        <el-col style="width:246px;">
          <el-button class="queryBtn" type="primary" icon="el-icon-search" @click="$refs['gridPanelEl'].loadData(searchValue)">
              查询
          </el-button>
          <el-button class="queryBtn"  @click="searchValue={}">
              清空
          </el-button>


          <el-button class="queryBtn"  @click="batchRetry()">
              批量重试
          </el-button>

        </el-col>

      </el-row>
    </el-form>

    <!-- grid panel  -->
    <ex-table-auto
      ref="gridPanelEl"
      class="el-table-nowwarp"
      :url=" queryUrl "
      autoFill
      autoPage
      border
      @selection-change="handleSelectionChange"
    >

      <el-table-column type="selection" width="40" align="center"></el-table-column>

      <el-table-column type="index" label="#" width="43">
        <template slot-scope="scope">
          {{
          ($refs['gridPanelEl'].getLastRequestParam().pageIndex - 1) * $refs['gridPanelEl'].getLastRequestParam().pageSize + scope.$index + 1
          }}
        </template>
      </el-table-column>

      <el-table-column prop="happenTime" label="发生时间" min-width="12"></el-table-column>


      <el-table-column prop="transferType" label="调用方向" min-width="10">
        <template slot-scope="scope">
          <font v-if="scope.row.transferType==='haiq_to_customer'">haiq &gt; customer</font>
          <font v-if="scope.row.transferType==='customer_to_haiq'">customer &gt; haiq</font>
        </template>
      </el-table-column>

      <el-table-column prop="apiName" label="接口名" min-width="16" show-overflow-tooltip></el-table-column>

      <el-table-column prop="timeCost" label="耗时（ms）" min-width="10"></el-table-column>

      <el-table-column prop="status" label="状态" min-width="8">
        <template slot-scope="scope">
          <font v-if="scope.row.status==='success'">成功</font>
          <font v-if="scope.row.status==='doing'">处理中</font>
          <font style="color: red;" v-if="scope.row.status==='fail'">失败</font>
          <font v-if="scope.row.retryTime > 1">({{ scope.row.retryTime }})</font>
        </template>
      </el-table-column>

      <el-table-column prop="requestJson" label="请求" min-width="20">
        <template slot-scope="scope">
          <el-tooltip placement="left" effect="light">
            <div slot="content" style="line-height: 24px; width: 600px; font-size: 14px; ">
              <el-button @click="$copyToClip(scope.row.requestJson, '请求内容已复制到剪切板')" type="text" style="padding-top: 0px; padding-bottom: 0px;">复制到剪切板</el-button>
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
          <el-button :disabled="scope.row.transferType==='customer_to_haiq'"
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

    <el-dialog title="回调重试" :visible.sync="dialogFormVisible" >

      <el-form :model="retryForm" style="padding-top: 0px; padding-bottom: 0px;">
        <el-form-item label="请求参数" >
          <code-editor v-model="retryForm.requestJson" height="400px"></code-editor>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="retryRequest()" :disabled="saveButtonDisabled" >确 定</el-button>
      </div>

    </el-dialog>


  </div>
</template>
<script>

import CodeEditor from '@/component/code-editor.vue'

export default {

  components:{ CodeEditor },


  data() {
    return {
      saveButtonDisabled: false,
      isShow: false,
      rowData: {},

      searchValue: {},
      configValueTime: '',

      queryUrl: '',

      dialogFormVisible: false,
      retryForm: {
        requestId: '',
        requestJson: ''
      },

      multipleSelection:[]


    };
  },

  created() {
    var _this = this

    if(this.apiUrl!=''){
      _this.queryUrl = $portal_context + '/document/AutoDocumentRpc/queryRequestMsg.json?apiUrl=' + this.apiUrl
    }
    else{
      _this.queryUrl = $portal_context + '/document/AutoDocumentRpc/queryRequestMsg.json'
    }

  },

  mounted(){
    this.resetHeight()
  },

  props: {
    'apiDataList': {
      type: Array,
      default: []
    },

    'windowHeight':{
      type: Number,
      default: -1
    },

    'apiUrl':{
      type: String,
      default: ''
    }
  },

  destroyed() {

  },

  methods: {

    resetHeight2(){
      // this.$refs['gridPanelEl'].setHeight(140 + 'px')

      this.$refs['gridPanelEl'].setHeight(window.innerHeight - 229)
    },

    resetHeight(){
      //

      //this.$refs['gridPanelEl'].setHeight((window.innerHeight - 400) + 'px')

      //this.$refs['gridPanelEl'].autoResizeHeight()

    },

    maxLenghTrim(str, len){
      if(!str){
        return ''
      }
      if(str.length < len){
        return str
      }
      return str.substring(0, len) + '...'
    },

    batchRetry(){
      let _this = this
      if(this.multipleSelection.length == 0){
        _this.$message({
          type: 'error',
          message: '请选中要重试的记录'
        })
        return
      }


      let ids = this.multipleSelection.filter(e=>e.transferType == 'haiq_to_customer' && e.status =='fail').map(e=>e.requestId)

      if(!ids || ids.length ==0){
        _this.$message({
          type: 'error',
          message: '没有需要重试的记录，批量重试只针对调用方向为"haiq > customer" 状态为"失败"的记录'
        })
        return
      }

      this.$confirm('本操作只针对调用方向为"haiq > customer" 状态为"失败"的记录<br/>当前可重试记录数为 '+ids.length+', 是否继续?', '提示', {
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

    retryRequest(){

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
  .retry-dialog-message{
    width: 500px;
  }
</style>
