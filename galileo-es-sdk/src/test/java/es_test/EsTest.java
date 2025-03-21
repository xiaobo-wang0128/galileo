package es_test;

import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.es.EsClientInit;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.xcontent.XContentType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaobo
 * @date 2023/5/18 13:54
 */
public class EsTest {

    public static void main(String[] args) throws Exception {

        EsClientInit esClientInit = new EsClientInit();

//        host: es-cn-5yd37zj4a00031ri1.public.elasticsearch.aliyuncs.com
//        port: 9200
//        usernmae: elastic
//        password: Iml_es_product_2023

        esClientInit.setHost("es-cn-5yd37zj4a00031ri1.public.elasticsearch.aliyuncs.com");
        esClientInit.setPort(9200);
        esClientInit.setUserName("elastic");
        esClientInit.setPassword("Iml_es_product_2023");

        RestHighLevelClient client = esClientInit.getClient();


        String indexName = "test_index";

        {

            CreateIndexRequest request = new CreateIndexRequest(indexName);
            client.indices().create(request, RequestOptions.DEFAULT);
        }

        Map<String, Object> obj = new HashMap<>();
        obj.put("aaa", "bbb");

        String index = CommonUtil.uuid();
        IndexRequest request = new IndexRequest();
        request.index(indexName).id(index);

        String postJson = JsonUtil.toJson(obj);
        request.source(postJson, XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);

        System.out.println("done");



        // 创建请求对象
        GetRequest getRequest = new GetRequest().index(indexName).id(index);
        // 客户端发送请求，获取响应对象
        GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);

        String json = response.getSourceAsString();

        System.out.println(json);

    }


}
