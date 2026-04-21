package com.fitconnect.service.command;

import com.fitconnect.dto.BookingResponse;
import com.fitconnect.entity.TrainerBooking;
import com.fitconnect.entity.enums.BookingStatus;
import com.fitconnect.repository.TrainerBookingRepository;

/**
 * Command Pattern — RejectBookingCommand (Concrete Command)
 *
 * Encapsulates the action of rejecting a trainer booking.
 * Receiver: TrainerBooking entity + TrainerBookingRepository
 */
public class RejectBookingCommand implements BookingCommand {

    private final TrainerBooking booking;
    private final TrainerBookingRepository bookingRepository;
    private final String trainerMessage;
    private final String trainerProposedTimeSlot;

    public RejectBookingCommand(TrainerBooking booking,
                                TrainerBookingRepository bookingRepository,
                                String trainerMessage,
                                String trainerProposedTimeSlot) {
        this.booking = booking;
        this.bookingRepository = bookingRepository;
        this.trainerMessage = trainerMessage;
        this.trainerProposedTimeSlot = trainerProposedTimeSlot;
    }

    @Override
    public BookingResponse execute() {
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setTrainerResponseMessage(trainerMessage);
        booking.setTrainerProposedTimeSlot(trainerProposedTimeSlot);
        TrainerBooking saved = bookingRepository.save(booking);
        return toResponse(saved);
    }

    private BookingResponse toResponse(TrainerBooking b) {
        return BookingResponse.builder()
                .id(b.getId())
                .trainerId(b.getTrainer().getId())
                .userId(b.getUser().getId())
                .trainerName(b.getTrainer().getUser().getName())
                .userName(b.getUser().getName())
                .date(b.getDate())
                .timeSlot(b.getTimeSlot())
                .userRating(b.getUserRating())
                .userReview(b.getUserReview())
                .trainerResponseMessage(b.getTrainerResponseMessage())
                .trainerProposedTimeSlot(b.getTrainerProposedTimeSlot())
                .status(b.getStatus())
                .build();
    }
}
