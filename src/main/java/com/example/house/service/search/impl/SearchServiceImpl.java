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
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.mustache.SearchTemplateRequestBuilder;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private TransportClient transportClient;
    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public void index(Long houseId) {
        House house = houseMapper.findOne(houseId);
        if(house == null) {
            logger.error("Index house {} dose not exist!", houseId);
        }
        HouseIndexTemplate houseIndexTemplate = new HouseIndexTemplate();
        modelMapper.map(house, houseIndexTemplate);

        HouseDetail houseDetail = houseDetailMapper.findByHouseId(houseId);
        if(houseDetail == null) {
            //exception
        }
        modelMapper.map(houseDetail, houseIndexTemplate);
        List<HouseTag> houseTags = houseTagMapper.findAllByHouseId(houseId);
        if(houseTags != null && !houseTags.isEmpty()) {
            List<String> houseTagsString = new ArrayList<>();
            houseTags.forEach(houseTag -> houseTagsString.add(houseTag.getName()));
            houseIndexTemplate.setTags(houseTagsString);
        }
        SearchRequestBuilder searchRequestBuilder = transportClient.prepareSearch(INDEX_NAME).setSearchType(INDEX_TYPE)
                .setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId));
        SearchResponse searchResponse = searchRequestBuilder.get();
        long totalHit = searchResponse.getHits().getTotalHits().value;
        boolean success;
        if(totalHit == 0) {
            success = create(houseIndexTemplate);
        } else if(totalHit == 1) {
            String esId = searchResponse.getHits().getAt(0).getId();
            success = update(esId, houseIndexTemplate);
        } else {
            success = deleteAndCreate(totalHit, houseIndexTemplate);
        }
        if(success) {
            logger.debug("");
        }
    }

    private boolean create(HouseIndexTemplate houseIndexTemplate) {
        try {
            IndexResponse indexResponse = transportClient.prepareIndex(INDEX_NAME, INDEX_TYPE)
                    .setSource(objectMapper.writeValueAsBytes(houseIndexTemplate)).get();
            logger.debug("Create index with house: " + houseIndexTemplate.getHouseId());
            if(indexResponse.status() == RestStatus.CREATED)
                return true;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return false;

    }
    private boolean update(String esId, HouseIndexTemplate houseIndexTemplate) {
        try {
            UpdateResponse indexResponse = transportClient.prepareUpdate(INDEX_NAME, INDEX_TYPE, esId)
                    .setDoc(objectMapper.writeValueAsBytes(houseIndexTemplate)).get();
            logger.debug("Create index with house: " + houseIndexTemplate.getHouseId());
            if(indexResponse.status() == RestStatus.OK)
                return true;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return false;
    }
    private boolean deleteAndCreate(long totalHit, HouseIndexTemplate houseIndexTemplate) {
        DeleteByQueryRequestBuilder deleteByQueryRequestBuilder = new DeleteByQueryRequestBuilder(
                transportClient, DeleteByQueryAction.INSTANCE).filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseIndexTemplate.getHouseId())).source(INDEX_NAME);
        BulkByScrollResponse bulkByScrollResponse = deleteByQueryRequestBuilder.get();
        long delete = bulkByScrollResponse.getDeleted();
        if(delete != totalHit)
            return false;
        else return create(houseIndexTemplate);
    }

    @Override
    public void remove(Long houseId) {
    }

}























