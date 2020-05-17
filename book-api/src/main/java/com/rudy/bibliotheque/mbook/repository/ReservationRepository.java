package com.rudy.bibliotheque.mbook.repository;

import com.rudy.bibliotheque.mbook.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationSearchRepository {
    List<Reservation> findAllByBookId(Long id);
}
