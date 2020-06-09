package com.rudy.bibliotheque.webui.controller;

import com.rudy.bibliotheque.webui.dto.CopyDTO;
import com.rudy.bibliotheque.webui.dto.ReservationCreateDTO;
import com.rudy.bibliotheque.webui.dto.ReservationDTO;
import com.rudy.bibliotheque.webui.dto.search.ReservationSearchDTO;
import com.rudy.bibliotheque.webui.proxies.BookApiProxy;
import com.rudy.bibliotheque.webui.util.Constant;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(Constant.RESERVATIONS_PATH)
public class ReservationController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private BookApiProxy bookApiProxy;

    @Autowired
    public ReservationController(BookApiProxy bookApiProxy) {
        this.bookApiProxy = bookApiProxy;
    }

    @PreAuthorize("hasRole('" + Constant.USER_ROLE_NAME + "')")
    @GetMapping(Constant.MY_RESERVATIONS_PATH)
    public String getUserReservations(@ModelAttribute("reservationSearch") ReservationSearchDTO reservationSearchDTO, Model model) {
        //Reset the status as the user can't define one on this page
        reservationSearchDTO.setStatus(new HashSet<>());
        loadReservationsInModel(reservationSearchDTO, model);
        return Constant.RESERVATIONS_USER_PAGE;
    }

    @PreAuthorize("hasRole('" + Constant.USER_ROLE_NAME + "')")
    @PostMapping(Constant.MY_RESERVATIONS_PATH)
    public String submitReservationForCurrentUser(@ModelAttribute("reservation") ReservationCreateDTO reservationCreateDTO) {
        ResponseEntity<ReservationDTO> newReservation = bookApiProxy.createReservationForCurrentUser(reservationCreateDTO);

        if (newReservation.getStatusCode() != HttpStatus.CREATED) {
            //TODO add logic
            log.error("Can't create reservation");
        }

        return Constant.REDIRECT + Constant.RESERVATIONS_PATH + Constant.MY_RESERVATIONS_PATH;
    }

    @PreAuthorize("hasRole('"+ Constant.USER_ROLE_NAME +"')")
    @PostMapping(Constant.SLASH_ID_PATH + Constant.CANCEL_PATH)
    public String cancelReservationForCurrentUser(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        ResponseEntity<ReservationDTO> reservationDTO = bookApiProxy.cancelMyReservation(id);

        if (reservationDTO.getStatusCode() != HttpStatus.NO_CONTENT) {
            //TODO add logic notification
            log.error("Can't extend borrow duration");
        }

        return Constant.REDIRECT + httpServletRequest.getHeader("referer");
    }

    private void loadReservationsInModel(ReservationSearchDTO reservationSearchDTO, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        KeycloakAuthenticationToken kp = (KeycloakAuthenticationToken) authentication;
        AccessToken token = kp.getAccount().getKeycloakSecurityContext().getToken();
        String userId = token.getSubject();

        List<ReservationDTO> reservations = bookApiProxy.getReservationsOfCurrentUser(reservationSearchDTO);
        List<ReservationDTO> ongoingReservations = new ArrayList<>();
        List<ReservationDTO> finishedReservations = new ArrayList<>();
        List<ReservationDTO> cancelledReservations = new ArrayList<>();

        for (ReservationDTO reservation : reservations) {
            switch (reservation.getStatus().getName()) {
                case Constant.STATUS_ONGOING:
                    ongoingReservations.add(reservation);
                    List<ReservationDTO> bookReservations = reservation.getBook().getOngoingReservations().stream().sorted(Comparator.comparing(ReservationDTO::getReservationStartDate)).collect(Collectors.toList());
                    int positionInQueue = 0;
                    for (ReservationDTO bookReservation : bookReservations) {
                        positionInQueue++;
                        if (bookReservation.getUserInfo().getId().equals(userId)) {
                            reservation.setCurrentUserPositionInQueue(positionInQueue);
                            break;
                        }
                    }

                    reservation.getBook().getCopies().stream()
                            .filter((copy) -> !copy.getOngoingBorrow().isEmpty() && copy.getOngoingBorrow().get(0).getLoanEndDate() != null && copy.getOngoingBorrow().get(0).getLoanEndDate().after(new Date()))
                            .min(Comparator.comparing((copy) -> copy.getOngoingBorrow().get(0).getLoanEndDate())).ifPresent(nextCopyReturn -> reservation.setNearestReturnDate(nextCopyReturn.getOngoingBorrow().get(0).getLoanEndDate()));
                    break;
                case Constant.STATUS_FINISHED:
                    finishedReservations.add(reservation);
                    break;
                case Constant.STATUS_CANCELLED:
                    cancelledReservations.add(reservation);
                    break;
            }
        }

        model.addAttribute("ongoingReservations", ongoingReservations);
        model.addAttribute("finishedReservations", finishedReservations);
        model.addAttribute("cancelledReservations", cancelledReservations);
    }
}
