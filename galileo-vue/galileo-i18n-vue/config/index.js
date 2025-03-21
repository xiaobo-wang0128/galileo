'use strict'
// Template version: 1.3.1
// see http://vuejs-templates.github.io/webpack for documentation.

const path = require('path')

// CDN 发布路径
// var CDN_PATH= "//xxx.cdn.com/";
// index.html 引入的根路径
var CDN_PATH = ''

// var JAVA_API_ADDRESS = 'http://192.168.4.254:6301'

var JAVA_API_ADDRESS = 'http://i18n.okios.cn/'

module.exports = {
  dev: {
    host: '0.0.0.0', // can be overwritten by process.env.HOST
    port: 8080, // can be overwritten by process.env.PORT, if port is in use, a free one will be determined
    proxyTable: {
      '/**/*.json': {
        target: JAVA_API_ADDRESS,
        changeOrigin: true,
        pathRewrite: {}
      },
      '/**/*.htm': {
        target: JAVA_API_ADDRESS,
        changeOrigin: true,
        pathRewrite: {}
      },
      '/kernel/sys/constant.js':{
        target: JAVA_API_ADDRESS,
        changeOrigin: true,
        pathRewrite: {}
      },
      '/statics/*': {
        target: JAVA_API_ADDRESS,
        changeOrigin: true,
        pathRewrite: {}
      },
      '/check/*': {
        target: JAVA_API_ADDRESS,
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
    assetsSubDirectory: 'statics',
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
