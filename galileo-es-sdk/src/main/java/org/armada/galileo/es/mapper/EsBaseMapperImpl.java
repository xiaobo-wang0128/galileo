package org.armada.galileo.es.mapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.lock.ConcurrentLock;
import org.armada.galileo.common.page.PageList;
import org.armada.galileo.common.page.PageParam;
import org.armada.galileo.common.page.ThreadPagingUtil;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.es.EsClientInit;
import org.armada.galileo.es.annotation.EsIndex;
import org.armada.galileo.es.entity.EsBaseEntity;
import org.armada.galileo.es.query.EsQueryWrapper;
import org.armada.galileo.exception.BizException;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author xiaobo
 * @date 2023/2/8 11:53
 */
@Slf4j
public class EsBaseMapperImpl<DO extends EsBaseEntity> implements EsBaseMapper<DO> {

    private static RestHighLevelClient client = null;

    private Class<DO> entityClass;

    private String indexName;

    private static Map<String, Boolean> initedIndexNames = new HashMap<>();

    private AtomicBoolean hasInit = new AtomicBoolean(false);

    private static AtomicBoolean hasInitClient = new AtomicBoolean(false);

    private static String baseType = "long, integer, short, byte, double, float, boolean";

    static List<String> baseTypes = new ArrayList<>();

    @Autowired
    private ConcurrentLock concurrentLock;

    @Autowired
    private ApplicationContext applicationContext;

    public Class<DO> getEntityClass() {
        return entityClass;
    }

    @Value("${spring.profiles.active}")
    private String active;

    static {
        for (String s : baseType.split(",")) {
            if (s.matches("\\s*")) {
                continue;
            }
            s = s.trim();
            baseTypes.add(s);
        }
    }

    public EsBaseMapperImpl() {
    }

    @PostConstruct
    public void initIndex() {

        if (hasInitClient.compareAndSet(false, true)) {
            EsClientInit esClientInit = applicationContext.getAutowireCapableBeanFactory().createBean(EsClientInit.class);
            EsBaseMapperImpl.client = esClientInit.getClient();
        }

        Class<DO> clz = (Class<DO>) CommonUtil.getSuperClassGenericType(getClass(), 0);
        String tmpName = clz.getName();
        if (tmpName.indexOf(".") != -1) {
            tmpName = tmpName.substring(tmpName.lastIndexOf(".") + 1);
        }

        tmpName = CommonUtil.convertJavaField2DB(tmpName);


        this.entityClass = clz;
        this.indexName = tmpName + "_" + active;

        synchronized (log) {
            if (initedIndexNames.get(indexName) != null && initedIndexNames.get(indexName)) {
                log.error("出现了重复的 index_name: " + indexName + ", 请确保 index_name 的唯一性");
                System.exit(0);
            }
            initedIndexNames.put(indexName, true);
        }


        String lockKey = "auto_create_es_index_" + indexName;
        if (!concurrentLock.lock(lockKey, null)) {
            return;
        }

        try {
            autoCreateEsIndex();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            concurrentLock.unlock(lockKey);
        }
    }

