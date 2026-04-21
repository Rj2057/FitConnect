package com.fitconnect.dto;

import com.fitconnect.entity.TrainerBooking;
import com.fitconnect.entity.enums.BookingStatus;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for trainer booking responses.
 *
 * ===== BUILDER PATTERN =====
 * Lombok @Builder generates a fluent builder at compile time.
 * Instead of a telescoping constructor like:
 *   new BookingResponse(id, trainerId, userId, trainerName, ...)   ← hard to read
 *
 * We use:
 *   BookingResponse response = BookingResponse.builder()
 *       .id(booking.getId())
 *       .trainerId(booking.getTrainer().getId())
 *       .userId(booking.getUser().getId())
 *       .trainerName(booking.getTrainer().getUser().getName())
 *       .userName(booking.getUser().getName())
 *       .date(booking.getDate())
 *       .timeSlot(booking.getTimeSlot())
 *       .status(booking.getStatus())
 *       .build();                                                   ← clean & readable
 *
 * @see BookingService#toResponse(TrainerBooking) — live usage in the service layer
 * ==========================
 */
@Data
@Builder
public class BookingResponse {

    private Long id;
    private Long trainerId;
    private Long userId;
    private String trainerName;
    private String userName;
    private LocalDate date;
    private String timeSlot;
    private Integer userRating;
    private String userReview;
    private String trainerResponseMessage;
    private String trainerProposedTimeSlot;
    private BookingStatus status;

    /**
     * Builder Pattern — static factory method.
     *
     * Converts a {@link TrainerBooking} entity into a {@link BookingResponse} DTO
     * using the Lombok-generated builder. This is the canonical way to construct
     * this DTO, keeping object creation logic in one place.
     *
     * Usage:
     *   BookingResponse dto = BookingResponse.from(booking);
     */
    public static BookingResponse from(TrainerBooking booking) {
        return BookingResponse.builder()           // 1. Get builder instance
                .id(booking.getId())               // 2. Set each field fluently
                .trainerId(booking.getTrainer().getId())
                .userId(booking.getUser().getId())
                .trainerName(booking.getTrainer().getUser().getName())
                .userName(booking.getUser().getName())
                .date(booking.getDate())
                .timeSlot(booking.getTimeSlot())
                .userRating(booking.getUserRating())
                .userReview(booking.getUserReview())
                .trainerResponseMessage(booking.getTrainerResponseMessage())
                .trainerProposedTimeSlot(booking.getTrainerProposedTimeSlot())
                .status(booking.getStatus())
                .build();                          // 3. Construct the immutable object
    }
}

