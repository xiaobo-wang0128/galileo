'use strict'
// Template version: 1.3.1
// see http://vuejs-templates.github.io/webpack for documentation.

const path = require('path')

// CDN 发布路径
// var CDN_PATH= "//xxx.cdn.com/";
// index.html 引入的根路径
var CDN_PATH = ''

// var JAVA_API_ADDRESS = 'http://127.0.0.1:9601'

// var API_HOST='http://bronze-api'
var API_HOST='http://192.168.4.243'

var UMS_API_ADDRESS = API_HOST + ':7101'
var WMS_API_ADDRESS = API_HOST + ':7201'
var OMS_API_ADDRESS = API_HOST + ':7301'
var FMS_API_ADDRESS = API_HOST + ':7401'
var MMS_API_ADDRESS = API_HOST + ':7501'
var SRM_API_ADDRESS = API_HOST + ':7601'
var TMS_API_ADDRESS = API_HOST + ':7701'


module.exports = {
  dev: {
    host: '0.0.0.0', // can be overwritten by process.env.HOST
    port: 8081, // can be overwritten by process.env.PORT, if port is in use, a free one will be determined
    proxyTable: {

      '/**/*.htm': {
        target: UMS_API_ADDRESS,
        changeOrigin: true,
        pathRewrite: {}
      },
      '/constant.js':{
        target: UMS_API_ADDRESS,
        changeOrigin: true,
        pathRewrite: {}
      },
      '/statics/*': {
        target: UMS_API_ADDRESS,
        changeOrigin: true,
        pathRewrite: {}
      },
      '/check/*': {
        target: UMS_API_ADDRESS,
        changeOrigin: true,
        pathRewrite: {}
      },

      '/ums-api/*': {
        target: UMS_API_ADDRESS,
        changeOrigin: true,
        pathRewrite: {}
      },
      '/wms-api/*': {
        target: WMS_API_ADDRESS,
        changeOrigin: true,
        pathRewrite: {}
      },
      '/oms-api/*': {
        target: OMS_API_ADDRESS,
        changeOrigin: true,
        pathRewrite: {}
      },
      '/fms-api/*': {
        target: MMS_API_ADDRESS,
        changeOrigin: true,
        pathRewrite: {}
      },
      '/srm-api/*': {
        target: SRM_API_ADDRESS,
        changeOrigin: true,
        pathRewrite: {}
      },
      '/tms-api/*': {
        target: TMS_API_ADDRESS,
        changeOrigin: true,
        pathRewrite: {}
      }

    },

    // index.html 引入的子路径
    assetsSubDirectory: '',
    // index.html 引入的根路径
    assetsPublicPath: '/',

    // Various Dev Server settings
    autoOpenBrowser: false,
    errorOverlay: true,
    notifyOnErrors: true,
    poll: false, // https://webpack.js.org/configuration/dev-server/#devserver-watchoptions-

    // Use Eslint Loader?
    // If true, your code will be linted during bundling and
    // linting errors and warnings will be shown in the console.
    useEslint: true,
    // If true, eslint errors and warnings will also be shown in the error overlay
    // in the browser.
    showEslintErrorsInOverlay: false,

    /**
     * Source Maps
     */

    // https://webpack.js.org/configuration/devtool/#development
    devtool: 'cheap-module-eval-source-map',

    // If you have problems debugging vue-files in devtools,
    // set this to false - it *may* help
    // https://vue-loader.vuejs.org/en/options.html#cachebusting
    cacheBusting: true,

    cssSourceMap: true
  },

  build: {

    // Template for index.html
    index: path.resolve(__dirname, '../dist/index.html'),

    // Paths
    assetsRoot: path.resolve(__dirname, '../dist'),
    assetsSubDirectory: 'resource',
    assetsPublicPath: '',

    /**
     * Source Maps
     */
    productionSourceMap: false,
    // https://webpack.js.org/configuration/devtool/#production
    devtool: '#source-map',

    // Gzip off by default as many popular static hosts such as
    // Surge or Netlify already gzip all static assets for you.
    // Before setting to `true`, make sure to:
    // npm install --save-dev compression-webpack-plugin
    productionGzip: false,
    productionGzipExtensions: ['js', 'css'],

    // Run the build command with an extra argument to
    // View the bundle analyzer report after build finishes:
    // `npm run build --report`
    // Set to `true` or `false` to always turn it on or off
    bundleAnalyzerReport: process.env.npm_config_report

  }
}
