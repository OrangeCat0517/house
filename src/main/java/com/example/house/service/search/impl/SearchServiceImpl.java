package com.example.house.service.search.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.house.base.HouseSort;
import com.example.house.base.RentValueBlock;
import com.example.house.base.ServiceMultiResult;
import com.example.house.base.ServiceResult;
import com.example.house.domain.House;
import com.example.house.domain.HouseDetail;
import com.example.house.domain.HouseTag;
import com.example.house.form.RentSearch;
import com.example.house.mapper.HouseDetailMapper;
import com.example.house.mapper.HouseMapper;
import com.example.house.mapper.HouseTagMapper;
import com.example.house.service.search.HouseIndexKey;
import com.example.house.service.search.HouseIndexMessage;
import com.example.house.service.search.HouseIndexTemplate;
import com.example.house.service.search.ISearchService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.gson.JsonSerializer;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class SearchServiceImpl implements ISearchService {
    private static final Logger logger = LoggerFactory.getLogger(ISearchService.class);

    private static final String INDEX_NAME = "house";
    private static final String INDEX_TYPE = "house";
    private static final String INDEX_TOPIC = "house";

    @Autowired
    private HouseMapper houseMapper;
    @Autowired
    private HouseDetailMapper houseDetailMapper;
    @Autowired
    private HouseTagMapper houseTagMapper;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = INDEX_TOPIC)
    private void handleMessage(String content) {
        try {
            HouseIndexMessage houseIndexMessage
                    = objectMapper.readValue(content, HouseIndexMessage.class);
            switch (houseIndexMessage.getOperation()) {
                case HouseIndexMessage.INDEX -> this.createOrUpdateIndex(houseIndexMessage);
                case HouseIndexMessage.REMOVE -> this.removeIndex(houseIndexMessage);
                default -> logger.warn("not support message content");
            }
        } catch (JsonProcessingException e) {
            logger.error("cannot parse json for " + content, e);
        }
    }

    //主要为了处理kafka
    private void removeIndex(HouseIndexMessage houseIndexMessage) {
        Long houseId = houseIndexMessage.getHouseId();
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(INDEX_NAME);
        deleteByQueryRequest.setQuery(
                QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId));
        BulkByScrollResponse bulkByScrollResponse = null;
        try {
            bulkByScrollResponse =
                    restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        long delete = bulkByScrollResponse.getDeleted();
        logger.debug("Delete total " + delete);

        if (delete <= 0) { //quartz
            logger.warn("Did not remove data from es for response: " + bulkByScrollResponse);
            // 重新加入消息队列
            this.remove(houseId, houseIndexMessage.getRetry() + 1);
        }
    }

    //主要为了处理kafka
    private void createOrUpdateIndex(HouseIndexMessage houseIndexMessage) {
        long houseId = houseIndexMessage.getHouseId();
        House house = houseMapper.findOne(houseId);
        if (house == null) {
            logger.error("Index house {} dose not exist!", houseId);
            this.index(houseId, houseIndexMessage.getRetry() + 1);
        }
        HouseIndexTemplate houseIndexTemplate = new HouseIndexTemplate();
        modelMapper.map(house, houseIndexTemplate);

        HouseDetail houseDetail = houseDetailMapper.findByHouseId(houseId);
        if (houseDetail == null) {
            //exception
        }
        modelMapper.map(houseDetail, houseIndexTemplate);
        List<HouseTag> houseTags = houseTagMapper.findAllByHouseId(houseId);
        if (houseTags != null && !houseTags.isEmpty()) {
            List<String> houseTagsString = new ArrayList<>();
            houseTags.forEach(houseTag -> houseTagsString.add(houseTag.getName()));
            houseIndexTemplate.setTags(houseTagsString);
        }
//        SearchRequestBuilder searchRequestBuilder = transportClient.prepareSearch(INDEX_NAME).setSearchType(INDEX_TYPE)
//                .setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId)); //ES6

        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        //SearchType searchType = SearchType.fromString(INDEX_TYPE);
        QueryBuilder queryBuilder = QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        searchRequest.source(sourceBuilder);
        //searchRequest.searchType(searchType);

        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        long totalHit = searchResponse.getHits().getTotalHits().value;
        boolean success;
        if (totalHit == 0) {
            success = create(houseIndexTemplate);
        } else if (totalHit == 1) {
            String esId = searchResponse.getHits().getAt(0).getId();
            success = update(esId, houseIndexTemplate);
        } else {
            success = deleteAndCreate(totalHit, houseIndexTemplate);
        }
        if (success) {
            logger.debug("");
        }
    }

    @Override
    public void index(Long houseId) {
        this.index(houseId, 0);
    }

    @Override
    public void remove(Long houseId) {
        this.remove(houseId, 0);
    }

    @Override
    public ServiceMultiResult<Long> query(RentSearch rentSearch) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        boolQuery.filter( //where
                QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, rentSearch.getCityEnName())
        );

        if (rentSearch.getRegionEnName() != null && !"*".equals(rentSearch.getRegionEnName())) {
            boolQuery.filter(
                    QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME, rentSearch.getRegionEnName())
            );
        }

        RentValueBlock area = RentValueBlock.matchArea(rentSearch.getAreaBlock());
        if (!RentValueBlock.ALL.equals(area)) {
            // where erea <= getmax and area >= min
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(HouseIndexKey.AREA);
            if (area.getMax() > 0) {
                rangeQueryBuilder.lte(area.getMax());
            }
            if (area.getMin() > 0) {
                rangeQueryBuilder.gte(area.getMin());
            }
            boolQuery.filter(rangeQueryBuilder);
        }

        RentValueBlock price = RentValueBlock.matchPrice(rentSearch.getPriceBlock());
        if (!RentValueBlock.ALL.equals(price)) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(HouseIndexKey.PRICE);
            if (price.getMax() > 0) {
                rangeQuery.lte(price.getMax());
            }
            if (price.getMin() > 0) {
                rangeQuery.gte(price.getMin());
            }
            boolQuery.filter(rangeQuery);
        }

        if (rentSearch.getDirection() > 0) {
            boolQuery.filter(
                    QueryBuilders.termQuery(HouseIndexKey.DIRECTION, rentSearch.getDirection())
            );
        }

        if (rentSearch.getRentWay() > -1) {
            boolQuery.filter(
                    QueryBuilders.termQuery(HouseIndexKey.RENT_WAY, rentSearch.getRentWay())
            );
        }
        boolQuery.must(
                QueryBuilders.multiMatchQuery(rentSearch.getKeywords(),
                        HouseIndexKey.TITLE,
                        HouseIndexKey.TRAFFIC,
                        HouseIndexKey.DISTRICT,
                        HouseIndexKey.ROUND_SERVICE,
                        HouseIndexKey.SUBWAY_LINE_NAME,
                        HouseIndexKey.SUBWAY_STATION_NAME
                ));

        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        QueryBuilder queryBuilder = QueryBuilders.boolQuery();

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.sort(HouseSort.getSortKey(rentSearch.getOrderBy()), SortOrder.fromString(rentSearch.getOrderDirection()));
        sourceBuilder.from(rentSearch.getStart());
        sourceBuilder.size(rentSearch.getSize());
        sourceBuilder.fetchSource(HouseIndexKey.HOUSE_ID, null);
        sourceBuilder.query(queryBuilder);
        searchRequest.source(sourceBuilder);
        List<Long> houseIds = new ArrayList<>();
        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            for (SearchHit searchHit : searchResponse.getHits().getHits()) {
                System.out.println(searchHit.getSourceAsMap());
            }
            if (searchResponse.status() != RestStatus.OK) {
                logger.warn("Search status is no ok for " + queryBuilder);
                return new ServiceMultiResult<>(0, houseIds);
            }
            for (SearchHit hit : searchResponse.getHits()) {
                System.out.println(hit.getSourceAsMap());
                houseIds.add(Long.parseLong(String.valueOf(hit.getSourceAsMap().get(HouseIndexKey.HOUSE_ID))));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ServiceMultiResult(searchResponse.getHits().getTotalHits().value, houseIds);
    }