    private void autoCreateEsIndex() {

        log.info("create es index: " + indexName);
        if (hasInit.compareAndSet(false, true)) {

            // 检查索引是否存在
            boolean exist = false;
            try {
                // 查询索引 - 请求对象
                GetIndexRequest request = new GetIndexRequest(indexName);
                // 发送请求，获取响应
                GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);

                exist = response.getIndices() != null && response.getIndices().length > 0 && response.getIndices()[0].equals(indexName);

            } catch (ElasticsearchStatusException e) {
                if (e.status() == RestStatus.NOT_FOUND) {
                    exist = false;
                }
            } catch (IOException e) {
            }


            List<Field> fields = new ArrayList<>();
            if (entityClass.getSuperclass() != null) {
                for (Field declaredField : entityClass.getSuperclass().getDeclaredFields()) {
                    fields.add(declaredField);
                }
            }

            for (Field field : entityClass.getDeclaredFields()) {
                fields.add(field);
            }

            //
            List<EsIndexType> indexTypes = new ArrayList<>();

            for (Field field : fields) {
                EsIndex esIndex = field.getAnnotation(EsIndex.class);

                EsIndexType indexType = new EsIndexType();
                indexType.setFieldName(field.getName());
                indexType.setIndex(false);

                String fieldType = field.getType().getName();
                if ("int".equals(fieldType)) {
                    fieldType = "integer";
                }

                if (fieldType.startsWith("java.lang")) {
                    fieldType = fieldType.substring(10).toLowerCase();
                }

                // 基础类型
                if (baseTypes.contains(fieldType)) {
                    indexType.setType(fieldType);
                }
                // 字符
                else if (fieldType.equals("string")) {
                    if (esIndex != null && esIndex.fullIndex()) {
                        indexType.setType("text");
                    } else {
                        indexType.setType("keyword");
                    }
                }
                // BigDecimal
                else if ("java.math.BigDecimal".equals(fieldType)) {
                    indexType.setType("double");
                }
                // 枚举
                else if (field.getType().isEnum()) {
                    indexType.setType("keyword");
                }
                // 数组
                else if (field.getType().isArray() || field.getType().getName().startsWith("java.Util.List")) {
                    indexType.setType("nested");
                }
                // 对象
                else {
                    indexType.setType("object");
                }

                if (esIndex != null) {
                    indexType.setIndex(true);
                }

                if (indexType.getType().equals("object")) {
                    indexType.setIndex(null);
                }

                indexTypes.add(indexType);
            }

            try {

                // 创建索引
                if (!exist) {
                    CreateIndexRequest request = new CreateIndexRequest(indexName);
                    request.mapping(toEsMapping(indexTypes), XContentType.JSON);
                    request.settings(genIndexSettings(), XContentType.JSON);
                    client.indices().create(request, RequestOptions.DEFAULT);
                }
                // 更新索引
                else {
                    PutMappingRequest request = new PutMappingRequest(indexName);
                    request.source(toEsMapping(indexTypes), XContentType.JSON);
                    client.indices().putMapping(request, RequestOptions.DEFAULT);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }
    }

    @Data
    @Accessors
    @AllArgsConstructor
    @NoArgsConstructor
    private static class EsIndexType {
        private String fieldName;
        // text  keyword
        private String type;
        // 默认不被索引
        private Boolean index;
    }

    private String toEsMapping(List<EsIndexType> esIndexTypes) {
        Map<String, Object> main = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();

        for (EsIndexType esIndexType : esIndexTypes) {
            Map<String, Object> type = new HashMap<>();
            type.put("type", esIndexType.getType());
            type.put("index", esIndexType.getIndex());
            properties.put(esIndexType.getFieldName(), type);
        }

        main.put("properties", properties);

        return JsonUtil.toJson(main);
    }


    private String genIndexSettings() {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> inner = new HashMap<>();
        inner.put("max_result_window", Integer.MAX_VALUE);
        map.put("index", inner);
        return JsonUtil.toJson(map);
    }


    @Override
    public void save(DO entity) {
        try {
            String index = entity.getIdValue();
            IndexRequest request = new IndexRequest();

            // 强制刷新作为请求的一部分。这种方式并不适用于索引和查询高吞吐量的场景
            // request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            // request.setRefreshPolicy("true");

            request.index(indexName).id(index);

            String postJson = JsonUtil.toJson(entity);
            request.source(postJson, XContentType.JSON);
            client.index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void batchSave(List<DO> entityList) {
        try {
            BulkRequest batch = new BulkRequest();

            for (DO entity : entityList) {
                IndexRequest request = new IndexRequest();
                request.index(indexName).id(entity.getIdValue());

                String postJson = JsonUtil.toJson(entity);
                request.source(postJson, XContentType.JSON);
                batch.add(request);
            }
            client.bulk(batch, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }


    @Override
    public void deleteById(String id) {
        try {
            DeleteRequest request = new DeleteRequest().index(indexName).id(id);

            client.delete(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteBatchIds(String[] idList) {
        try {
            BulkRequest request = new BulkRequest();
            for (String id : idList) {
                request.add(new DeleteRequest().index(indexName).id(id));
            }
            client.bulk(request, RequestOptions.DEFAULT);

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(EsQueryWrapper queryWrapper) {
        try {

            DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(indexName);
            deleteByQueryRequest.setQuery(queryWrapper.getBoolQueryBuilder());
            client.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public DO selectById(String id) {
        try {
            // 创建请求对象
            GetRequest request = new GetRequest().index(indexName).id(id);
            // 客户端发送请求，获取响应对象
            GetResponse response = client.get(request, RequestOptions.DEFAULT);

            String json = response.getSourceAsString();

            return JsonUtil.fromJson(json, entityClass);

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<DO> selectBatchIds(String[] idList) {
        try {
            SearchRequest request = new SearchRequest();
            request.indices(indexName);

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            //多个id 查找
            IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery();
            idsQueryBuilder.addIds(idList);
            searchSourceBuilder.query(idsQueryBuilder);

            request.source(searchSourceBuilder);

            SearchResponse response = client.search(request, RequestOptions.DEFAULT);

            SearchHits hits = response.getHits();

            List<DO> list = new ArrayList();

            for (SearchHit hit : hits) {
                // 输出每条查询的结果信息
                String json = hit.getSourceAsString();
                list.add(JsonUtil.fromJson(json, entityClass));
            }
            return list;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private SearchHits queryByWrapper(EsQueryWrapper queryWrapper) throws Exception {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indexName);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();


        if (queryWrapper.getOrderBy() != null && queryWrapper.getSortOrder() != null) {
            searchSourceBuilder.sort(queryWrapper.getOrderBy(), queryWrapper.getSortOrder());
        }

        searchSourceBuilder.from(queryWrapper.getStart());
        searchSourceBuilder.size(queryWrapper.getLimit());

        searchSourceBuilder.query(queryWrapper.getBoolQueryBuilder());
        searchRequest.source(searchSourceBuilder);

        log.info("[es query] " + searchSourceBuilder.toString());

        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        SearchHits hits = response.getHits();
        return hits;
    }

    @Override
    public DO selectOne(EsQueryWrapper queryWrapper) {
        try {

            SearchHits hits = queryByWrapper(queryWrapper);

            for (SearchHit hit : hits) {
                // 输出每条查询的结果信息
                String json = hit.getSourceAsString();
                return JsonUtil.fromJson(json, entityClass);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Integer selectCount(EsQueryWrapper queryWrapper) {
        try {
            CountRequest countRequest = new CountRequest();
            countRequest.indices(indexName);
            countRequest.query(queryWrapper.getBoolQueryBuilder());
            CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);

            return (int) countResponse.getCount();

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<DO> selectList(EsQueryWrapper queryWrapper) {

        PageParam param = ThreadPagingUtil.get();

        // 分页查询
        if (param != null && param.getOpenPage() != null && param.getOpenPage()) {

            PageList<DO> pageList = new PageList();

            try {
                // 需要查询总数
                if (param.getNeedCount()) {

                    int start = 0;
                    Integer limit = param.getPageSize();
                    if (limit == null || limit <= 0) {
                        limit = 50;
                    }

                    Integer currentPage = param.getPageIndex();
                    if (currentPage == null) {
                        currentPage = 1;
                    }

                    if (currentPage > 1) {
                        start = (currentPage - 1) * limit;
                    }

                    queryWrapper.setStart(start);
                    queryWrapper.setLimit(limit);

                    // ---- start select -----
                    SearchHits hits = queryByWrapper(queryWrapper);


                    for (SearchHit hit : hits) {
                        String json = hit.getSourceAsString();
                        pageList.add(JsonUtil.fromJson(json, entityClass));
                    }

                    int count = selectCount(queryWrapper);

                    // ----  page list

                    int pageSize = limit;

                    int left = count % pageSize;
                    int totalPage = left == 0 ? count / pageSize : count / pageSize + 1;

                    pageList.setTotalSize(count);
                    pageList.setPageIndex(currentPage);

                    pageList.setHasNext(currentPage < totalPage ? true : false);
                    pageList.setHasPre(currentPage > 1 ? true : false);
                    pageList.setPageSize(pageSize);
                    pageList.setTotalPage(totalPage);

                }

                // 直接根据 offset 查询
                else {

                    queryWrapper.setStart(param.getStart());
                    queryWrapper.setLimit(param.getLimit());

                    // ---- start select -----
                    SearchHits hits = queryByWrapper(queryWrapper);

                    for (SearchHit hit : hits) {
                        String json = hit.getSourceAsString();
                        pageList.add(JsonUtil.fromJson(json, entityClass));
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                ThreadPagingUtil.clear();
            }

            return pageList;

        }
        // 直接查询，默认 limit
        else {

            try {

                // 默认最大查 1000 条
                queryWrapper.setStart(0);
                queryWrapper.setLimit(1000);

                SearchHits hits = queryByWrapper(queryWrapper);

                List<DO> list = new ArrayList();

                for (SearchHit hit : hits) {
                    // 输出每条查询的结果信息
                    String json = hit.getSourceAsString();
                    list.add(JsonUtil.fromJson(json, entityClass));
                }

                return list;

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new BizException(e);
            }
        }

    }


    // 删除索引
    public void dropIndex() {
        try {
            // 删除索引 - 请求对象
            DeleteIndexRequest request = new DeleteIndexRequest(indexName);
            // 发送请求，获取响应
            AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);

            log.info("dropIndex 操作结果: " + response.isAcknowledged());

            initedIndexNames.remove(indexName);

            hasInit.set(false);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    public Map<String, Long> countWithGroupBy(EsQueryWrapper queryWrapper, String groupField) {
        try {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices(indexName);

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder
                    .query(queryWrapper.getBoolQueryBuilder())
                    .aggregation(AggregationBuilders.terms("group_query").field(groupField))   // 用于设置聚合处理
                    .size(0);
            // 3.为请求对象配置查询对象
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            // 5.处理聚合结果
            Aggregations aggregations = searchResponse.getAggregations();
            // 根据字段类型,转移
            ParsedStringTerms price_group = aggregations.get("group_query");
            // 解析桶里的内容
            List<? extends Terms.Bucket> buckets = price_group.getBuckets();

            Map<String, Long> result = new HashMap<>();
            for (Terms.Bucket bucket : buckets) {
                result.put(bucket.getKey().toString(), bucket.getDocCount());
            }

            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }


}
