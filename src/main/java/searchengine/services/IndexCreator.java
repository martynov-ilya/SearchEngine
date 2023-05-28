package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.helpers.HtmlCleaner;
import searchengine.helpers.IndexSet;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@RequiredArgsConstructor
public class IndexCreator {
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final LemmaSearcher lemmaSearcher;

    public List<IndexSet> getIndexPageSet(Site site) {
        List<IndexSet> indexListSet = new ArrayList<>();

        Iterable<Page> pageList = pageRepository.findBySiteId(site);
        List<Lemma> lemmaList = lemmaRepository.findBySitePageId(site);

        for (Page page : pageList) {
            if (page.getCode() < 400) {
                int pageId = page.getId();
                String content = page.getContent();

                String title = HtmlCleaner.clear(content, "title");
                String body = HtmlCleaner.clear(content, "body");
                HashMap<String, Integer> titleList  = lemmaSearcher.getMapLemmas(title);
                HashMap<String, Integer> bodyList = lemmaSearcher.getMapLemmas(body);

                for (Lemma lemma : lemmaList) {
                    int lemmaId = lemma.getId();
                    String theExactLemma = lemma.getLemma();
                    if (titleList.containsKey(theExactLemma) || bodyList.containsKey(theExactLemma)) {
                        float wholeRank = 0.0F;
                        if (titleList.get(theExactLemma) != null) {
                            Float titleRank = Float.valueOf(titleList.get(theExactLemma));
                            wholeRank += titleRank;
                        }
                        if (bodyList.get(theExactLemma) != null) {
                            float bodyRank = (float) (bodyList.get(theExactLemma) * 0.8);
                            wholeRank += bodyRank;
                        }
                        indexListSet.add(new IndexSet(pageId, lemmaId, wholeRank));
                    }
                }
            }
        }
        return indexListSet;
    }
}
