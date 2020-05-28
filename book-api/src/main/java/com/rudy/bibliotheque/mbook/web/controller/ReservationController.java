package com.rudy.bibliotheque.mbook.web.controller;

import com.rudy.bibliotheque.mbook.config.ApplicationPropertiesConfig;
import com.rudy.bibliotheque.mbook.model.Borrow;
import com.rudy.bibliotheque.mbook.model.Reservation;
import com.rudy.bibliotheque.mbook.search.LoanSearch;
import com.rudy.bibliotheque.mbook.search.ReservationSearch;
import com.rudy.bibliotheque.mbook.service.BorrowService;
import com.rudy.bibliotheque.mbook.service.CopyService;
import com.rudy.bibliotheque.mbook.service.ReservationService;
import com.rudy.bibliotheque.mbook.service.UserInfoService;
import com.rudy.bibliotheque.mbook.util.Constant;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(Constant.RESERVATIONS_PATH)
public class ReservationController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private ApplicationPropertiesConfig appProperties;
    private ReservationService reservationService;
    private UserInfoService userInfoService;
    private KeycloakRestTemplate keycloakRestTemplate;

    @Autowired
    public ReservationController(ReservationService reservationService, UserInfoService userInfoService, ApplicationPropertiesConfig appProperties, KeycloakRestTemplate keycloakRestTemplate) {
        this.appProperties = appProperties;
        this.keycloakRestTemplate = keycloakRestTemplate;
        this.reservationService = reservationService;
        this.userInfoService = userInfoService;
    }

    @PreAuthorize("hasRole('"+ Constant.STAFF_ROLE_NAME +"')")
    @GetMapping
    public List<Reservation> getAllReservations(@ModelAttribute("reservationSearch") ReservationSearch reservationSearch){
        return reservationService.getAllReservationsBySearch(reservationSearch);
    }

    //TODO cancel reservation
}
