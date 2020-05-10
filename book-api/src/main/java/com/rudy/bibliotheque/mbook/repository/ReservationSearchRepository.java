package com.rudy.bibliotheque.mbook.repository;

import com.rudy.bibliotheque.mbook.model.Borrow;
import com.rudy.bibliotheque.mbook.model.Reservation;
import com.rudy.bibliotheque.mbook.search.LoanSearch;
import com.rudy.bibliotheque.mbook.search.ReservationSearch;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationSearchRepository {
    List<Reservation> findAllBySearch(ReservationSearch reservationSearch);
}
