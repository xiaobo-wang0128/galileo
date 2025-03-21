package org.armada.galileo.es.query;


import lombok.Getter;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.sort.SortOrder;

import java.util.Collection;

/**
 * @author xiaobo
 * @date 2023/2/8 11:33
 */
@Getter
public class EsQueryWrapper<T> {

    private BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

    private String orderBy;

    private SortOrder sortOrder;

    private int start = 0;

    private int limit = 50;


    public void setStart(int start) {
        this.start = start;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public EsQueryWrapper sort(String orderBy, SortOrder sortOrder) {
        this.orderBy = orderBy;
        this.sortOrder = sortOrder;
        return this;
    }

    public EsQueryWrapper eq(String column, Object val) {
        boolQueryBuilder.must(QueryBuilders.termQuery(column, val));
        return this;
    }

    public EsQueryWrapper ne(String column, Object val) {
        boolQueryBuilder.mustNot(QueryBuilders.termQuery(column, val));
        return this;
    }

    public EsQueryWrapper gt(String column, Object val) {
        boolQueryBuilder.must(QueryBuilders.rangeQuery(column).gt(val));
        return this;
    }

    public EsQueryWrapper ge(String column, Object val) {
        boolQueryBuilder.must(QueryBuilders.rangeQuery(column).gte(val));
        return this;
    }

    public EsQueryWrapper lt(String column, Object val) {
        boolQueryBuilder.must(QueryBuilders.rangeQuery(column).lt(val));
        return this;
    }

    public EsQueryWrapper le(String column, Object val) {
        boolQueryBuilder.must(QueryBuilders.rangeQuery(column).lte(val));
        return this;
    }

    public EsQueryWrapper like(String column, Object val) {
        boolQueryBuilder.must(QueryBuilders.matchQuery(column, val).operator(Operator.AND));
        return this;
    }

    public EsQueryWrapper notLike(String column, Object val) {
        boolQueryBuilder.mustNot(QueryBuilders.matchQuery(column, val).operator(Operator.AND));
        return this;
    }


    public EsQueryWrapper between(String column, Object val1, Object val2) {
        boolQueryBuilder.must(QueryBuilders.rangeQuery(column).gte(val1).lte(val2));
        return this;
    }

    public EsQueryWrapper notBetween(String column, Object val1, Object val2) {
        boolQueryBuilder.mustNot(QueryBuilders.rangeQuery(column).gte(val1).lte(val2));
        return this;
    }


    public EsQueryWrapper in(String column, Collection<?> coll) {
        boolQueryBuilder.must(QueryBuilders.termsQuery(column, coll));
        return this;
    }

    public EsQueryWrapper notIn(String column, Collection<?> coll) {
        boolQueryBuilder.mustNot(QueryBuilders.termsQuery(column, coll));
        return this;
    }


}
