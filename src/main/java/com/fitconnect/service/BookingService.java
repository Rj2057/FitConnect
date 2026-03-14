package com.fitconnect.service;

import com.fitconnect.dto.BookingRatingRequest;
import com.fitconnect.dto.BookingRequest;
import com.fitconnect.dto.BookingResponse;
import com.fitconnect.entity.Trainer;
import com.fitconnect.entity.TrainerBooking;
import com.fitconnect.entity.User;
import com.fitconnect.entity.enums.BookingStatus;
import com.fitconnect.exception.BadRequestException;
import com.fitconnect.exception.ResourceNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import com.fitconnect.repository.TrainerBookingRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class BookingService {

    private final TrainerBookingRepository bookingRepository;
    private final CurrentUserService currentUserService;
    private final TrainerService trainerService;

    public BookingService(TrainerBookingRepository bookingRepository,
                          CurrentUserService currentUserService,
                          TrainerService trainerService) {
        this.bookingRepository = bookingRepository;
        this.currentUserService = currentUserService;
        this.trainerService = trainerService;
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        Trainer trainer = trainerService.getTrainerEntity(request.getTrainerId());
        validateBookingSlot(request.getDate().getDayOfWeek(), request.getTimeSlot());

        TrainerBooking booking = TrainerBooking.builder()
                .trainer(trainer)
                .user(currentUser)
                .date(request.getDate())
                .timeSlot(request.getTimeSlot())
                .status(BookingStatus.PENDING)
                .build();

        return toResponse(bookingRepository.save(booking));
    }

    public List<BookingResponse> getUserBookings() {
        User currentUser = currentUserService.getCurrentUser();
        return bookingRepository.findByUser(currentUser).stream().map(this::toResponse).toList();
    }

    public List<BookingResponse> getTrainerBookings() {
        Trainer trainer = trainerService.getCurrentTrainer();
        return bookingRepository.findByTrainer(trainer).stream().map(this::toResponse).toList();
    }

    @Transactional
    public BookingResponse rateBooking(Long bookingId, BookingRatingRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        TrainerBooking booking = bookingRepository.findByIdAndUser(bookingId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Cancelled bookings cannot be rated");
        }

        if (request.getReview().trim().isEmpty()) {
            throw new BadRequestException("Review cannot be empty");
        }

        if (!hasSessionEnded(booking)) {
            throw new BadRequestException("You can rate only after the booked session time has passed");
        }

        booking.setUserRating(request.getRating());
        booking.setUserReview(request.getReview().trim());

        TrainerBooking saved = bookingRepository.save(booking);
        trainerService.refreshTrainerRating(saved.getTrainer());
        return toResponse(saved);
    }

    private boolean hasSessionEnded(TrainerBooking booking) {
        String[] parts = booking.getTimeSlot().split("-");
        if (parts.length != 2) {
            throw new BadRequestException("Invalid booking time slot format");
        }

        LocalTime endTime;
        try {
            endTime = LocalTime.parse(parts[1]);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid booking time slot format");
        }

        LocalDateTime bookingEnd = LocalDateTime.of(booking.getDate(), endTime);
        return !LocalDateTime.now().isBefore(bookingEnd);
    }

    private BookingResponse toResponse(TrainerBooking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .trainerId(booking.getTrainer().getId())
                .userId(booking.getUser().getId())
                .trainerName(booking.getTrainer().getUser().getName())
                .userName(booking.getUser().getName())
                .date(booking.getDate())
                .timeSlot(booking.getTimeSlot())
                .userRating(booking.getUserRating())
                .userReview(booking.getUserReview())
                .status(booking.getStatus())
                .build();
    }

    private void validateBookingSlot(DayOfWeek dayOfWeek, String slot) {
        String[] parts = slot.split("-");
        if (parts.length != 2) {
            throw new BadRequestException("Invalid time slot format. Use HH:mm-HH:mm");
        }

        LocalTime start;
        LocalTime end;
        try {
            start = LocalTime.parse(parts[0]);
            end = LocalTime.parse(parts[1]);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid time slot format. Use HH:mm-HH:mm");
        }

        if (!end.equals(start.plusHours(1))) {
            throw new BadRequestException("Only 1-hour slots are allowed");
        }

        LocalTime open = dayOfWeek == DayOfWeek.SUNDAY ? LocalTime.of(8, 0) : LocalTime.of(6, 0);
        LocalTime close = dayOfWeek == DayOfWeek.SUNDAY ? LocalTime.of(21, 0) : LocalTime.of(22, 0);

        if (start.isBefore(open) || end.isAfter(close)) {
            throw new BadRequestException("Selected slot is outside allowed timings");
        }
    }
}
