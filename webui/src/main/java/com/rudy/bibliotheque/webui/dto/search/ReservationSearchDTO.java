package com.rudy.bibliotheque.webui.dto.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReservationSearchDTO {
    private String userId;
    private String bookId;
    private Set<String> status;
}
