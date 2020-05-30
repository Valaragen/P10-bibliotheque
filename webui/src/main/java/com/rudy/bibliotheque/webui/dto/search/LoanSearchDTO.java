package com.rudy.bibliotheque.webui.dto.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanSearchDTO {
    private Set<String> status;
}
