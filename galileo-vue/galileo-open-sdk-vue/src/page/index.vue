<template>

  <el-container style="overflow: hidden; height: 100%; width: 100%; ">

    <el-header style=" border-bottom: 1px solid #060f1a12; ">
      <api-header></api-header>
    </el-header>

    <el-container class="api-body-contaniner" style="
      padding: 20px 20px 0px 20px;
      width: 1200px; margin-left: 50%; left: -600px; position: relative; ">

      <div style="width: 100%;">

        home

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
