package com.rudy.bibliotheque.mbook.repository;

import com.rudy.bibliotheque.mbook.model.Reservation;
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

        List<Predicate> predicates = new ArrayList<>();
        Path<UserInfo> userInfoPath = reservationRoot.get("id").get("userInfo");
        Path<UserInfo> bookPath = reservationRoot.get("id").get("book");

        if(reservationSearch.getUserId() != null) {
            predicates.add(cb.like(userInfoPath.get("id"), reservationSearch.getUserId()));
        } else {
            predicates.add(cb.like(userInfoPath.get("id"), "%"));
        }

        if(reservationSearch.getBookId() != null) {
            predicates.add(cb.like(bookPath.get("id"), reservationSearch.getBookId()));
        }

        query.select(reservationRoot)
                .where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(query)
                .getResultList();
    }
}
