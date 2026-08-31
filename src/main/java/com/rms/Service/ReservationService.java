
package com.rms.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.rms.Enums.ReservationStatus;
import com.rms.Exception.ReservationNotFoundException;
import com.rms.Exception.ResourceNotFoundException;
import com.rms.Exception.UserNotFoundException;
import com.rms.Repository.ReservationRepository;
import com.rms.Repository.ResourceRepository;
import com.rms.Repository.UserRepository;
import com.rms.dto.ReservationRequest;
import com.rms.dto.ReservationResponse;
import com.rms.entity.Reservation;
import com.rms.entity.Resource;
import com.rms.entity.User;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    /*
     * Only these Reservation entity fields are allowed
     * for sorting.
     */
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "startTime",
            "endTime",
            "price",
            "createdAt"
    );

    public ReservationService(
            ReservationRepository reservationRepository,
            ResourceRepository resourceRepository,
            UserRepository userRepository) {

        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // CREATE RESERVATION
    // =========================================================

    public ReservationResponse createReservation(
            ReservationRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new RuntimeException(
                    "User is not authenticated");
        }

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

        validateReservationTime(
                request.getStartTime(),
                request.getEndTime());

        Reservation reservation =
                new Reservation();

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
                reservationRepository.save(
                        reservation);

        return convertToResponse(
                savedReservation);
    }

    // =========================================================
    // USER - GET OWN RESERVATIONS WITH PAGINATION
    // =========================================================

    public Page<ReservationResponse> getMyReservations(
            int page,
            int size) {

        // Validate page number
        if (page < 0) {

            throw new IllegalArgumentException(
                    "Page must be zero or greater");
        }

        // Validate page size
        if (size < 1) {

            throw new IllegalArgumentException(
                    "Size must be greater than zero");
        }

        // Prevent excessively large pages
        if (size > 100) {

            throw new IllegalArgumentException(
                    "Size cannot be greater than 100");
        }

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new RuntimeException(
                    "User is not authenticated");
        }

        String username =
                authentication.getName();

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "User not found"));

        /*
         * Retrieve only the requested page instead of
         * loading all reservations for the user.
         */
        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"));

        Page<Reservation> reservationPage =
                reservationRepository.findByUserId(
                        user.getId(),
                        pageable);

        return reservationPage.map(
                this::convertToResponse);
    }

    // =========================================================
    // ADMIN - FILTER + PAGINATION + SORTING
    // =========================================================

    public Page<ReservationResponse> getReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sortBy,
            String direction) {

        // Validate page
        if (page < 0) {

            throw new IllegalArgumentException(
                    "Page must be zero or greater");
        }

        // Validate page size
        if (size < 1) {

            throw new IllegalArgumentException(
                    "Size must be greater than zero");
        }

        // Prevent excessively large pages
        if (size > 100) {

            throw new IllegalArgumentException(
                    "Size cannot be greater than 100");
        }

        // Validate price range
        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException(
                    "Minimum price cannot be greater than maximum price");
        }

        // =====================================================
        // SORT FIELD VALIDATION
        // =====================================================

        String validatedSortBy =
                (sortBy == null || sortBy.isBlank())
                        ? "createdAt"
                        : sortBy;

        /*
         * Whitelist allowed sort fields.
         */
        if (!ALLOWED_SORT_FIELDS.contains(
                validatedSortBy)) {

            throw new IllegalArgumentException(
                    "Invalid sort field: "
                            + validatedSortBy);
        }

        // =====================================================
        // SORT DIRECTION
        // =====================================================

        Sort.Direction sortDirection;

        if ("desc".equalsIgnoreCase(direction)) {

            sortDirection =
                    Sort.Direction.DESC;

        } else if ("asc".equalsIgnoreCase(direction)
                || direction == null
                || direction.isBlank()) {

            sortDirection =
                    Sort.Direction.ASC;

        } else {

            throw new IllegalArgumentException(
                    "Invalid sort direction. "
                            + "Use 'asc' or 'desc'");
        }

        Sort sort =
                Sort.by(
                        sortDirection,
                        validatedSortBy);

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        sort);

        // =====================================================
        // SPECIFICATION
        // =====================================================

        Specification<Reservation> specification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.conjunction();

        // Filter by status
        if (status != null) {

            specification =
                    specification.and(
                            (root, query, criteriaBuilder) ->
                                    criteriaBuilder.equal(
                                            root.get("status"),
                                            status));
        }

        // Filter by minimum price
        if (minPrice != null) {

            specification =
                    specification.and(
                            (root, query, criteriaBuilder) ->
                                    criteriaBuilder
                                            .greaterThanOrEqualTo(
                                                    root.get("price"),
                                                    minPrice));
        }

        // Filter by maximum price
        if (maxPrice != null) {

            specification =
                    specification.and(
                            (root, query, criteriaBuilder) ->
                                    criteriaBuilder
                                            .lessThanOrEqualTo(
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

    // =========================================================
    // UPDATE RESERVATION
    // =========================================================

    public ReservationResponse updateReservation(
            Long id,
            ReservationRequest request,
            ReservationStatus status) {

        Reservation reservation =
                reservationRepository.findById(id)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation not found"));

        validateReservationTime(
                request.getStartTime(),
                request.getEndTime());

        reservation.setStartTime(
                request.getStartTime());

        reservation.setEndTime(
                request.getEndTime());

        reservation.setPrice(
                request.getPrice());

        if (status != null) {

            reservation.setStatus(
                    status);
        }

        Reservation updatedReservation =
                reservationRepository.save(
                        reservation);

        return convertToResponse(
                updatedReservation);
    }

    // =========================================================
    // DELETE RESERVATION
    // =========================================================

    public void deleteReservation(Long id) {

        Reservation reservation =
                reservationRepository.findById(id)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation not found"));

        reservationRepository.delete(
                reservation);
    }

    // =========================================================
    // VALIDATE RESERVATION TIME
    // =========================================================

    private void validateReservationTime(
            LocalDateTime startTime,
            LocalDateTime endTime) {

        if (startTime == null
                || endTime == null) {

            throw new IllegalArgumentException(
                    "Start time and end time are required");
        }

        if (!startTime.isBefore(endTime)) {

            throw new IllegalArgumentException(
                    "Start time must be before end time");
        }
    }

    // =========================================================
    // CONVERT ENTITY TO RESPONSE DTO
    // =========================================================

    private ReservationResponse convertToResponse(
            Reservation reservation) {

        ReservationResponse response =
                new ReservationResponse();

        response.setId(
                reservation.getId());

        response.setResourceId(
                reservation.getResource()
                        .getId());

        response.setUserId(
                reservation.getUser()
                        .getId());

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

