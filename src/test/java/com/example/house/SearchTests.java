package com.example.house;


import com.example.house.service.search.ISearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SearchTests {

    @Autowired
    private ISearchService searchService;

    @Test
    void index() {
        searchService.index(16L);
    }

    @Test
    void remove() {
        searchService.remove(16L);
    }
}
