package com.rudy.bibliotheque.webui.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CopyDTO {

    private String code;

    private BookDTO book;

    private String stateAtPurchase;
    private String currentState;
    private boolean borrowed;

    private List<BorrowDTO> ongoingBorrow;
}
