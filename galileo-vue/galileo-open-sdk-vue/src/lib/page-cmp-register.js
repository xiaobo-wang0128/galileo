var routes = []

const isDev = process.env.NODE_ENV === 'development'

/* --------------------------------------------------------------------------------- */
/* -----------------------------  此处按格式添加模块---------------------------------- */
/* --------------------------------------------------------------------------------- */

// $portal_context +

routes.push({
  name: '接口文档',
  path:  '/',
  component: () => import('../page/api_doc.vue')
})

routes.push({
  name: '接口文档',
  path:  '/api_doc',
  component: () => import('../page/api_doc.vue')
})

routes.push({
  name: '接口日志',
  path:  '/api_log',
  component: () => import('../page/api_log.vue')
})

routes.push({
  name: '应用管理',
  path: '/app_info',
  component: () => import('../page/app_info.vue')
})

/* --------------------------------------------------------------------------------- */
/* -----------------------------  以下代码请勿修改  ---------------------------------- */
/* --------------------------------------------------------------------------------- */

var routeMap = {}
var namesMap = {}
var components = []
for (var i = 0; i < routes.length; i++) {
  routeMap[routes[i].path] = routes[i].component

  namesMap[routes[i].path] = routes[i].name

  components.push(routes[i].component)
}

export default {

  props: ['uri'],
  // components: components,

  methods: {
    getRoutes () {
      return routes
    },

    getPageName (uri) {
      if (namesMap[uri]) {
        return namesMap[uri]
      }
      return ''
    }
  },

  getPageName (uri) {
    if (namesMap[uri]) {
      return namesMap[uri]
    }
    return ''
  },

  computed: {
    ViewComponent () {
      const matchingView = routeMap[this.uri]
      return matchingView
    }
  },
  render (h) {
    return h(this.ViewComponent)
  }
}
