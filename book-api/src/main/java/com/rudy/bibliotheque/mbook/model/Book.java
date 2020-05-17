package com.rudy.bibliotheque.mbook.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.rudy.bibliotheque.mbook.model.common.AbstractEntity;
import com.rudy.bibliotheque.mbook.util.Constant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Filter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class Book extends AbstractEntity {

    @Column(length = 13, unique = true, nullable = false)
    private String isbn;
    @Column(length = 100, nullable = false)
    private String name;
    @Column(length = 5000)
    private String description;
    @Column(length = 100, nullable = false)
    private String author;
    @Column(length = 100, nullable = false)
    private String publisher;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date releaseDate;

    private Integer copyNumber;

    private Integer availableCopyNumber;

    @JsonBackReference
    @OneToMany(mappedBy = "book", fetch = FetchType.EAGER)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Reservation> reservations;

    @JsonBackReference
    @OneToMany(mappedBy = "book")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Filter(name = "onlyOngoing", condition = "status = " + Constant.STATUS_ONGOING)
    private Set<Reservation> ongoingReservations;

}
