export default `let appId = '10303030401111'
let appSecret = 'abcdefg'
let timestamp = new Date().getTime()
let postJson = JSON.stringify({
  "userAccount": "string",
  "userNickname": "string",
  "userPassword": "string",
  "userStatus": "string",
  "roleType": "string"
})
let apiUrl = 'http://api.kyb.com/open/api/user_create_1'

// 计算签名
let appSign = md5(appSecret + timestamp + postJson)

// http 请求头
let httpHeaders = {
  "x-app-id": appId,
  "x-app-sign": appSign,
  "x-request-time": timestamp
}

// 发送请求
let res = httpUtil.postJson(apiUrl, httpHeaders, postJson)`
