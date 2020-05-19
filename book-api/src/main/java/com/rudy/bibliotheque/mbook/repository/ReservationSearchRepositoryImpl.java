package com.rudy.bibliotheque.mbook.repository;

import com.rudy.bibliotheque.mbook.model.Book;
import com.rudy.bibliotheque.mbook.model.Reservation;
import com.rudy.bibliotheque.mbook.model.ReservationStatus;
import com.rudy.bibliotheque.mbook.model.UserInfo;
import com.rudy.bibliotheque.mbook.search.ReservationSearch;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationSearchRepositoryImpl implements ReservationSearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Reservation> findAllBySearch(ReservationSearch reservationSearch) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Reservation> query = cb.createQuery(Reservation.class);
        Root<Reservation> reservationRoot = query.from(Reservation.class);

        List<Predicate> andPredicates = new ArrayList<>();
        List<Predicate> orPredicates = new ArrayList<>();
        Path<UserInfo> userInfoPath = reservationRoot.get("userInfo");
        Path<Book> bookPath = reservationRoot.get("book");

        if (reservationSearch.getUserId() != null) {
            andPredicates.add(cb.like(userInfoPath.get("id"), reservationSearch.getUserId()));
        } else {
            andPredicates.add(cb.like(userInfoPath.get("id"), "%"));
        }

        if (reservationSearch.getBookId() != null) {
            andPredicates.add(cb.like(bookPath.get("id"), reservationSearch.getBookId()));
        }

        if (reservationSearch.getStatus() != null) {
            if (!reservationSearch.getStatus().isEmpty()) {
                if (reservationSearch.getStatus().contains(ReservationStatus.ONGOING.getName())) {
                    orPredicates.add(cb.equal(reservationRoot.get("status"), ReservationStatus.ONGOING));
                }
                if (reservationSearch.getStatus().contains(ReservationStatus.CANCELLED.getName())) {
                    orPredicates.add(cb.equal(reservationRoot.get("status"), ReservationStatus.CANCELLED));
                }
                if (reservationSearch.getStatus().contains(ReservationStatus.FINISHED.getName())) {
                    orPredicates.add(cb.equal(reservationRoot.get("status"), ReservationStatus.FINISHED));
                }
            }
        } else {
            orPredicates.add(cb.isNotNull(reservationRoot.get("status")));
        }

        query.select(reservationRoot)
                .where(cb.and(cb.or(orPredicates.toArray(new Predicate[0])), cb.and(andPredicates.toArray(new Predicate[0]))));

        return entityManager.createQuery(query)
                .getResultList();
    }
}
