package com.rudy.bibliotheque.webui.dto;

import lombok.Data;

@Data
public class ReservationCreateDTO {
    private String userId;
    private Long bookId;
}
