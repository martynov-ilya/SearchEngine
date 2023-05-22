package searchengine.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

//@Entity
//@Setter
//@Getter
//@Builder
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "site")
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Enumerated(EnumType.STRING)
    private StatusIndexing status;

    @Column(name = "status_time")
    private LocalDateTime statusTime;

    @Column(name = "last_error")
    private String lastError;

    private String url;

    private String name;

}