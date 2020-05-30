package com.rudy.bibliotheque.mbook.dto;

import lombok.Data;

@Data
public class ReservationCreateDTO {
    private String userId;
    private Long bookId;
}
