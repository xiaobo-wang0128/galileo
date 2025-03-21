import Vue from 'vue'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import App from './app.vue'
import router from './lib/router'
import moment from 'moment'

import HaiguiExUI from 'galileo-ex-ui-old'

import sysInit from './common/sys-init.js'



moment.locale('zh-cn')

Vue.use(ElementUI, { size: 'small' })

// 加载自定义控件
HaiguiExUI.install(Vue)

sysInit(Vue)

// 为上传控件设置上传、下载 url
Vue.prototype.$ex_default_upload_url = '/asama/aliyun/ResourceRpc/upload.json'
Vue.prototype.$ex_default_download_url = '/asama/aliyun/ResourceRpc/down.json?objectName='

// 登陆 url ，默认为 '/login.htm'
Vue.prototype.$ex_default_login_url = 'http://sophon.okios.cn/login.htm?returnUrl=http://i18n.okios.cn/login.htm'

Vue.prototype.$moment = moment

Vue.prototype.$gridpanels = {}

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
