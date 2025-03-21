/**
 * 缓存应用常用 的业务数据
 * @param  {[type]} Vue [description]
 * @return {[type]}     [description]
 */
export default (Vue) => {

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


