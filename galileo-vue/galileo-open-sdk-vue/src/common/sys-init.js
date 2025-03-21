/**
 * 缓存应用常用 的业务数据
 * @param  {[type]} Vue [description]
 * @return {[type]}     [description]
 */
export default (Vue) => {



  //日期格式化
  Vue.prototype.$dateFormat = function (mill, format) {
    let t = Number.parseInt(mill)
    if (Number.isNaN(t)) return ""
    if (!t) return ""
    let date = new Date(t)

    let fmt = 'YYYY-mm-dd HH:MM'

    if (format) {
      fmt = format
    }
    let ret;
    const opt = {
      "Y+": date.getFullYear().toString(), // 年
      "m+": (date.getMonth() + 1).toString(), // 月
      "d+": date.getDate().toString(), // 日
      "H+": date.getHours().toString(), // 时
      "M+": date.getMinutes().toString(), // 分
      "S+": date.getSeconds().toString() // 秒
    }

    for (let k in opt) {
      ret = new RegExp("(" + k + ")").exec(fmt);
      if (ret) {
        fmt = fmt.replace(ret[1], (ret[1].length == 1) ? (opt[k]) : (opt[k].padStart(ret[1].length, "0")))
      };
    }

    return fmt;

  }


  // Vue.prototype.$loginUser = TrainLoginUser
	// Vue.prototype.$constantArray = {}
	// Vue.prototype.$constantMap = {}
	// for(let key in TrainConstantData){
	// 	let tmpArray = TrainConstantData[key]
	// 	// 缓存常量数据
	// 	Vue.prototype.$constantArray[key] = tmpArray
	// 	// 缓存常量的键值对
	// 	Vue.prototype.$constantMap[key] = {}

	// 	for(let i =0 ; i < tmpArray.length; i++){
	// 		Vue.prototype.$constantMap[key]['v_'+tmpArray[i].code]  = tmpArray[i].label
	// 	}
	// }


	// Vue.prototype.$getDictArray = function(group){

	// 	return Vue.prototype.$constantArray[group]
	// }

	// Vue.prototype.$getDictByValue = function(group, code){

	// 	let tmpGroup = Vue.prototype.$constantMap[group]

	// 	if(tmpGroup){
	// 		return tmpGroup['v_' + code ]
	// 	}

	// 	return ''
	// }


}