//    @Override
//    public ServiceResult<List<String>> suggest(String prefix) {
//        CompletionSuggestionBuilder suggestion = SuggestBuilders.completionSuggestion("suggest").prefix(prefix).size(5);
//
//        SuggestBuilder suggestBuilder = new SuggestBuilder();
//        suggestBuilder.addSuggestion("autocomplete", suggestion);
//
//        SearchRequestBuilder requestBuilder = this.esClient.prepareSearch(INDEX_NAME)
//                .setTypes(INDEX_TYPE)
//                .suggest(suggestBuilder);
//        logger.debug(requestBuilder.toString());
//
//        SearchResponse response = requestBuilder.get();
//        Suggest suggest = response.getSuggest();
//        if (suggest == null) {
//            return ServiceResult.of(new ArrayList<>());
//        }
//        Suggest.Suggestion result = suggest.getSuggestion("autocomplete");
//
//        int maxSuggest = 0;
//        Set<String> suggestSet = new HashSet<>();
//
//        for (Object term : result.getEntries()) {
//            if (term instanceof CompletionSuggestion.Entry) {
//                CompletionSuggestion.Entry item = (CompletionSuggestion.Entry) term;
//
//                if (item.getOptions().isEmpty()) {
//                    continue;
//                }
//
//                for (CompletionSuggestion.Entry.Option option : item.getOptions()) {
//                    String tip = option.getText().string();
//                    if (suggestSet.contains(tip)) {
//                        continue;
//                    }
//                    suggestSet.add(tip);
//                    maxSuggest++;
//                }
//            }
//
//            if (maxSuggest > 5) {
//                break;
//            }
//        }
//        List<String> suggests = Lists.newArrayList(suggestSet.toArray(new String[]{}));
//        return ServiceResult.of(suggests);
//    }
//
//    @Override
//    public ServiceResult<Long> aggregateDistrictHouse(String cityEnName, String regionEnName, String district) {
//        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
//                .filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, cityEnName))
//                .filter(QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME, regionEnName))
//                .filter(QueryBuilders.termQuery(HouseIndexKey.DISTRICT, district));
//
//        SearchRequestBuilder requestBuilder = this.esClient.prepareSearch(INDEX_NAME)
//                .setTypes(INDEX_TYPE)
//                .setQuery(boolQuery)
//                .addAggregation(
//                        AggregationBuilders.terms(HouseIndexKey.AGG_DISTRICT)
//                                .field(HouseIndexKey.DISTRICT)
//                ).setSize(0);
//
//        logger.debug(requestBuilder.toString());
//
//        SearchResponse response = requestBuilder.get();
//        if (response.status() == RestStatus.OK) {
//            Terms terms = response.getAggregations().get(HouseIndexKey.AGG_DISTRICT);
//            if (terms.getBuckets() != null && !terms.getBuckets().isEmpty()) {
//                return ServiceResult.of(terms.getBucketByKey(district).getDocCount());
//            }
//        } else {
//            logger.warn("Failed to Aggregate for " + HouseIndexKey.AGG_DISTRICT);
//
//        }
//        return ServiceResult.of(0L);
//    }

    private void index(Long houseId, int retry) {
        if(retry> HouseIndexMessage.MAX_RETRY) {
            logger.error("retry index times over 3 for house");
        }
        HouseIndexMessage houseIndexMessage
                = new HouseIndexMessage(houseId, HouseIndexMessage.INDEX, retry);
        try {
            kafkaTemplate.send(INDEX_TOPIC, objectMapper.writeValueAsString(houseIndexMessage));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private boolean create(HouseIndexTemplate houseIndexTemplate) {
        try {
            IndexRequest indexRequest = new IndexRequest(INDEX_NAME)
                    .source(objectMapper.writeValueAsBytes(houseIndexTemplate), XContentType.JSON);
            IndexResponse response =
                    restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

            if (response.status() == RestStatus.CREATED)
                return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean update(String esId, HouseIndexTemplate houseIndexTemplate) {
        try {
            UpdateRequest updateRequest = new UpdateRequest(INDEX_NAME, esId)
                            .doc(objectMapper.writeValueAsBytes(houseIndexTemplate), XContentType.JSON);
            UpdateResponse updateResponse
                    = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);

            logger.debug("Create index with house: " + houseIndexTemplate.getHouseId());
            if (updateResponse.status() == RestStatus.OK)
                return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void remove(Long houseId, int retry) {
        if (retry > HouseIndexMessage.MAX_RETRY) {
            logger.error("Retry remove times over 3 for house: " + houseId + " Please check it!");
            return;
        }

        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.REMOVE, retry);
        try {
            this.kafkaTemplate.send(INDEX_TOPIC, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            logger.error("Cannot encode json for " + message, e);
        }
    }


    private boolean deleteAndCreate(long totalHit, HouseIndexTemplate houseIndexTemplate) {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(INDEX_NAME);
        deleteByQueryRequest.setQuery(
                QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseIndexTemplate.getHouseId()));
        BulkByScrollResponse bulkByScrollResponse = null;
        try {
            bulkByScrollResponse =
                    restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        long delete = bulkByScrollResponse.getDeleted();
        if (delete != totalHit)
            return false;
        else return create(houseIndexTemplate);
    }

}























