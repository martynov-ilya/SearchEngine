package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.Morphology;
import org.springframework.stereotype.Service;
import searchengine.helpers.HtmlCleaner;
import searchengine.helpers.SearchDataSet;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SearchIndex;
import searchengine.model.Site;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final LemmaSearcher lemmaSearcher;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final SiteRepository siteRepository;

    @Override
    public List<SearchDataSet> allSiteSearch(String searchText, int offset, int limit) {

        List<Site> siteList = siteRepository.findAll();
        List<SearchDataSet> result = new ArrayList<>();
        List<Lemma> foundLemmaList = new ArrayList<>();
        List<String> textLemmaList = getLemmaFromSearchText(searchText);
        for (Site site : siteList) {
            foundLemmaList.addAll(getLemmaListFromSite(textLemmaList, site));
        }
        List<SearchDataSet> searchData = null;
        for (Lemma l : foundLemmaList) {
            if (l.getLemma().equals(searchText)) {
                searchData = new ArrayList<>(getSearchDtoList(foundLemmaList, textLemmaList, offset, limit));
                result.addAll(searchData);
            }
        }

        result.sort((o1, o2) -> Float.compare(o2.getRelevance(), o1.getRelevance()));
        if (offset + 1 >= result.size()) {
            return Collections.emptyList();
        }
        return result.subList(offset, Math.min(offset + limit, result.size()));
    }

    @Override
    public List<SearchDataSet> siteSearch(String searchText, String url, int offset, int limit) {

        Site site = siteRepository.findByUrl(url);
        List<String> textLemmaList = getLemmaFromSearchText(searchText);
        List<Lemma> foundLemmaList = getLemmaListFromSite(textLemmaList, site);
        return getSearchDtoList(foundLemmaList, textLemmaList, offset, limit);
    }

    private List<String> getLemmaFromSearchText(String searchText) {
        String[] words = searchText.toLowerCase(Locale.ROOT).split(" ");
        List<String> lemmaList = new ArrayList<>();
        for (String lemma : words) {
            List<String> list = lemmaSearcher.getLemma(lemma);
            lemmaList.addAll(list);
        }
        return lemmaList;
    }

    private List<Lemma> getLemmaListFromSite(List<String> lemmas, Site site) {
        lemmaRepository.flush();
        List<Lemma> lemmaList = lemmaRepository.findLemmaListBySite(lemmas, site);
        List<Lemma> result = new ArrayList<>(lemmaList);
        result.sort(Comparator.comparingInt(Lemma::getFrequency));
        return result;
    }

    private List<SearchDataSet> getSearchData(Hashtable<Page, Float> pageList, List<String> textLemmaList) {
        List<SearchDataSet> result = new ArrayList<>();

        for (Page page : pageList.keySet()) {
            String uri = page.getPath();
            String content = page.getContent();
            Site pageSite = page.getSiteId();
            String site = pageSite.getUrl();
            String siteName = pageSite.getName();
            Float absRelevance = pageList.get(page);

            StringBuilder clearContent = new StringBuilder();
            String title = HtmlCleaner.clear(content, "title");
            String body = HtmlCleaner.clear(content, "body");
            clearContent.append(title).append(" ").append(body);
            String snippet = getSnippet(clearContent.toString(), textLemmaList);

            result.add(new SearchDataSet(site, siteName, uri, title, snippet, absRelevance));
        }
        return result;
    }

    private String getSnippet(String content, List<String> lemmaList) {
        List<Integer> lemmaIndex = new ArrayList<>();
        StringBuilder result = new StringBuilder();
        for (String lemma : lemmaList) {
            lemmaIndex.addAll(lemmaSearcher.getLemmaIndexInContent(content, lemma));
        }
        Collections.sort(lemmaIndex);
        List<String> wordsList = getWordsFromContent(content, lemmaIndex);
        for (int i = 0; i < wordsList.size(); i++) {
            result.append(wordsList.get(i)).append("... ");
            if (i > 3) {
                break;
            }
        }
        return result.toString();
    }

    private List<String> getWordsFromContent(String content, List<Integer> lemmaIndex) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < lemmaIndex.size(); i++) {
            int start = lemmaIndex.get(i);
            int end = content.indexOf(" ", start);
            int nextPoint = i + 1;
            while (nextPoint < lemmaIndex.size() && lemmaIndex.get(nextPoint) - end > 0 && lemmaIndex.get(nextPoint) - end < 5) {
                end = content.indexOf(" ", lemmaIndex.get(nextPoint));
                nextPoint += 1;
            }
            i = nextPoint - 1;
            String text = getWordsFromIndex(start, end, content);
            result.add(text);
        }
        result.sort(Comparator.comparingInt(String::length).reversed());
        return result;
    }

    private String getWordsFromIndex(int start, int end, String content) {
        String word = content.substring(start, end);
        int prevPoint;
        int lastPoint;
        if (content.lastIndexOf(" ", start) != -1) {
            prevPoint = content.lastIndexOf(" ", start);
        } else prevPoint = start;
        if (content.indexOf(" ", end + 30) != -1) {
            lastPoint = content.indexOf(" ", end + 30);
        } else lastPoint = content.indexOf(" ", end);
        String text = content.substring(prevPoint, lastPoint);
        try {
            text = text.replaceAll(word, "<b>" + word + "</b>");
        } catch (Exception e) {
        }
        return text;
    }

    private List<SearchDataSet> getSearchDtoList(List<Lemma> lemmaList, List<String> textLemmaList, int offset, int limit) {
        List<SearchDataSet> result = new ArrayList<>();
        pageRepository.flush();
        if (lemmaList.size() >= textLemmaList.size()) {
            List<Page> foundPageList = pageRepository.findByLemmaList(lemmaList);
            indexRepository.flush();
            List<SearchIndex> foundIndexList = indexRepository.findByPagesAndLemmas(lemmaList, foundPageList);
            Hashtable<Page, Float> sortedPageByAbsRelevance = getPageAbsRelevance(foundPageList, foundIndexList);
            List<SearchDataSet> dataList = getSearchData(sortedPageByAbsRelevance, textLemmaList);

            if (offset > dataList.size()) {
                return new ArrayList<>();
            }

            if (dataList.size() > limit) {
                for (int i = offset; i < limit; i++) {
                    result.add(dataList.get(i));
                }
                return result;
            } else return dataList;
        } else return result;
    }

    private Hashtable<Page, Float> getPageAbsRelevance(List<Page> pageList, List<SearchIndex> indexList) {
        HashMap<Page, Float> pageWithRelevance = new HashMap<>();
        for (Page page : pageList) {
            float relevant = 0;
            for (SearchIndex index : indexList) {
                if (index.getPage() == page) {
                    relevant += index.getRank();
                }
            }
            pageWithRelevance.put(page, relevant);
        }
        HashMap<Page, Float> pageWithAbsRelevance = new HashMap<>();
        for (Page page : pageWithRelevance.keySet()) {
            float absRelevant = pageWithRelevance.get(page) / Collections.max(pageWithRelevance.values());
            pageWithAbsRelevance.put(page, absRelevant);
        }
        return pageWithAbsRelevance.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, Hashtable::new));
    }

}