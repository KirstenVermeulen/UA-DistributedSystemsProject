package ua.HTTP;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetNeighbors {
    public final String previous_node;
    public final String next_node;

    public GetNeighbors(@JsonProperty("copyright") String previous_node,
                @JsonProperty("date") String next_node) {
        this.previous_node = previous_node;
        this.next_node = next_node;
    }

}
