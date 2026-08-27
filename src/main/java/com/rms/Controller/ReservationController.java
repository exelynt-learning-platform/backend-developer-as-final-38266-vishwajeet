package com.rms.Controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rms.Enums.ReservationStatus;
import com.rms.Service.ReservationService;
import com.rms.dto.ReservationRequest;
import com.rms.dto.ReservationResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(
            ReservationService reservationService) {

        this.reservationService = reservationService;
    }

    // CREATE RESERVATION
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request) {

        ReservationResponse response =
                reservationService.createReservation(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // USER - GET OWN RESERVATIONS
    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponse>>
            getMyReservations() {

        List<ReservationResponse> reservations =
                reservationService.getMyReservations();

        return ResponseEntity.ok(reservations);
    }

    // ADMIN - GET RESERVATIONS
    // FILTER + PAGINATION + SORTING
    @GetMapping
    public ResponseEntity<Page<ReservationResponse>>
            getReservations(

                    @RequestParam(required = false)
                    ReservationStatus status,

                    @RequestParam(required = false)
                    BigDecimal minPrice,

                    @RequestParam(required = false)
                    BigDecimal maxPrice,

                    @RequestParam(defaultValue = "0")
                    int page,

                    @RequestParam(defaultValue = "10")
                    int size,

                    @RequestParam(defaultValue = "createdAt")
                    String sortBy,

                    @RequestParam(defaultValue = "desc")
                    String direction) {

        Page<ReservationResponse> reservations =
                reservationService.getReservations(
                        status,
                        minPrice,
                        maxPrice,
                        page,
                        size,
                        sortBy,
                        direction
                );

        return ResponseEntity.ok(reservations);
    }

    // ADMIN - UPDATE RESERVATION
    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse>
            updateReservation(

                    @PathVariable Long id,

                    @RequestParam
                    ReservationStatus status,

                    @Valid
                    @RequestBody ReservationRequest request) {

        ReservationResponse response =
                reservationService.updateReservation(
                        id,
                        request,
                        status
                );

        return ResponseEntity.ok(response);
    }

    // ADMIN - DELETE RESERVATION
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id) {

        reservationService.deleteReservation(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}