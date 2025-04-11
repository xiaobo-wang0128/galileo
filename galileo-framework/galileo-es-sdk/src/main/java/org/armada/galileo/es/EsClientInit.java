package org.armada.galileo.es;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.Convert;
import org.armada.galileo.common.util.SpringConfig;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author xiaobo
 * @date 2023/2/8 11:44
 */
@Slf4j
@Data
public class EsClientInit {

    @Value("${es.host:192.168.4.254}")
    String host ;

    @Value("${es.port:9200}")
    Integer port;

    @Value("${es.username:}")
    String userName;

    @Value("${es.password:}")
    String password ;


    public RestHighLevelClient getClient() {
        RestHighLevelClient client = initClient(host, port, userName, password);
        return client;
    }

    private RestHighLevelClient initClient(String host, Integer port, String userName, String password) {

        log.info("start initIndex es client, host:{}, port:{}, username:{}, password:{}", host, port, userName, password);

        if (CommonUtil.isEmpty(host)) {
            log.error("initIndex es client error, host is null");
            System.exit(0);
        }

        if (port == null) {
            log.error("initIndex es client error, port is null");
            System.exit(0);
        }

        if (CommonUtil.isEmpty(userName) && CommonUtil.isEmpty(password)) {
            return new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http")));
        } else {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(userName, password)
            );  //es账号密码（默认用户名为elastic）

            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(host, port, "http"))
                            .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                                public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                                    httpClientBuilder.disableAuthCaching();
                                    return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                                }
                            }));

            return client;
        }

    }
}
