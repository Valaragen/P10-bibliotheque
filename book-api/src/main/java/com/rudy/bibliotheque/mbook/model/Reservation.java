package com.rudy.bibliotheque.mbook.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rudy.bibliotheque.mbook.model.common.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class Reservation extends AbstractEntity {
    @JsonIgnoreProperties({"reservations"})
    @ManyToOne
    private Book book;

    @ManyToOne
    private UserInfo userInfo;

    @CreationTimestamp
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date reservationStartDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    private Date reservationEndDate;

    @Column(nullable = false, length = 100)
    private ReservationStatus status;

}
