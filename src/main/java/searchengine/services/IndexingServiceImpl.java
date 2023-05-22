package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.Responses.BadResponse;
import searchengine.Responses.SimpleResponse;
import searchengine.config.Configuration;
import searchengine.config.SitesList;

import searchengine.model.Site;
import searchengine.model.StatusIndexing;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sitesList;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;

    private static final String LAST_ERROR_MESSAGE = "Остановлено пользователем";
    private ExecutorService executorService;
    private final Configuration configuration;
    @Override
    public ResponseEntity<Object> allIndexingSites() {
            if (checkIndexingStatus())
               return new ResponseEntity<>(new BadResponse(false, "Индексация не стартовала"), HttpStatus.BAD_REQUEST);

            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (searchengine.config.Site indexedSites : sitesList.getSites()) {

            executorService.submit(
                new MainPageIndexer(indexedSites, pageRepository,
                        siteRepository,
                        configuration));
       }
        executorService.shutdown();
       return new ResponseEntity<>(new SimpleResponse(true), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> stopIndexing() {
        if (checkIndexingStatus()){
            executorService.shutdownNow();
            return new ResponseEntity<>(new SimpleResponse(true), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new BadResponse(false, "Индексация не запущена"), HttpStatus.BAD_REQUEST);
        }
    }

    private boolean checkIndexingStatus() {
        List<Site> siteList = siteRepository.findAll();
        for (Site site : siteList) {
            if (site.getStatus() == StatusIndexing.INDEXING) {
                return true;
            }
        }
        return false;
    }

}
