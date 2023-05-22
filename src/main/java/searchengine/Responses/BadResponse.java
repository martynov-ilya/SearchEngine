package searchengine.Responses;

import lombok.Value;

@Value
public class BadResponse {
    boolean result;
    String error;
}
