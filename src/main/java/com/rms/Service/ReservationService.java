package com.rms.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.rms.Enums.ReservationStatus;
import com.rms.Repository.ReservationRepository;
import com.rms.Repository.ResourceRepository;
import com.rms.Repository.UserRepository;
import com.rms.dto.ReservationRequest;
import com.rms.dto.ReservationResponse;
import com.rms.entity.Reservation;
import com.rms.entity.Resource;
import com.rms.entity.User;
import com.rms.Exception.ReservationNotFoundException;
import com.rms.Exception.ResourceNotFoundException;
import com.rms.Exception.UserNotFoundException;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ResourceRepository resourceRepository,
            UserRepository userRepository) {

        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    // CREATE RESERVATION
    public ReservationResponse createReservation(
            ReservationRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        String username = authentication.getName();

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "User not found"));

        Resource resource =
                resourceRepository.findById(
                        request.getResourceId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Resource not found"));

        if (!resource.getAvailable()) {
            throw new RuntimeException(
                    "Resource is not available");
        }

        if (!request.getStartTime()
                .isBefore(request.getEndTime())) {

            throw new RuntimeException(
                    "Start time must be before end time");
        }

        Reservation reservation = new Reservation();

        reservation.setUser(user);

        reservation.setResource(resource);

        reservation.setStartTime(
                request.getStartTime());

        reservation.setEndTime(
                request.getEndTime());

        reservation.setPrice(
                request.getPrice());

        reservation.setStatus(
                ReservationStatus.PENDING);

        reservation.setCreatedAt(
                LocalDateTime.now());

        Reservation savedReservation =
                reservationRepository.save(reservation);

        return convertToResponse(savedReservation);
    }

    // USER - GET OWN RESERVATIONS
    public List<ReservationResponse> getMyReservations() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        String username = authentication.getName();

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "User not found"));

        return reservationRepository
                .findByUserId(user.getId())
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // ADMIN - GET ALL RESERVATIONS
    public List<ReservationResponse> getAllReservations() {

        return reservationRepository
                .findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // ADMIN - FILTER + PAGINATION + SORTING
    public Page<ReservationResponse> getReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sortBy,
            String direction) {

        if (page < 0) {
            throw new RuntimeException(
                    "Page must be zero or greater");
        }

        if (size < 1) {
            throw new RuntimeException(
                    "Size must be greater than zero");
        }

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new RuntimeException(
                    "Minimum price cannot be greater than maximum price");
        }

        Sort sort;

        if (sortBy != null && !sortBy.isBlank()) {

            if ("desc".equalsIgnoreCase(direction)) {

                sort = Sort.by(
                        Sort.Direction.DESC,
                        sortBy);

            } else {

                sort = Sort.by(
                        Sort.Direction.ASC,
                        sortBy);
            }

        } else {

            sort = Sort.by(
                    Sort.Direction.DESC,
                    "createdAt");
        }

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        sort);

        Specification<Reservation> specification =
                (root, query, criteriaBuilder) -> null;

        if (status != null) {

            specification =
                    specification.and(
                            (root, query, criteriaBuilder) ->
                                    criteriaBuilder.equal(
                                            root.get("status"),
                                            status));
        }

        if (minPrice != null) {

            specification =
                    specification.and(
                            (root, query, criteriaBuilder) ->
                                    criteriaBuilder.greaterThanOrEqualTo(
                                            root.get("price"),
                                            minPrice));
        }

        if (maxPrice != null) {

            specification =
                    specification.and(
                            (root, query, criteriaBuilder) ->
                                    criteriaBuilder.lessThanOrEqualTo(
                                            root.get("price"),
                                            maxPrice));
        }

        Page<Reservation> reservationPage =
                reservationRepository.findAll(
                        specification,
                        pageable);

        return reservationPage.map(
                this::convertToResponse);
    }

 
    public ReservationResponse updateReservation(
            Long id,
            ReservationRequest request,
            ReservationStatus status) {

        Reservation reservation =
                reservationRepository.findById(id)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation not found"));

        if (!request.getStartTime()
                .isBefore(request.getEndTime())) {

            throw new RuntimeException(
                    "Start time must be before end time");
        }

        reservation.setStartTime(
                request.getStartTime());

        reservation.setEndTime(
                request.getEndTime());

        reservation.setPrice(
                request.getPrice());

        reservation.setStatus(status);

        Reservation updatedReservation =
                reservationRepository.save(reservation);

        return convertToResponse(updatedReservation);
    }
   
    public void deleteReservation(Long id) {

        Reservation reservation =
                reservationRepository.findById(id)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation not found"));

        reservationRepository.delete(reservation);
    }

    private ReservationResponse convertToResponse(
            Reservation reservation) {

        ReservationResponse response =
                new ReservationResponse();

        response.setId(
                reservation.getId());

        response.setResourceId(
                reservation.getResource().getId());

        response.setUserId(
                reservation.getUser().getId());

        response.setStartTime(
                reservation.getStartTime());

        response.setEndTime(
                reservation.getEndTime());

        response.setPrice(
                reservation.getPrice());

        response.setStatus(
                reservation.getStatus());

        return response;
    }
}