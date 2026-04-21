package com.fitconnect.controller;

import com.fitconnect.dto.EquipmentRentalBuyRequest;
import com.fitconnect.dto.EquipmentRentalListingRequest;
import com.fitconnect.dto.EquipmentRentalListingResponse;
import com.fitconnect.dto.EquipmentRentalTransactionResponse;
import com.fitconnect.dto.GymMarketplaceHistoryResponse;
import com.fitconnect.service.EquipmentMarketplaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/equipment-marketplace")
@Tag(name = "Equipment Marketplace", description = "Gym owner equipment rent marketplace")
public class EquipmentMarketplaceController {

    private final EquipmentMarketplaceService marketplaceService;

    public EquipmentMarketplaceController(EquipmentMarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @GetMapping("/listings")
    @PreAuthorize("hasRole('GYM_OWNER')")
    @Operation(summary = "Get marketplace listings", description = "Returns active listings from other owners")
    public ResponseEntity<List<EquipmentRentalListingResponse>> getListings() {
        return ResponseEntity.ok(marketplaceService.getAllListings());
    }

    @GetMapping("/listings/my")
    @PreAuthorize("hasRole('GYM_OWNER')")
    @Operation(summary = "Get my listings", description = "Returns active listings posted by authenticated owner")
    public ResponseEntity<List<EquipmentRentalListingResponse>> getMyListings() {
        return ResponseEntity.ok(marketplaceService.getMyListings());
    }

    @PostMapping("/listings")
    @PreAuthorize("hasRole('GYM_OWNER')")
    @Operation(summary = "Create listing", description = "Owner can post equipment for rent with monthly price and details")
    public ResponseEntity<EquipmentRentalListingResponse> createListing(@Valid @RequestBody EquipmentRentalListingRequest request) {
        return ResponseEntity.ok(marketplaceService.createListing(request));
    }

    @DeleteMapping("/listings/{listingId}")
    @PreAuthorize("hasRole('GYM_OWNER')")
    @Operation(summary = "Cancel listing", description = "Owner can cancel their active listing")
    public ResponseEntity<Void> cancelListing(@PathVariable Long listingId) {
        marketplaceService.cancelMyListing(listingId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/listings/{listingId}/buy")
    @PreAuthorize("hasRole('GYM_OWNER')")
    @Operation(summary = "Buy listing", description = "Owner can directly buy/rent listing for one of their gyms")
    public ResponseEntity<EquipmentRentalTransactionResponse> buyListing(@PathVariable Long listingId,
                                                                         @Valid @RequestBody EquipmentRentalBuyRequest request) {
        return ResponseEntity.ok(marketplaceService.buyListing(listingId, request));
    }

    @GetMapping("/transactions/my")
    @PreAuthorize("hasRole('GYM_OWNER')")
    @Operation(summary = "Get my transactions", description = "Returns buy/sell transactions with buyer and seller details")
    public ResponseEntity<List<EquipmentRentalTransactionResponse>> getMyTransactions() {
        return ResponseEntity.ok(marketplaceService.getMyTransactions());
    }

    @GetMapping("/gyms/{gymId}/history")
    @PreAuthorize("hasRole('GYM_OWNER')")
    @Operation(summary = "Get gym sold/rented history", description = "Returns monthly sold and rented history for owner's selected gym")
    public ResponseEntity<List<GymMarketplaceHistoryResponse>> getGymHistory(@PathVariable Long gymId,
                                                                             @RequestParam(defaultValue = "6") Integer months) {
        return ResponseEntity.ok(marketplaceService.getGymMarketplaceHistory(gymId, months));
    }
}
