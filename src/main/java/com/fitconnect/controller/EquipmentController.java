package com.fitconnect.controller;

import com.fitconnect.dto.EquipmentRequest;
import com.fitconnect.dto.EquipmentResponse;
import com.fitconnect.dto.MessageResponse;
import com.fitconnect.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/equipment")
@Tag(name = "Equipment", description = "Gym equipment management and discovery")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('GYM_OWNER')")
    @Operation(summary = "Create equipment", description = "Allows a gym owner to add equipment to a gym they own")
    public ResponseEntity<EquipmentResponse> createEquipment(@Valid @RequestBody EquipmentRequest request) {
        return ResponseEntity.ok(equipmentService.createEquipment(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GYM_OWNER')")
    @Operation(summary = "Update equipment", description = "Allows a gym owner to update existing equipment details")
    public ResponseEntity<EquipmentResponse> updateEquipment(@PathVariable Long id,
                                                             @Valid @RequestBody EquipmentRequest request) {
        return ResponseEntity.ok(equipmentService.updateEquipment(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GYM_OWNER')")
    @Operation(summary = "Delete equipment", description = "Allows a gym owner to remove equipment from a gym they own")
    public ResponseEntity<MessageResponse> deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.ok(new MessageResponse("Equipment deleted successfully"));
    }

    @GetMapping("/gym/{gymId}")
    @Operation(summary = "Get gym equipment", description = "Returns the equipment list for a specific gym")
    public ResponseEntity<List<EquipmentResponse>> getByGym(@PathVariable Long gymId) {
        return ResponseEntity.ok(equipmentService.getEquipmentByGym(gymId));
    }
}
