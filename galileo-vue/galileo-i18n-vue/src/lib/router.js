import Vue from 'vue'
import Router from 'vue-router'

import MainLayout from '../layout/main-layout'

import SingleLayout from '../layout/single-layout'


Vue.use(Router)


const routes = [
// {
//   path: $portal_context,
//   component: MainLayout
// },
//   // 带菜单的配置页
//   {
//     path: $portal_context + '/page/*',
//     component: MainLayout
//   },
  // 独立页面浏览
  {
    path: $portal_context + '/',
    component: SingleLayout,
    children: [
      {
        path: '/',
        component: () => import('../page/i18n.vue')
      },
      {
        path: '/util',
        component: () => import('../page/util.vue')
      }
    ]
  }

]


let RouterInstance = new Router({
  base: '/',
  mode: 'history',
  routes: routes
})


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
