//package test;
//
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;
////import com.jayway.jsonpath.Configuration;
////import com.jayway.jsonpath.JsonPath;
//import com.jayway.jsonpath.Configuration;
//import com.jayway.jsonpath.JsonPath;
//import org.armada.galileo.common.util.CommonUtil;
//
//import java.nio.charset.StandardCharsets;
//
///**
// * @author xiaobo
// * @date 2023/2/23 13:46
// */
//public class JsonPathTest {
//    public static void main(String[] args) {
//
//        String json = new String(CommonUtil.readFileFromLocal("/Users/wangxiaobo/project/_codes/aml_2022/bronze.git/bronze-oms/oms-web/src/main/resources/open_api.json"), StandardCharsets.UTF_8);
//
//        Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);
//
//        String urlHead = JsonPath.read(document, "$.urlHead");
//        //String groups = JsonPath.read(document, "$.index");
//        //String name = JsonPath.read(document, "$.name");
//
//
//        System.out.println(urlHead);
//        //System.out.println(groups);
//        //System.out.println(name);
//
//        // JsonObject jsonObject = new JsonObject();
//
//
//        //JsonObject jsonObject= (JsonObject) new JsonParser().parse(json);
//
//        //System.out.println(jsonObject.get("groups"));
//
//
//    }
//}
