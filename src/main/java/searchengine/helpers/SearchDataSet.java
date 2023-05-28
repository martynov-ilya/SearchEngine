package searchengine.helpers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchDataSet {

        private String address;
        private String site;
        private String siteName;
        private String uri;
        private String title;
        private String snippet;
        private Float relevance;

        public SearchDataSet(String site, String siteName, String uri,
                                String title, String snippet, Float relevance) {
            this.address = address;
            this.site = site;
            this.siteName = siteName;
            this.uri = uri;
            this.title = title;
            this.snippet = snippet;
            this.relevance = relevance;
        }
    }
