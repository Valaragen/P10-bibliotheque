package com.rudy.bibliotheque.mbook.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanSearch {
    private Set<String> status;
    private String userId;
}
