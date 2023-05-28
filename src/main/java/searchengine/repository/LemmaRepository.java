package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

   int countBySitePageId(Site site);
    List<Lemma> findBySitePageId(Site siteId);

    @Query(value = "SELECT l.* FROM lemma l WHERE l.lemma IN :lemmas AND l.site_id = :site", nativeQuery = true)
    List<Lemma> findLemmaListBySite(@Param("lemmas") List<String> lemmaList,
                                    @Param("site") Site site);
}
