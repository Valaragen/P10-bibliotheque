package com.rudy.bibliotheque.mbook.repository;

import com.rudy.bibliotheque.mbook.model.Borrow;
import com.rudy.bibliotheque.mbook.model.UserInfo;
import com.rudy.bibliotheque.mbook.search.LoanSearch;
import com.rudy.bibliotheque.mbook.util.Constant;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BorrowSearchRepositoryImpl implements BorrowSearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Borrow> findAllBySearch(LoanSearch loanSearch) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Borrow> query = cb.createQuery(Borrow.class);
        Root<Borrow> borrowRoot = query.from(Borrow.class);

        List<Predicate> statusPredicates = new ArrayList<>();


        if(loanSearch.getStatus() != null) {
            if (!loanSearch.getStatus().isEmpty()) {
                Path<Date> loanStartDatePath = borrowRoot.get("loanStartDate");
                Path<Date> loanEndDatePath = borrowRoot.get("loanEndDate");
                Path<Date> returnedOnPath = borrowRoot.get("returnedOn");
                if (loanSearch.getStatus().contains(Constant.STATUS_PENDING)) {
                    statusPredicates.add(cb.isNull(loanStartDatePath));
                }
                if (loanSearch.getStatus().contains(Constant.STATUS_ONGOING)) {
                    statusPredicates.add(cb.and(cb.isNotNull(loanStartDatePath), cb.isNull(returnedOnPath)));
                }
                if (loanSearch.getStatus().contains(Constant.STATUS_LATE)) {
                    Date now = new Date();
                    statusPredicates.add(cb.and(cb.isNull(returnedOnPath), cb.lessThan(loanEndDatePath, now)));
                }
                if (loanSearch.getStatus().contains(Constant.STATUS_FINISHED)) {
                    statusPredicates.add(cb.isNotNull(returnedOnPath));
                }
            }
        } else {
            Path<Date> loanRequestDatePath = borrowRoot.get("loanRequestDate");
            statusPredicates.add(cb.isNotNull(loanRequestDatePath));
        }

        List<Predicate> userPredicates = new ArrayList<>();
        Path<UserInfo> userInfoPath = borrowRoot.get("userInfo");
        if(loanSearch.getUserId() != null) {
            userPredicates.add(cb.like(userInfoPath.get("id"), loanSearch.getUserId()));
        } else {
            userPredicates.add(cb.like(userInfoPath.get("id"), "%"));
        }

        query.select(borrowRoot)
                .where(cb.and(cb.or(statusPredicates.toArray(new Predicate[0])), cb.and(userPredicates.toArray(new Predicate[0]))));

        return entityManager.createQuery(query)
                .getResultList();
    }
}
