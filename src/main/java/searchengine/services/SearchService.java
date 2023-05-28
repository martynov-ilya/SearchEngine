package searchengine.services;

import searchengine.helpers.SearchDataSet;

import java.util.List;

public interface SearchService {
    List<SearchDataSet> allSiteSearch(String text, int offset, int limit);
    List<SearchDataSet> siteSearch(String searchText, String url, int offset, int limit);
}
