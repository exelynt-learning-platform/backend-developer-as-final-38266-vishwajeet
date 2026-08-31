
package com.rms.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of(
                    "status",
                    "price",
                    "startTime",
                    "endTime",
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

    @Transactional
    public ReservationResponse createReservation(
            ReservationRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Reservation request is required");
        }

        validateReservationTime(
                request.getStartTime(),
                request.getEndTime());

        User user = getAuthenticatedUser();

        Resource resource =
                resourceRepository.findById(
                        request.getResourceId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Resource not found"));

        if (!resource.getAvailable()) {
            throw new IllegalArgumentException(
                    "Resource is not available");
        }

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
    // USER - GET OWN RESERVATIONS
    // PAGINATION
    // =========================================================

    public Page<ReservationResponse> getMyReservations(
            int page,
            int size) {

        validatePagination(page, size);

        User user = getAuthenticatedUser();

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"));

        Page<Reservation> reservations =
                reservationRepository.findByUserId(
                        user.getId(),
                        pageable);

        return reservations.map(
                this::convertToResponse);
    }

    // =========================================================
    // ADMIN - GET ALL RESERVATIONS
    // FILTER + PAGINATION + SORTING
    // =========================================================

    public Page<ReservationResponse> getReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sortBy,
            String direction) {

        validatePagination(page, size);

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException(
                    "Minimum price cannot be greater than maximum price");
        }

        String validatedSortBy =
                validateSortField(sortBy);

        Sort.Direction sortDirection =
                validateSortDirection(direction);

        Sort sort =
                Sort.by(
                        sortDirection,
                        validatedSortBy);

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        sort);

        Specification<Reservation> specification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.conjunction();

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
                                    criteriaBuilder
                                            .greaterThanOrEqualTo(
                                                    root.get("price"),
                                                    minPrice));
        }

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

    @Transactional
    public ReservationResponse updateReservation(
            Long id,
            ReservationRequest request,
            ReservationStatus status) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Reservation request is required");
        }

        Reservation reservation =
                reservationRepository.findById(id)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation not found"));

        validateOwnership(reservation);

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
            reservation.setStatus(status);
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

    @Transactional
    public void deleteReservation(Long id) {

        Reservation reservation =
                reservationRepository.findById(id)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation not found"));

        validateOwnership(reservation);

        reservationRepository.delete(
                reservation);
    }

    // =========================================================
    // AUTHENTICATED USER
    // =========================================================

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {

            throw new RuntimeException(
                    "User is not authenticated");
        }

        String username =
                authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"));
    }

    // =========================================================
    // OWNERSHIP VALIDATION
    // =========================================================

    private void validateOwnership(
            Reservation reservation) {

        if (isAdmin()) {
            return;
        }

        User currentUser =
                getAuthenticatedUser();

        if (reservation.getUser() == null
                || reservation.getUser().getId() == null
                || !reservation.getUser()
                        .getId()
                        .equals(currentUser.getId())) {

            throw new RuntimeException(
                    "You are not authorized to modify this reservation");
        }
    }

    // =========================================================
    // ADMIN CHECK
    // =========================================================

    private boolean isAdmin() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(
                                authority.getAuthority()));
    }

    // =========================================================
    // PAGINATION VALIDATION
    // =========================================================

    private void validatePagination(
            int page,
            int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must be zero or greater");
        }

        if (size < 1) {
            throw new IllegalArgumentException(
                    "Size must be greater than zero");
        }

        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Size cannot be greater than "
                            + MAX_PAGE_SIZE);
        }
    }

    // =========================================================
    // SORT FIELD VALIDATION
    // =========================================================

    private String validateSortField(
            String sortBy) {

        if (sortBy == null
                || sortBy.isBlank()) {

            return "createdAt";
        }

        if (!ALLOWED_SORT_FIELDS.contains(
                sortBy)) {

            throw new IllegalArgumentException(
                    "Invalid sort field: "
                            + sortBy);
        }

        return sortBy;
    }

    // =========================================================
    // SORT DIRECTION VALIDATION
    // =========================================================

    private Sort.Direction validateSortDirection(
            String direction) {

        if (direction == null
                || direction.isBlank()
                || "asc".equalsIgnoreCase(direction)) {

            return Sort.Direction.ASC;
        }

        if ("desc".equalsIgnoreCase(direction)) {

            return Sort.Direction.DESC;
        }

        throw new IllegalArgumentException(
                "Invalid sort direction. Use 'asc' or 'desc'");
    }

    // =========================================================
    // TIME VALIDATION
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
    // ENTITY -> DTO
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

