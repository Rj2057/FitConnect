package com.fitconnect.controller;

import com.fitconnect.dto.BookingRatingRequest;
import com.fitconnect.dto.BookingRequest;
import com.fitconnect.dto.BookingResponse;
import com.fitconnect.dto.BookingStatusUpdateRequest;
import com.fitconnect.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Trainer session booking workflows")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @PreAuthorize("hasRole('GYM_USER')")
    @Operation(summary = "Create booking", description = "Allows a gym user to book a personal training session")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('GYM_USER')")
    @Operation(summary = "Get user bookings", description = "Returns bookings for the authenticated gym user")
    public ResponseEntity<List<BookingResponse>> getUserBookings() {
        return ResponseEntity.ok(bookingService.getUserBookings());
    }

    @GetMapping("/trainer")
    @PreAuthorize("hasRole('GYM_TRAINER')")
    @Operation(summary = "Get trainer bookings", description = "Returns client bookings for the authenticated trainer")
    public ResponseEntity<List<BookingResponse>> getTrainerBookings() {
        return ResponseEntity.ok(bookingService.getTrainerBookings());
    }

    @PostMapping("/{bookingId}/rating")
    @PreAuthorize("hasRole('GYM_USER')")
    @Operation(summary = "Rate trainer booking", description = "Allows a gym user to rate and review a trainer for their own booking")
    public ResponseEntity<BookingResponse> rateBooking(@PathVariable Long bookingId,
                                                       @Valid @RequestBody BookingRatingRequest request) {
        return ResponseEntity.ok(bookingService.rateBooking(bookingId, request));
    }

    @PutMapping("/{bookingId}/status")
    @PreAuthorize("hasRole('GYM_TRAINER')")
    @Operation(summary = "Update booking status", description = "Allows trainer to accept or reject booking with a response message")
    public ResponseEntity<BookingResponse> updateBookingStatus(@PathVariable Long bookingId,
                                                               @Valid @RequestBody BookingStatusUpdateRequest request) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(bookingId, request));
    }
}
