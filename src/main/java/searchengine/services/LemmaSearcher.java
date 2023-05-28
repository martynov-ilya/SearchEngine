package searchengine.services;

import java.util.HashMap;
import java.util.List;

public interface LemmaSearcher {
    List<String> getLemma(String word);
    HashMap<String, Integer> getMapLemmas(String content);
    List<Integer> getLemmaIndexInContent(String content, String lemma);
}
