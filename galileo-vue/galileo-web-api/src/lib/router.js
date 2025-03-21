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
  // 带菜单的配置页
  {
    path: $portal_context + '/page/*',
    component: MainLayout
  },
  // 独立页面浏览
  {
    path: $portal_context + '/',
    component: SingleLayout,
    children: [
      // {
      //   path: 'system/sys_config',
      //   component: () => import('../page/system/sys_config.vue')
      // }, {
      //   path: 'system/flow_config',
      //   component: () => import('../page/system/flow_config.vue')
      // },
      // {
      //   path: 'system/plugin_config',
      //   component: () => import('../page/system/plugin_config.vue')
      // },
      // {
      //   path: 'system/i18n_config',
      //   component: () => import('../page/system/i18n_config.vue')
      // },
      {
        path: '/',
        component: () => import('../page/system/api.vue')
      }
      // {
      //   path: 'system/user_role',
      //   component: () => import('../page/system/user_role.vue')
      // },
      // {
      //   path: 'system/user_info',
      //   component: () => import('../page/system/user_info.vue')
      // },
      // {
      //   path: 'system/user_log',
      //   component: () => import('../page/system/user_log.vue')
      // },


// [{
//       "label": "角色列表",
//       "uri": $portal_context + "/page/user/user_role"
//     },
//     {
//       "label": "用户列表",
//       "uri": $portal_context + "/page/user/user_info"
//     },
//     {
//       "label": "用户登陆日志",
//       "uri": $portal_context + "/page/user/user_log"
//     }
//   ]

      // {
      //   path: 'test/test',
      //   component: () => import('../page/test/test.vue')
      // }
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
