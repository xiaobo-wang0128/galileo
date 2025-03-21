//package com;
//
//import lombok.extern.slf4j.Slf4j;
//import org.armada.galileo.document.util.doc.DocGenerate;
//
//import java.net.URL;
//
///**
// * 文档生成器，会在 cicd 时自动执行（也可以单独运行）
// * @author xiaobo
// * @date 2021/11/9 3:59 下午
// */
//@Slf4j
//public class DocGenerateTest {
//
//    public static void main(String[] args) throws Exception {
//
//        URL url = ClassLoader.getSystemResource("client_api_doc.json");
//
//        log.info("json file path: " + url);
//
//        String path = url.getPath();
//
//        path = path.substring(0, path.indexOf("/galileo-start/target/classes/client_api_doc.json"));
//
//        DocGenerate dg = new DocGenerate();
//
//        String source = path + "/galileo-start/src/main/java";
//        String output = path + "/galileo-start/src/main/resources/output_doc.db";
//
//        // dg.initDoc("client_api_doc.json", source, output);
//
//    }
//
//}
