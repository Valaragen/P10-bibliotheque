package com.rudy.bibliotheque.mbook.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rudy.bibliotheque.mbook.model.common.AbstractEntityComposedId;
import com.rudy.bibliotheque.mbook.util.Constant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class Copy extends AbstractEntityComposedId {

    @Id
    private String code;

    @ManyToOne
    private Book book;

    @Column(nullable = false)
    private String stateAtPurchase;
    @Column(nullable = false)
    private String currentState;

    @Column(nullable = false)
    private boolean borrowed;

    @JsonIgnoreProperties({"copy"})
    @OneToMany(mappedBy = "copy", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Where(clause = Constant.CLAUSE_LOAN_REQUEST_DATE_IS_NOT_NULL_AND_RETURNED_ON_IS_NULL)
    private List<Borrow> ongoingBorrow;

}
