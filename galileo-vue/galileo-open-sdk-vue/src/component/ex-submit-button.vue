<template>


  <el-button v-if="simple" :circle="circle" @click.stop="doDirectRequest" slot="reference" :type="type"
             :disabled="disabled" :icon="loading ? 'el-icon-loading' : icon ">
    <slot></slot>
  </el-button>

  <el-popover v-else placement="top" v-model="visible" :disabled="disabled">

    <div style="font-size: 14px; padding: 4px 4px 16px 30px; position: relative">
      <i class="el-icon-warning" style="color: #E6A23C; font-size: 18px; position: absolute; left: 4px"></i>
      {{actionTitle}}
    </div>

    <div style="text-align: right; padding: 0 4px 0 4px">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="doRequest">确定</el-button>
    </div>

    <el-button slot="reference" :type="type" :icon="loading ? 'el-icon-loading' : icon ">
      <slot></slot>
    </el-button>

  </el-popover>

</template>

<script>

  export default {
    data() {
      return {
        loading: false,
        visible: false,
        disabled: false,
        titleMessage: ((this.message && this.message != '') ? this.message : '确定要继续吗？')
      }
    },
    props: {

      simple: {
        type: Boolean,
        default: true
      },

      circle: {
        type: Boolean,
        default: false
      },

      icon: {
        type: String,
        default: ''
      },

      type: {
        type: String,
        default: 'text'
      },

      actionUrl: {
        type: String,
        default: ''
      },
      actionData: {
        type: Object | Array
      },
      actionTitle: {
        type: String,
        default: '确定要继续吗？'
      },
      successMessage: {
        type: String,
        default: '操作成功'
      },

      message: {
        type: String,
        default: ''
      },

      rowId: {
        type: String,
        default: ''
      },

    },
    methods: {

      doDirectRequest() {

        let _this = this

        if (this.message != '') {

          this.$confirm(_this.message, '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {

            _this.doRequest()

          }).catch(() => {

          });


        } else {
          _this.doRequest()
        }
      },

      doRequest() {
        const _this = this

        if (_this.actionUrl == '') {
          _this.loading = false
          _this.visible = false
          _this.disabled = false

          return
        }

        if (_this.loading) {
          return
        }

        _this.loading = true
        _this.visible = false
        _this.disabled = true

        _this.$emit('beforeSubmit')

        let submitType = '';
        if (this.actionData) {
          submitType = 'json'
        }

        _this.$ajax({
          url: _this.actionUrl,
          method: 'post',
          type: submitType,
          data: _this.actionData,
          success(res) {
            _this.$message({
              showClose: true,
              message: _this.successMessage,
              type: 'success'
            })

            _this.loading = false
            _this.visible = false

            _this.$emit('submitDone', res.data)

          },
          afterAjax() {
            _this.loading = false
            _this.visible = false
            _this.disabled = false
          }
        })
      }
    }
  }
</script>
