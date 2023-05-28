package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.helpers.HtmlCleaner;
import searchengine.helpers.LemmasSet;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.PageRepository;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
public class LemmaService {

    private final PageRepository pageRepository;
    private final LemmaSearcher lemmaSearcher;

    public List<LemmasSet> getLemmasPageSet() {
        List<LemmasSet> lemmaListSet = new CopyOnWriteArrayList<>();
        Iterable<Page> pageList = pageRepository.findAll();
        TreeMap<String, Integer> lemmaList = new TreeMap<>();
        for (Page page : pageList) {
            String content = page.getContent();

            String title = HtmlCleaner.clear(content, "title");
            String body = HtmlCleaner.clear(content, "body");

            HashMap<String, Integer> titleList =  lemmaSearcher.getMapLemmas(title);
            HashMap<String, Integer> bodyList = lemmaSearcher.getMapLemmas(body);

            Set<String> allWords = new HashSet<>();
            allWords.addAll(titleList.keySet());
            allWords.addAll(bodyList.keySet());

            for (String word : allWords) {
                lemmaList.put(word, lemmaList.getOrDefault(word, 0) + 1);
            }
        }
        for (String lemma : lemmaList.keySet()) {
            Integer frequency = lemmaList.get(lemma);
            lemmaListSet.add(new LemmasSet(lemma, frequency));
        }

        return lemmaListSet;
    }
}
