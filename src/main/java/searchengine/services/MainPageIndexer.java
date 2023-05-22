package searchengine.services;

import lombok.RequiredArgsConstructor;
import searchengine.config.Configuration;
import searchengine.model.Site;
import searchengine.model.StatusIndexing;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.concurrent.ForkJoinPool;

@RequiredArgsConstructor
public class MainPageIndexer implements Runnable{

    private final searchengine.config.Site indexerSite;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final Configuration configuration;

    private void setSitePagesToBase(Site site) throws InterruptedException {
        if (!Thread.interrupted()) {

            String mainUrl = indexerSite.getUrl();
            ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
            forkJoinPool.invoke(new SiteIndexer(mainUrl + "/",
                    pageRepository,
                    siteRepository,
                    site,
                    configuration));

        } else throw new InterruptedException();
    }

    @Override
    public void run() {

        if (siteRepository.findByUrl(indexerSite.getUrl()) != null) {
                delDataSite();
             }
        try {
        Site site = Site.builder()
                .name(indexerSite.getName())
                .url(indexerSite.getUrl())
                .status(StatusIndexing.INDEXING)
                .statusTime(LocalDateTime.now()).build();
        siteRepository.flush();
        siteRepository.save(site);

            setSitePagesToBase(site);

            site.setStatusTime(LocalDateTime.now());
            site.setStatus(StatusIndexing.INDEXED);
            siteRepository.save(site);

        } catch (InterruptedException e) {
            Site site = Site.builder()
                    .lastError("Остановлено пользователем")
                    .status(StatusIndexing.FAILED)
                    .statusTime(LocalDateTime.now()).build();
            siteRepository.flush();
            siteRepository.save(site);
        }
    }

    private void delDataSite() {
        Site sitePage = siteRepository.findByUrl(indexerSite.getUrl());
        siteRepository.delete(sitePage);
    }
}
