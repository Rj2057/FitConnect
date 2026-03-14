package com.fitconnect.service;

import com.fitconnect.dto.EquipmentRequest;
import com.fitconnect.dto.EquipmentResponse;
import com.fitconnect.entity.Equipment;
import com.fitconnect.entity.Gym;
import com.fitconnect.entity.User;
import com.fitconnect.exception.ResourceNotFoundException;
import com.fitconnect.exception.UnauthorizedException;
import com.fitconnect.repository.EquipmentRepository;
import com.fitconnect.repository.GymRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final GymRepository gymRepository;
    private final CurrentUserService currentUserService;

    public EquipmentService(EquipmentRepository equipmentRepository,
                            GymRepository gymRepository,
                            CurrentUserService currentUserService) {
        this.equipmentRepository = equipmentRepository;
        this.gymRepository = gymRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public EquipmentResponse createEquipment(EquipmentRequest request) {
        User owner = currentUserService.getCurrentUser();
        Gym gym = getOwnedGym(request.getGymId(), owner.getId());

        Equipment equipment = Equipment.builder()
                .gym(gym)
                .equipmentName(request.getEquipmentName())
                .quantity(request.getQuantity())
                .condition(request.getCondition())
                .build();

        return toResponse(equipmentRepository.save(equipment));
    }

    @Transactional
    public EquipmentResponse updateEquipment(Long id, EquipmentRequest request) {
        User owner = currentUserService.getCurrentUser();
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        if (!equipment.getGym().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("Only gym owner can update equipment");
        }

        if (!equipment.getGym().getId().equals(request.getGymId())) {
            getOwnedGym(request.getGymId(), owner.getId());
            equipment.setGym(gymRepository.getReferenceById(request.getGymId()));
        }

        equipment.setEquipmentName(request.getEquipmentName());
        equipment.setQuantity(request.getQuantity());
        equipment.setCondition(request.getCondition());

        return toResponse(equipmentRepository.save(equipment));
    }

    @Transactional
    public void deleteEquipment(Long id) {
        User owner = currentUserService.getCurrentUser();
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        if (!equipment.getGym().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("Only gym owner can delete equipment");
        }

        equipmentRepository.delete(equipment);
    }

    public List<EquipmentResponse> getEquipmentByGym(Long gymId) {
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));
        return equipmentRepository.findByGym(gym).stream().map(this::toResponse).toList();
    }

    private Gym getOwnedGym(Long gymId, Long ownerId) {
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));

        if (!gym.getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedException("You do not own this gym");
        }

        return gym;
    }

    private EquipmentResponse toResponse(Equipment equipment) {
        return EquipmentResponse.builder()
                .id(equipment.getId())
                .gymId(equipment.getGym().getId())
                .equipmentName(equipment.getEquipmentName())
                .quantity(equipment.getQuantity())
                .condition(equipment.getCondition())
                .build();
    }
}
