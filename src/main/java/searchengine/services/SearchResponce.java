package searchengine.services;


import lombok.Getter;
import lombok.Setter;
import searchengine.helpers.SearchDataSet;

import java.util.List;

    @Setter
    @Getter
    public class SearchResponce {
        private boolean result;
        private int count;
        private List<SearchDataSet> data;

        public SearchResponce(boolean result){
            this.result = result;
        }

        public SearchResponce(boolean result, int count, List<SearchDataSet> data) {
            this.result = result;
            this.count = count;
            this.data = data;
        }
    }
