<template>

  <el-container style="overflow: hidden; height: 100%; width: 100%; ">

    <el-header style=" border-bottom: 1px solid #060f1a12; ">
      <api-header></api-header>
    </el-header>

    <el-container class="api-body-contaniner" style="
      padding: 20px 20px 0px 20px;
      width: 1200px; margin-left: 50%; left: -600px; position: relative; ">

      <div style="width: 100%;">
        <div >
          <el-button icon="el-icon-plus" @click="$jump('/app_form')">新增应用</el-button>
        </div>

        <ex-table-auto
          style="margin-top: 20px;"
          class="el-table-nowwarp"
          ref="app_info_grid"
          :url="queryUrl"
          :autoLoad="true"
          :autoFill="false"
          :autoPage="false"
          border>

          <el-table-column type="index" label="#" width="43"></el-table-column>
          <el-table-column prop="appName" label="应用名" min-width="8"></el-table-column>
          <el-table-column prop="appId" label="appId" min-width="8"></el-table-column>
          <el-table-column prop="callbackUrl" label="回传地址" min-width="8"></el-table-column>

          <el-table-column prop="gmtModify" label="最后修改时间" min-width="8" align="center">
            <template slot-scope="scope">
              {{$dateFormat(scope.row.gmtModify)}}
            </template>

          </el-table-column>
          <el-table-column prop="status" label="操作" width="140" align="center">
            <template slot-scope="scope">
              <el-button type="text" @click="$jump('app_form?id=' + scope.row.id)">编辑</el-button>
<!--              <el-button type="text" @click="downloadFile(scope.row.key)">删除</el-button>-->

              <submit-button
                             :actionUrl=" delUrl + '?id=' + scope.row.id"
                             :simple="false"
                             actionTitle="确定删除吗？"
                             @submitDone="$refs['app_info_grid'].loadData()"
              >删除</submit-button>

            </template>
          </el-table-column>

        </ex-table-auto>


      </div>

    </el-container>

  </el-container>

</template>


<script>

  import NavMenu from './api_sub/api-menu.vue'

  import ApiItem from './api_sub/api-item.vue'

  import ApiPortal from './api_sub/api-portal.vue'

  import ApiConfig from './api_sub/api-config.vue'

  import ApiLog from './api_sub/api-log.vue'

  import ApiDoc from './api_sub/api-doc.vue'

  import ApiHeader from './api_sub/api-header.vue'

  export default {

    components: { ApiLog, ApiPortal, NavMenu, ApiItem, ApiConfig, ApiDoc, ApiHeader },

    data() {
      return {

        dialogVisible: false,

        saveButtonDisabled: false,

        dbClearValue: {},

        rules: {},

        dbClearAll: false,

        appArr:[]

      };
    },

    watch: {


    },
    created() {

      this.queryUrl = $portal_context +'/open/OpenApiConfigRpc/queryAllApp.json'
      this.delUrl = $portal_context +'/open/OpenApiConfigRpc/delAppInfo.json';
      this.loadAllApps()
    },
    mounted() {
      var _this = this;

    },

    destroyed() {
    },

    methods: {


      loadAllApps() {
      },


      delPlan(item){

        let _this = this

        this.$confirm('确定删除配置 [' + item.projectName + '] 吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {

          _this.$ajax({
            url: '/haiq-devops/plan/PlanRpc/del.json?id='+item.id,
            success (res) {
              _this.$message({
                showClose: true,
                message: '删除成功',
                type: 'success'
              })

              _this.loadAllProject()
            }
          })

        }).catch(() => {

        });

      }


    }
  }

</script>
<style>
  .db_clear_list .el-checkbox {
    width: 600px;
    line-height: 30px;
  }

  .resource-div-block{
    display: flex;
    flex-flow:row wrap;
    justify-content: flex-start;
    align-items:center;
  }
  .resource-div-block div{
    padding: 14px 40px 14px 40px;
    margin-bottom: 14px;
    font-size: 28px;
    color: #409eff;
    background: #ecf5ff;
    border-color: #b3d8ff;
    margin-right: 20px;
    cursor: pointer;
    position: relative;
  }



</style>
