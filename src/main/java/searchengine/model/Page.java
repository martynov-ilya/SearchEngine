package searchengine.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "page", indexes = {@Index(name = "path_list", columnList = "path")})
@Setter
@Getter
@NoArgsConstructor
public class Page implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "site_id", referencedColumnName = "id")
    private Site siteId;
    @Column(length = 1000, columnDefinition = "VARCHAR(515)", nullable = false)
    private String path;

    private int code;
    @Column(length = 16777215, columnDefinition = "mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci")
    private String content;


    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL)
    private List<SearchIndex> index = new ArrayList<>();

    public Page(Site siteId, String path, int code, String content) {
        this.siteId = siteId;
        this.path = path;
        this.code = code;
        this.content = content;
    }
}
