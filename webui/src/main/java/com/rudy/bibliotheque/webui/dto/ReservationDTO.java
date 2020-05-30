package com.rudy.bibliotheque.webui.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReservationDTO {

    private Long id;

    private BookDTO book;

    private UserInfoDTO userInfo;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reservationStartDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reservationEndDate;

    private ReservationStatus status;

    @JsonIgnore
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date nearestReturnDate;

    @JsonIgnore
    private Integer currentUserPositionInQueue;


}
