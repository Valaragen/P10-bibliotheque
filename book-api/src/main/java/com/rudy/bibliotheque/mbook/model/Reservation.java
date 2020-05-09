package com.rudy.bibliotheque.mbook.model;

import com.rudy.bibliotheque.mbook.model.common.AbstractEntityComposedId;
import com.rudy.bibliotheque.mbook.model.id.ReservationId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class Reservation extends AbstractEntityComposedId {

    @EmbeddedId
    private ReservationId id;

    @CreationTimestamp
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date reservationDate;

}
