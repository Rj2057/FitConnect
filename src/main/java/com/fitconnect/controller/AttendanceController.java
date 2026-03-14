package com.fitconnect.controller;

import com.fitconnect.dto.AttendanceCheckInRequest;
import com.fitconnect.dto.AttendanceResponse;
import com.fitconnect.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
@Tag(name = "Attendance", description = "Gym attendance and check-in operations")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasRole('GYM_USER')")
    @Operation(summary = "Check in to gym", description = "Creates an attendance record and updates the user's streak. If gymId is omitted, active membership gym is used")
    public ResponseEntity<AttendanceResponse> checkIn(@RequestBody(required = false) AttendanceCheckInRequest request) {
        return ResponseEntity.ok(attendanceService.checkIn(request));
    }
}
