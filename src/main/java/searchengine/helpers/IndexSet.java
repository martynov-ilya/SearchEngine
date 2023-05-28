package searchengine.helpers;

import lombok.Value;
@Value
public class IndexSet {
    Integer pageID;
    Integer lemmaID;
    Float rank;
}
