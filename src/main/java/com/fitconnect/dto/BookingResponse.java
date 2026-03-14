package com.fitconnect.dto;

import com.fitconnect.entity.enums.BookingStatus;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

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
    private BookingStatus status;
}
