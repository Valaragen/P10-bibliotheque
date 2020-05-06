package com.rudy.bibliotheque.mbook.model.id;

import com.rudy.bibliotheque.mbook.model.Book;
import com.rudy.bibliotheque.mbook.model.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ReservationId implements Serializable {

    @ManyToOne
    private Book book;

    @ManyToOne
    private UserInfo userInfo;

}
