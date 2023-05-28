package searchengine.services;

import org.springframework.http.ResponseEntity;

public interface IndexingService {
    ResponseEntity<Object> allIndexingSites();
    ResponseEntity<Object> stopIndexing();

    ResponseEntity<Object> indexingPage(String url);
}
