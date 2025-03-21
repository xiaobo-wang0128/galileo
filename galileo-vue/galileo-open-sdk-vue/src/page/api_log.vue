<template>

  <el-container style="overflow: hidden; height: 100%; width: 100%; ">

    <el-header style=" border-bottom: 1px solid #060f1a12; ">
      <api-header></api-header>
    </el-header>

    <el-container class="api-body-contaniner"
      style="
        padding: 20px 0px 0px 0px;
        width: 1200px; margin-left: 50%; left: -600px;
        position: relative; ">
      <api-log :apiDataList="apiRoot" :apiUrlCahce="apiUrlCahce"></api-log>
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

    components: {ApiLog, ApiPortal, NavMenu, ApiItem, ApiConfig, ApiDoc, ApiHeader},

    data() {
      return {

        apiRoot: [],

        apiUrlCahce: {}
      };
    },

    watch: {},
    created() {
      var _this = this

      _this.$ajax({
        url: $portal_context + '/open/AutoDocumentRpc/getAllDocs.json',
        success: function (res) {

          let apiUrlCahce = {}

          res.data.list.forEach(module=>{

            module.groups.forEach(group=>{

              // console.log(group)

              group.children.forEach(api=>{

                let key = api.apiUrl

                apiUrlCahce[key] = api.apiName
              })
            })

            _this.apiRoot = res.data.list
            _this.apiUrlCahce = apiUrlCahce

          })

        }
      })

    },
    mounted() {
      var _this = this;

    },

    destroyed() {
    },

    methods: {}
  }

</script>
<style>


</style>
