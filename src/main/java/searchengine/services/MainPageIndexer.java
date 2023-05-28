package searchengine.services;

import lombok.RequiredArgsConstructor;
import searchengine.config.Configuration;
import searchengine.helpers.IndexSet;
import searchengine.helpers.LemmasSet;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;

@RequiredArgsConstructor
public class MainPageIndexer implements Runnable{

    private final searchengine.config.Site indexerSite;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final IndexCreator    indexCreator;
    private final LemmaService lemmaService;
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
            Site site = new Site();
            site.setUrl(indexerSite.getUrl());
            site.setName(indexerSite.getName());
            site.setStatus(StatusIndexing.INDEXING);
            site.setStatusTime(LocalDateTime.now());

        siteRepository.flush();
        siteRepository.save(site);

            setSitePagesToBase(site);

            site.setStatusTime(LocalDateTime.now());
            site.setStatus(StatusIndexing.INDEXED);
            siteRepository.save(site);

            setLemmasPageToBase();
            indexingDataFromSite();

        } catch (InterruptedException ex) {

            Site site = new Site();
            site.setLastError("Остановлено пользователем");
            site.setStatus(StatusIndexing.FAILED);
            site.setStatusTime(LocalDateTime.now());

            siteRepository.flush();
            siteRepository.save(site);
        }
    }

    private void indexingDataFromSite() throws InterruptedException {
        if (!Thread.interrupted()) {
            Site site = siteRepository.findByUrl(indexerSite.getUrl());

            List<IndexSet> indexPageSetList = indexCreator.getIndexPageSet(site);
            List<SearchIndex> indexList = new CopyOnWriteArrayList<>();
            site.setStatusTime(LocalDateTime.now());
            for (IndexSet indexPageSet : indexPageSetList) {
                Page page = pageRepository.getById(indexPageSet.getPageID());
                Lemma lemma = lemmaRepository.getById(indexPageSet.getLemmaID());
                indexList.add(new SearchIndex(page, lemma, indexPageSet.getRank()));
            }
            indexRepository.flush();
            indexRepository.saveAll(indexList);
            site.setStatusTime(LocalDateTime.now());
            site.setStatus(StatusIndexing.INDEXED);
            siteRepository.save(site);
        } else {
            throw new InterruptedException();
        }
    }

    private void setLemmasPageToBase() {
        if (!Thread.interrupted()) {
            Site sitePage = siteRepository.findByUrl(indexerSite.getUrl());
            sitePage.setStatusTime(LocalDateTime.now());
            List<LemmasSet> LemmaPageSetList = lemmaService.getLemmasPageSet();
            List<Lemma> lemmaList = new CopyOnWriteArrayList<>();
            for (LemmasSet lemmaPageSet : LemmaPageSetList) {
                lemmaList.add(new Lemma(lemmaPageSet.getLemma(), lemmaPageSet.getFrequency(), sitePage));
            }
            lemmaRepository.flush();
            lemmaRepository.saveAll(lemmaList);
        } else {
            throw new RuntimeException();
        }
    }

    private void delDataSite() {
        Site sitePage = siteRepository.findByUrl(indexerSite.getUrl());
        siteRepository.delete(sitePage);
    }
}
