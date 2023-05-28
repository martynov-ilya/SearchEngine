package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@Component
public class LemmaSearcherImpl implements LemmaSearcher{

    private static LuceneMorphology russianLuceneMorphology;
    private final static String REGEX = "\\p{Punct}|[0-9]|№|©|◄|«|»|—|-|@|…";

    static {
        try {
            russianLuceneMorphology = new RussianLuceneMorphology();
        } catch (Exception e) {
        }
    }

    @Override
    public HashMap<String, Integer> getMapLemmas(String content) {
        content = content.toLowerCase(Locale.ROOT).replaceAll(REGEX, " ");
        HashMap<String, Integer> lemmaList = new HashMap<>();
        String[] elements = content.toLowerCase(Locale.ROOT).split("\\s+");
        for (String elem : elements) {
            List<String> wordsList = getLemma(elem);
            for (String word : wordsList) {
                int count = lemmaList.getOrDefault(word, 0);
                lemmaList.put(word, count + 1);
            }
        }
        return lemmaList;
    }

    @Override
    public List<String> getLemma(String word) {
        List<String> lemmaList = new ArrayList<>();
        try {
            List<String> baseWordForm = russianLuceneMorphology.getNormalForms(word);
            if (!isServiceUnits(word)) {
                lemmaList.addAll(baseWordForm);
            }
        } catch (Exception e) {
        }
        return lemmaList;
    }

    @Override
    public List<Integer> getLemmaIndexInContent(String content, String lemma) {
        List<Integer> lemmaIndexList = new ArrayList<>();
        String[] elements = content.toLowerCase(Locale.ROOT).split("\\p{Punct}|\\s");
        int index = 0;
        for (String elem : elements) {
            List<String> lemmas = getLemma(elem);
            for (String lem : lemmas) {
                if (lem.equals(lemma)) {
                    lemmaIndexList.add(index);
                }
            }
            index += elem.length() + 1;
        }
        return lemmaIndexList;
    }

    private boolean isServiceUnits(String word) {
        List<String> wordDefaultForm = russianLuceneMorphology.getMorphInfo(word);
        for (String elem : wordDefaultForm) {
            if (elem.contains("ПРЕДЛ")
                    || elem.contains("ЧАСТ")
                    || elem.contains("МЕЖД")
                    || elem.contains("МС")
                    || elem.contains("СОЮЗ")
                    || elem.length() <= 3) {
                return true;
            }
        }
        return false;
    }
}
