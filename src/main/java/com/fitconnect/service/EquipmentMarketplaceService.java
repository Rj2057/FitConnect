package com.fitconnect.service;

import com.fitconnect.dto.EquipmentRentalBuyRequest;
import com.fitconnect.dto.EquipmentRentalListingRequest;
import com.fitconnect.dto.EquipmentRentalListingResponse;
import com.fitconnect.dto.EquipmentRentalTransactionResponse;
import com.fitconnect.dto.GymMarketplaceHistoryResponse;
import com.fitconnect.entity.EquipmentRentalListing;
import com.fitconnect.entity.EquipmentRentalTransaction;
import com.fitconnect.entity.Gym;
import com.fitconnect.entity.User;
import com.fitconnect.exception.BadRequestException;
import com.fitconnect.exception.ResourceNotFoundException;
import com.fitconnect.exception.UnauthorizedException;
import com.fitconnect.repository.EquipmentRentalListingRepository;
import com.fitconnect.repository.EquipmentRentalTransactionRepository;
import com.fitconnect.repository.GymRepository;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class EquipmentMarketplaceService {

    private final EquipmentRentalListingRepository listingRepository;
    private final EquipmentRentalTransactionRepository transactionRepository;
    private final GymRepository gymRepository;
    private final CurrentUserService currentUserService;

    public EquipmentMarketplaceService(EquipmentRentalListingRepository listingRepository,
                                       EquipmentRentalTransactionRepository transactionRepository,
                                       GymRepository gymRepository,
                                       CurrentUserService currentUserService) {
        this.listingRepository = listingRepository;
        this.transactionRepository = transactionRepository;
        this.gymRepository = gymRepository;
        this.currentUserService = currentUserService;
    }

    public List<EquipmentRentalListingResponse> getAllListings() {
        User currentUser = currentUserService.getCurrentUser();
        return listingRepository.findAll().stream()
                .filter(listing -> !listing.getSellerOwner().getId().equals(currentUser.getId()))
                .map(this::toListingResponse)
                .toList();
    }

    public List<EquipmentRentalListingResponse> getMyListings() {
        User currentUser = currentUserService.getCurrentUser();
        return listingRepository.findBySellerOwner(currentUser).stream()
                .map(this::toListingResponse)
                .toList();
    }

    @Transactional
    public EquipmentRentalListingResponse createListing(EquipmentRentalListingRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        Gym sellerGym = getOwnedGym(request.getSellerGymId(), currentUser.getId());

        EquipmentRentalListing listing = EquipmentRentalListing.builder()
                .sellerOwner(currentUser)
                .sellerGym(sellerGym)
                .equipmentName(request.getEquipmentName().trim())
                .details(request.getDetails() == null ? null : request.getDetails().trim())
                .monthlyRentPrice(request.getMonthlyRentPrice())
                .build();

        return toListingResponse(listingRepository.save(listing));
    }

    @Transactional
    public EquipmentRentalTransactionResponse buyListing(Long listingId, EquipmentRentalBuyRequest request) {
        User buyerOwner = currentUserService.getCurrentUser();
        EquipmentRentalListing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (listing.getSellerOwner().getId().equals(buyerOwner.getId())) {
            throw new BadRequestException("You cannot buy your own listing");
        }

        Gym buyerGym = getOwnedGym(request.getBuyerGymId(), buyerOwner.getId());
        if (buyerGym.getId().equals(listing.getSellerGym().getId())) {
            throw new BadRequestException("Buyer gym must be different from seller gym");
        }

        EquipmentRentalTransaction transaction = EquipmentRentalTransaction.builder()
                .sellerOwner(listing.getSellerOwner())
                .buyerOwner(buyerOwner)
                .sellerGym(listing.getSellerGym())
                .buyerGym(buyerGym)
                .equipmentName(listing.getEquipmentName())
                .details(listing.getDetails())
                .monthlyRentPrice(listing.getMonthlyRentPrice())
                .build();

        EquipmentRentalTransaction saved = transactionRepository.save(transaction);
        listingRepository.delete(listing);
        return toTransactionResponse(saved);
    }

    public List<EquipmentRentalTransactionResponse> getMyTransactions() {
        User currentUser = currentUserService.getCurrentUser();
        return transactionRepository.findBySellerOwnerOrBuyerOwner(currentUser, currentUser).stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    @Transactional
    public void cancelMyListing(Long listingId) {
        User currentUser = currentUserService.getCurrentUser();
        EquipmentRentalListing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
        if (!listing.getSellerOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can cancel only your own listings");
        }
        listingRepository.delete(listing);
    }

    public List<GymMarketplaceHistoryResponse> getGymMarketplaceHistory(Long gymId, int months) {
        User currentUser = currentUserService.getCurrentUser();
        Gym gym = getOwnedGym(gymId, currentUser.getId());
        List<EquipmentRentalTransaction> transactions = transactionRepository.findBySellerGymOrBuyerGym(gym, gym);

        List<GymMarketplaceHistoryResponse> history = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int i = 0; i < Math.max(1, months); i++) {
            YearMonth target = current.minusMonths(i);
            List<EquipmentRentalTransaction> monthTx = transactions.stream()
                    .filter(tx -> YearMonth.from(tx.getPurchasedAt()).equals(target))
                    .toList();

            int soldCount = (int) monthTx.stream().filter(tx -> tx.getSellerGym().getId().equals(gym.getId())).count();
            int rentedCount = (int) monthTx.stream().filter(tx -> tx.getBuyerGym().getId().equals(gym.getId())).count();
            BigDecimal soldAmount = monthTx.stream()
                    .filter(tx -> tx.getSellerGym().getId().equals(gym.getId()))
                    .map(EquipmentRentalTransaction::getMonthlyRentPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal rentedAmount = monthTx.stream()
                    .filter(tx -> tx.getBuyerGym().getId().equals(gym.getId()))
                    .map(EquipmentRentalTransaction::getMonthlyRentPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            history.add(GymMarketplaceHistoryResponse.builder()
                    .year(target.getYear())
                    .month(target.getMonthValue())
                    .label(target.getMonth().name() + " " + target.getYear())
                    .soldCount(soldCount)
                    .rentedCount(rentedCount)
                    .soldAmount(soldAmount)
                    .rentedAmount(rentedAmount)
                    .build());
        }
        return history;
    }

    private Gym getOwnedGym(Long gymId, Long ownerId) {
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));
        if (!gym.getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedException("You do not own this gym");
        }
        return gym;
    }

    private EquipmentRentalListingResponse toListingResponse(EquipmentRentalListing listing) {
        return EquipmentRentalListingResponse.builder()
                .id(listing.getId())
                .sellerOwnerId(listing.getSellerOwner().getId())
                .sellerOwnerName(listing.getSellerOwner().getName())
                .sellerOwnerEmail(listing.getSellerOwner().getEmail())
                .sellerGymId(listing.getSellerGym().getId())
                .sellerGymName(listing.getSellerGym().getName())
                .equipmentName(listing.getEquipmentName())
                .details(listing.getDetails())
                .monthlyRentPrice(listing.getMonthlyRentPrice())
                .createdAt(listing.getCreatedAt())
                .build();
    }

    private EquipmentRentalTransactionResponse toTransactionResponse(EquipmentRentalTransaction transaction) {
        return EquipmentRentalTransactionResponse.builder()
                .id(transaction.getId())
                .sellerOwnerId(transaction.getSellerOwner().getId())
                .sellerOwnerName(transaction.getSellerOwner().getName())
                .sellerOwnerEmail(transaction.getSellerOwner().getEmail())
                .buyerOwnerId(transaction.getBuyerOwner().getId())
                .buyerOwnerName(transaction.getBuyerOwner().getName())
                .buyerOwnerEmail(transaction.getBuyerOwner().getEmail())
                .sellerGymId(transaction.getSellerGym().getId())
                .sellerGymName(transaction.getSellerGym().getName())
                .buyerGymId(transaction.getBuyerGym().getId())
                .buyerGymName(transaction.getBuyerGym().getName())
                .equipmentName(transaction.getEquipmentName())
                .details(transaction.getDetails())
                .monthlyRentPrice(transaction.getMonthlyRentPrice())
                .purchasedAt(transaction.getPurchasedAt())
                .build();
    }
}
