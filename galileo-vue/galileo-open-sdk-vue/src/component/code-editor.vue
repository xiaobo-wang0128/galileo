<template>
  
  <codemirror :value="inputValue" :options="cmOption" class="code-mirror" @ready="onCmReady3" @focus="onCmFocus" @input="onCmCodeChange" ref="myCmGenerate"></codemirror>

</template>
<script>

import { codemirror } from 'vue-codemirror'

// 我这里引入的是JS语言文件
import 'codemirror/mode/javascript/javascript.js'



import 'codemirror/lib/codemirror.css'
// require active-line.js
import 'codemirror/addon/selection/active-line.js'
// styleSelectedText
import 'codemirror/addon/selection/mark-selection.js'
// hint
import 'codemirror/addon/hint/show-hint.js'
import 'codemirror/addon/hint/sql-hint.js'
import 'codemirror/addon/hint/show-hint.css'
import 'codemirror/addon/hint/javascript-hint.js'
// highlightSelectionMatches
import 'codemirror/addon/scroll/annotatescrollbar.js'
import 'codemirror/addon/search/matchesonscrollbar.js'
import 'codemirror/addon/search/match-highlighter.js'
// keyMap
import 'codemirror/mode/clike/clike.js'
import 'codemirror/mode/sql/sql.js'

import 'codemirror/addon/edit/matchbrackets.js'
import 'codemirror/addon/comment/comment.js'
import 'codemirror/addon/dialog/dialog.js'
import 'codemirror/addon/dialog/dialog.css'
import 'codemirror/addon/search/searchcursor.js'
import 'codemirror/addon/search/search.js'
import 'codemirror/keymap/sublime.js'
// foldGutter
import 'codemirror/addon/fold/foldgutter.css'
import 'codemirror/addon/fold/brace-fold.js'
import 'codemirror/addon/fold/comment-fold.js'
import 'codemirror/addon/fold/foldcode.js'
import 'codemirror/addon/fold/foldgutter.js'
import 'codemirror/addon/fold/indent-fold.js'
import 'codemirror/addon/fold/markdown-fold.js'
import 'codemirror/addon/fold/xml-fold.js'
// 编辑的主题文件
import 'codemirror/theme/monokai.css'
import 'codemirror/theme/base16-light.css'

import 'codemirror/theme/idea.css'


export default {

  components: { codemirror },
  
  data() {
    return {
      item: 'aaa',
      cmOption: {
        tabSize: 2, // tab
        styleActiveLine: true, // 高亮选中行
        lineNumbers: true, // 显示行号
        lineWrapping: true,
        styleSelectedText: true,
        line: true,
        foldGutter: false, // 块槽
        // gutters: ['CodeMirror-linenumbers', 'CodeMirror-foldgutter'],
        highlightSelectionMatches: { showToken: /\w/, annotateScrollbar: true }, // 可以启用该选项来突出显示当前选中的内容的所有实例
        // 模式, 可查看 codemirror/mode 中的所有模式
        mode: { 
          name: 'javascript',
          json: true
        },
        // hint.js options
        //mode: this.mode,
        hintOptions: {
          // 当匹配只有一项的时候是否自动补全
          completeSingle: false
        },
        // 快捷键 可提供三种模式 sublime、emacs、vim
        keyMap: 'sublime',
        matchBrackets: true,
        showCursorWhenSelecting: true,
        theme: 'idea', // 主题 
        extraKeys: { 'Ctrl': 'autocomplete' },  // 可以用于为编辑器指定额外的键绑定，以及keyMap定义的键绑定,
        
        cursorHeight: 0.94
      },

      inputValue: ''

    }
  },

  props:{

    value: {
      type: String,
      default: ''
    },

    height: {
      type: String,
      default: '200px'
    },

    width: {
      type: String,
      default: '100%'
    }

    // 模式, 可查看 codemirror/mode 中的所有模式
    // mode: {
    //   type: Object,
    //   default: {
    //     name: 'javascript',
    //     json: true  
    //   }
    // }

  },

  watch:{

    value(val, old){
      // console.log(val, old)
      this.inputValue = val
    }
  },

  created(){
    this.inputValue = this.value
  },
  methods: {

    setActive(){
    },

    onCmReady3() {
      
      let _this = this
      
      if(this.height !='auto'){
        this.$refs.myCmGenerate.codemirror.setSize( _this.width, _this.height )  
      }
      else{
       this.$refs.myCmGenerate.codemirror.setSize( _this.width  )   
      }

    },
    onCmFocus(instance, event) {
     
    },
    onCmCodeChange(instance, obj) {

      this.$emit('input', instance)
    }
  }
}

</script>
<style>

.CodeMirror-scroll {
  overflow: scroll !important;
  margin-bottom: 0;
  margin-right: 0;
  padding-bottom: 0;
  /*height: 100%;*/
  outline: none;
  position: relative;
  /*border: 1px solid #dddddd;*/

}
.code-mirror {
  font-size: 13px;
  line-height: 150%;
  text-align: left;
}

.CodeMirror {
  border: 1px solid #dddddd;
  height: auto;
}

</style>
