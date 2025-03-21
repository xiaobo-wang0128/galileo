import Vue from 'vue'
import Router from 'vue-router'

import MainLayout from '../layout/main-layout'

import SingleLayout from '../layout/single-layout'


Vue.use(Router)


const routes = [
  // 带菜单的配置页
  // 独立页面浏览
  {
    path: '/',
    component: SingleLayout,
    children: [
      {
        path: '/',
        component: () => import('../page/index.vue')
      },
      {
        path: '/api_doc/*',
        component: () => import('../page/api_doc.vue')
      },
      {
        path: '/api_log',
        component: () => import('../page/api_log.vue')
      },
      {
        path: '/app_info',
        component: () => import('../page/app_info.vue')
      },
      {
        path: '/app_form',
        component: () => import('../page/app_form.vue')
      }

    ]
  }

]


let RouterInstance = new Router({
  base: '/',
  mode: 'history',
  routes: routes
})


Vue.prototype.$jump = function(path, param) {
  RouterInstance.push({
    path: path,
    query: param
  })
}

Vue.prototype.$jumpBack = function() {
  window.history.go(-1)
}

// RouterInstance.beforeEach(async (to, from, next) => {
//   if (!Vue.prototype.$store['loginUser']) {
//     await Vue.prototype.$ajax({
//       url: '/ess/user/UserRpc/get_login.json',
//       method: 'post',

//       success: function (responseData) {
//         var loginUser = responseData.data

//         Vue.prototype.$store['loginUser'] = loginUser

//         next()
//       },
//       fail: function () {
//         window.location = '/login.htm'
//       }
//     })
//   } else {
//     next()
//   }
// })


export default RouterInstance
