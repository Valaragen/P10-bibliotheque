package com.rudy.bibliotheque.mbook.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.rudy.bibliotheque.mbook.model.common.AbstractEntity;
import com.rudy.bibliotheque.mbook.util.Constant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Where;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
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

    @JsonIgnoreProperties({"book"})
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Reservation> reservations;

    @JsonIgnoreProperties({"book"})
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Where(clause = Constant.CLAUSE_STATUS_IS_ONGOING)
    private List<Reservation> ongoingReservations;

}
