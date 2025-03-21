<template>
  <el-popover placement="top" v-model="visible">
    <div style="font-size: 14px; padding: 4px 4px 16px 30px; position: relative">
      <i class="el-icon-warning" style="color: #E6A23C; font-size: 18px; position: absolute; left: 4px"></i>
      {{titleMessage}}
    </div>
    <div style="text-align: right; padding: 0 4px 0 4px">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="deleteThisRow()">确定</el-button>
    </div>
    <el-button slot="reference" type="text" :disabled="disabled">{{label}}</el-button>
  </el-popover>
</template>

<script>
export default {
  data() {
    return {
      visible: false,
      titleMessage: ((this.message && this.message != '') ? this.message : '确定要删除该记录吗？')
    }
  },
  props: {
    deleteUrl: {
      type: String,
      default: ''
    },
    disabled: {
      type: Boolean,
      default: false
    },
    message: {
      type: String,
      default: ''
    },
    label:{
      type: String,
      default: '删除'
    },
    rowId: {
      type: String,
      default: ''
    },
    deleteParam: {
      type: Object
    }
  },
  methods: {
    deleteThisRow () {
      const _this = this
      let data = {}

      if (this.deleteParam) {
        data = this.deleteParam
      } else {
        data.securityId = _this.rowId
      }
      
      _this.$ajax({
        url: _this.deleteUrl,
        method: 'post',
        data,
        success (res) {
          // _this.$message({
          //   showClose: true,
          //   message: _this.label + '成功',
          //   type: 'success'
          // })

          _this.$emit('afterOperationDone', res)
        },
        afterAjax () {
          _this.visible = false
        }
      })
    }
  }
}
</script>
