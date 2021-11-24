package com.example.house.service.search.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.house.domain.House;
import com.example.house.domain.HouseDetail;
import com.example.house.domain.HouseTag;
import com.example.house.mapper.HouseDetailMapper;
import com.example.house.mapper.HouseMapper;
import com.example.house.mapper.HouseTagMapper;
import com.example.house.service.search.HouseIndexKey;
import com.example.house.service.search.HouseIndexMessage;
import com.example.house.service.search.HouseIndexTemplate;
import com.example.house.service.search.ISearchService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.ActionType;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.mustache.SearchTemplateRequestBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
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

    private static final String INDEX_TOPIC = "house_build";

    @Autowired
    private HouseMapper houseMapper;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private HouseDetailMapper houseDetailMapper;
    @Autowired
    private HouseTagMapper houseTagMapper;
//    @Autowired
//    private TransportClient transportClient;

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = INDEX_TOPIC)
    private void handleMessage(String content) {
        try {
            HouseIndexMessage houseIndexMessage = objectMapper.readValue(content, HouseIndexMessage.class);
            switch (houseIndexMessage.getOperation()) {
                case HouseIndexMessage.INDEX:
                    this.createOrUpdateIndex(houseIndexMessage);
                    break;
                case HouseIndexMessage.REMOVE:
                    this.removeIndex(houseIndexMessage);
                    break;
                default:
                    logger.warn("not support message content");
                    break;
            }
        } catch (JsonProcessingException e) {
            logger.error("cannot parse json for " + content, e);
        }
    }

    //主要为了处理kafka
    private void removeIndex(HouseIndexMessage houseIndexMessage) {

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
        SearchType searchType = SearchType.fromString(INDEX_TYPE);
        QueryBuilder queryBuilder = QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        searchRequest.source(sourceBuilder);
        searchRequest.searchType(searchType);

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

    private void index(Long houseId, int retry) {
        if(retry> HouseIndexMessage.MAX_RETRY) {
            logger.error("retry index times over 3 for house");
        }
        HouseIndexMessage houseIndexMessage = new HouseIndexMessage(houseId, HouseIndexMessage.INDEX, retry);
        try {
            this.kafkaTemplate.send(INDEX_TOPIC, objectMapper.writeValueAsString(houseIndexMessage));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private boolean create(HouseIndexTemplate houseIndexTemplate) {
        try {
//            IndexResponse indexResponse = transportClient.prepareIndex(INDEX_NAME, INDEX_TYPE)
//                    .setSource(objectMapper.writeValueAsBytes(houseIndexTemplate)).get();

            IndexRequest indexRequest = new IndexRequest(INDEX_NAME)
                    .source(objectMapper.writeValueAsBytes(houseIndexTemplate));
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
//            UpdateResponse indexResponse = transportClient.prepareUpdate(INDEX_NAME, INDEX_TYPE, esId)
//                    .setDoc(objectMapper.writeValueAsBytes(houseIndexTemplate)).get();  //ES 6

            UpdateRequest updateRequest = new UpdateRequest(INDEX_NAME, esId)
                            .doc(objectMapper.writeValueAsBytes(houseIndexTemplate));
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

    private boolean deleteAndCreate(long totalHit, HouseIndexTemplate houseIndexTemplate) {
//        DeleteByQueryRequestBuilder deleteByQueryRequestBuilder = new DeleteByQueryRequestBuilder(
//                transportClient, DeleteByQueryAction.INSTANCE)
//                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseIndexTemplate.getHouseId()))
//                .source(INDEX_NAME);   //ES 6
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(INDEX_NAME);
        deleteByQueryRequest.setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseIndexTemplate.getHouseId()));
        BulkByScrollResponse bulkByScrollResponse = null;
        try {
            bulkByScrollResponse = restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        long delete = bulkByScrollResponse.getDeleted();
        if (delete != totalHit)
            return false;
        else return create(houseIndexTemplate);
    }

    @Override
    public void remove(Long houseId) {
    }

}























