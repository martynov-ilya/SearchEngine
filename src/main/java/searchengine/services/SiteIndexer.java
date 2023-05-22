package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.config.Configuration;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;


public class SiteIndexer extends RecursiveAction {

    private final String url;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final Site site;
    private final Configuration configuration;

    private CopyOnWriteArraySet<String> allLinks;

    public SiteIndexer(String url, PageRepository pageRepository, SiteRepository siteRepository, Site site, Configuration configuration, CopyOnWriteArraySet<String> allLinks) {
        this.url = url;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.site = site;
        this.configuration = configuration;
        this.allLinks = allLinks;
    }

    public SiteIndexer(String url, PageRepository pageRepository, SiteRepository siteRepository, Site site, Configuration configuration) {
        this.url = url;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.site = site;
        this.configuration = configuration;
        this.allLinks = new CopyOnWriteArraySet<>();
    }

    @Override
    protected void compute() {
        try {
            Thread.sleep(150);
            Document document = Jsoup.connect(url).userAgent(configuration.getUserAgent()).referrer(configuration.getReferrer()).get();

            String html = document.outerHtml();
            Connection.Response response = document.connection().response();

            int statusCode = response.statusCode();

            saveDataToRepository(statusCode, html);

            Elements elements = document.select("body").select("a");
            List<SiteIndexer> taskLinkJoin = new ArrayList<>();
            for (Element element : elements) {
                String link = element.attr("abs:href");
                if (link.startsWith(element.baseUri()) && !link.equals(element.baseUri()) && !link.contains("#") && !link.contains(".pdf") && !link.contains(".jpg") && !link.contains(".JPG") && !link.contains(".png") && !allLinks.contains(link)
                       && pageRepository.findByPathAndSiteId(link.replaceAll(site.getUrl(), ""), site) == null ){
                    allLinks.add(link);
                    SiteIndexer siteIndexer = new SiteIndexer(link,
                            pageRepository,
                            siteRepository,
                            site,
                            configuration,
                            allLinks);
                    siteIndexer.fork();
                    taskLinkJoin.add(siteIndexer);
                }
            }
            taskLinkJoin.forEach(ForkJoinTask::join);
        } catch (Exception e) {
            saveDataToRepository(500, "" );
        }
    }

    private void saveDataToRepository(int statusCode, String htmlData)
    {
        if (pageRepository.findByPathAndSiteId(url.replaceAll(site.getUrl(), ""), site) != null){
            return;
        }

        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
        Page pageEntity = Page.builder()
                .code(statusCode)
                .siteId(site)
                .path(url.replaceAll(site.getUrl(), ""))
                .content(htmlData)
                .build();
        pageRepository.save(pageEntity);
    }
}
