
package com.rms.Controller;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

        this.reservationService =
                reservationService;
    }

    // =========================================================
    // CREATE
    // =========================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request) {

        return ResponseEntity.ok(
                reservationService.createReservation(
                        request));
    }

    // =========================================================
    // USER - MY RESERVATIONS
    // =========================================================

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<ReservationResponse>>
            getMyReservations(
                    @RequestParam(defaultValue = "0")
                    int page,

                    @RequestParam(defaultValue = "10")
                    int size) {

        return ResponseEntity.ok(
                reservationService.getMyReservations(
                        page,
                        size));
    }

    // =========================================================
    // ADMIN - ALL RESERVATIONS
    // =========================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
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

        return ResponseEntity.ok(
                reservationService.getReservations(
                        status,
                        minPrice,
                        maxPrice,
                        page,
                        size,
                        sortBy,
                        direction));
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ReservationResponse>
            updateReservation(

                    @PathVariable Long id,

                    @Valid
                    @RequestBody
                    ReservationRequest request,

                    @RequestParam(required = false)
                    ReservationStatus status) {

        return ResponseEntity.ok(
                reservationService.updateReservation(
                        id,
                        request,
                        status));
    }

    // =========================================================
    // DELETE
    // =========================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id) {

        reservationService.deleteReservation(id);

        return ResponseEntity.noContent().build();
    }
}

