/**
 * 缓存应用常用 的业务数据
 * @param  {[type]} Vue [description]
 * @return {[type]}     [description]
 */
export default (Vue) => {

	Vue.prototype.$allLanOptions = [
    {
      "label": "中文",
      "value": "zh"
    },
    {
      "label": "英文",
      "value": "en"
    },
    {
      "label": "俄文",
      "value": "ru"
    },
    {
      "label": "法文",
      "value": "fr"
    },
    {
      "label": "日文",
      "value": "ja"
    },
    {
      "label": "德文",
      "value": "de"
    },
    {
      "label": "朝鲜文",
      "value": "ko"
    },
    {
      "label": "哈萨克语",
      "value": "kk"
    }
  ];

  Vue.prototype.$lanMap = {}
  Vue.prototype.$allLanOptions.forEach(e=>{
    Vue.prototype.$lanMap[e.value] = e
  })

  Vue.prototype.$getLanCN = function(lan){

    // console.log(lan)
    //
    // console.log( (typeof lan) )

    if(!lan){
      return ''
    }

    if(typeof lan == 'object'){
      let result = []
      lan.forEach(e=>{
        result.push(Vue.prototype.$lanMap[e].label)
      })

      return result.join(',')
    }
    else{
      return  Vue.prototype.$lanMap[lan].label
    }
  }



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


