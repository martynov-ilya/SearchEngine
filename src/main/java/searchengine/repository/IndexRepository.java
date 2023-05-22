package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SearchIndex;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<SearchIndex, Integer> {
    List<SearchIndex> findByPageId (int pageId);
}
