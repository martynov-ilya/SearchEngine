package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.Collection;
import java.util.List;


@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    Page findByPathAndSiteId(String path, Site site);
    Iterable<Page> findBySiteId(Site site);

    Integer countBySiteId(Site siteId);

    @Query(value = "SELECT p.* FROM page p JOIN index_search i ON p.id = i.page_id WHERE i.lemma_id IN :lemmas", nativeQuery = true)
    List<Page> findByLemmaList(@Param("lemmas") Collection<Lemma> lemmaListId);

}
