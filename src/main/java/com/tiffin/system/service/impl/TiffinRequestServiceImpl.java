package com.tiffin.system.service.impl;

import com.tiffin.system.dto.CreateTiffinRequest;
import com.tiffin.system.dto.MenuDto;
import com.tiffin.system.dto.MenuItemDto;
import com.tiffin.system.dto.ReviewRequestDto;
import com.tiffin.system.dto.TiffinRequestDto;
import com.tiffin.system.entity.ComboPackage;
import com.tiffin.system.entity.TiffinRecord;
import com.tiffin.system.entity.TiffinRequest;
import com.tiffin.system.entity.User;
import com.tiffin.system.entity.enums.RecordStatus;
import com.tiffin.system.entity.enums.RequestStatus;
import com.tiffin.system.entity.enums.UserStatus;
import com.tiffin.system.exception.BadRequestException;
import com.tiffin.system.exception.ResourceNotFoundException;
import com.tiffin.system.repository.ComboPackageRepository;
import com.tiffin.system.repository.TiffinRecordRepository;
import com.tiffin.system.repository.TiffinRequestRepository;
import com.tiffin.system.repository.UserRepository;
import com.tiffin.system.service.AuditService;
import com.tiffin.system.service.MenuService;
import com.tiffin.system.service.PriceService;
import com.tiffin.system.service.TiffinRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TiffinRequestServiceImpl implements TiffinRequestService {

    private final TiffinRequestRepository tiffinRequestRepository;
    private final TiffinRecordRepository tiffinRecordRepository;
    private final UserRepository userRepository;
    private final ComboPackageRepository comboPackageRepository;
    private final MenuService menuService;
    private final PriceService priceService;
    private final AuditService auditService;
    private final com.tiffin.system.service.EmailService emailService;

    @Override
    @Transactional
    public TiffinRequestDto createRequest(CreateTiffinRequest request, String currentUserEmail) {
        User user;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
        } else {
            user = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("User account is inactive. Cannot submit tiffin requests.");
        }

        // Check for existing active requests on the same service date
        List<RequestStatus> excluded = Arrays.asList(RequestStatus.REJECTED, RequestStatus.CANCELLED);
        List<TiffinRequest> existing = tiffinRequestRepository.findExistingActiveRequests(user.getId(), request.getServiceDate(), excluded);
        if (!existing.isEmpty()) {
            throw new BadRequestException("You already have an active request for " + request.getServiceDate() + " (Status: " + existing.get(0).getStatus() + ")");
        }

        String specialInstructions = request.getSpecialInstructions();
        if (request.getComboName() != null && !request.getComboName().isEmpty()) {
            specialInstructions = (specialInstructions != null && !specialInstructions.isEmpty())
                    ? "[" + request.getComboName() + "] " + specialInstructions
                    : "[" + request.getComboName() + "]";
        }

        TiffinRequest tiffinRequest = TiffinRequest.builder()
                .user(user)
                .serviceDate(request.getServiceDate())
                .tiffinType(request.getTiffinType())
                .specialInstructions(specialInstructions)
                .status(RequestStatus.PENDING)
                .build();

        TiffinRequest saved = tiffinRequestRepository.save(tiffinRequest);

        auditService.logAction("CREATE_TIFFIN_REQUEST", "TiffinRequest", saved.getId().toString(),
                currentUserEmail, "Requested " + saved.getTiffinType() + " for " + saved.getServiceDate());

        return mapToDto(saved);
    }

    @Override
    @Transactional
    public TiffinRequestDto approveRequest(Long requestId, String adminEmail) {
        TiffinRequest request = tiffinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Cannot approve request with current status: " + request.getStatus());
        }

        request.setStatus(RequestStatus.APPROVED);
        request.setReviewedBy(adminEmail);
        request.setReviewedAt(LocalDateTime.now());
        TiffinRequest savedRequest = tiffinRequestRepository.save(request);

        // Check if instructions contain a combo name like [Full Thali] or [Half Thali]
        BigDecimal chargedAmount = null;
        String menuSnapshot = null;

        if (request.getSpecialInstructions() != null && request.getSpecialInstructions().startsWith("[")) {
            int closingBracket = request.getSpecialInstructions().indexOf("]");
            if (closingBracket > 1) {
                String comboName = request.getSpecialInstructions().substring(1, closingBracket);
                Optional<ComboPackage> matchedCombo = comboPackageRepository.findAll().stream()
                        .filter(c -> c.getName().equalsIgnoreCase(comboName))
                        .findFirst();

                if (matchedCombo.isPresent()) {
                    ComboPackage combo = matchedCombo.get();
                    chargedAmount = combo.getPrice();
                    if (combo.getIncludedItems() != null && !combo.getIncludedItems().isEmpty()) {
                        menuSnapshot = combo.getName() + " (" + String.join(", ", combo.getIncludedItems()) + ")";
                    } else {
                        menuSnapshot = combo.getName() + " - " + (combo.getDescription() != null ? combo.getDescription() : "Standard Thali");
                    }
                }
            }
        }

        // Fallback to menu snapshot and price config if not matched
        if (menuSnapshot == null) {
            MenuDto menu = menuService.getMenuByDate(request.getServiceDate());
            if (menu != null && menu.getItems() != null && !menu.getItems().isEmpty()) {
                menuSnapshot = menu.getTitle() + ": " + menu.getItems().stream()
                        .map(MenuItemDto::getName)
                        .collect(Collectors.joining(", "));
            } else {
                menuSnapshot = (request.getTiffinType() == com.tiffin.system.entity.enums.TiffinType.FULL ? "Full Thali" : "Half Thali")
                        + " Meal (Dal, Sabzi, Roti, Rice, Salad)";
            }
        }

        if (chargedAmount == null) {
            chargedAmount = priceService.getPriceForTypeAndDate(request.getTiffinType(), request.getServiceDate());
        }

        // Create immutable billable daily consumption record
        TiffinRecord record = TiffinRecord.builder()
                .request(savedRequest)
                .user(savedRequest.getUser())
                .serviceDate(savedRequest.getServiceDate())
                .tiffinType(savedRequest.getTiffinType())
                .menuSnapshot(menuSnapshot)
                .chargedAmount(chargedAmount)
                .status(RecordStatus.UNPAID)
                .build();

        tiffinRecordRepository.save(record);

        auditService.logAction("APPROVE_TIFFIN_REQUEST", "TiffinRequest", requestId.toString(), adminEmail,
                "Approved request for " + savedRequest.getUser().getEmail() + " on " + savedRequest.getServiceDate() + " with charged amount Rs. " + chargedAmount);

        // Async Email Notification to customer
        try {
            emailService.sendRequestStatusEmail(savedRequest.getUser(), savedRequest);
        } catch (Exception e) {
            // graceful non-blocking
        }

        return mapToDto(savedRequest);
    }

    @Override
    @Transactional
    public TiffinRequestDto rejectRequest(Long requestId, ReviewRequestDto reviewDto, String adminEmail) {
        TiffinRequest request = tiffinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Cannot reject request with current status: " + request.getStatus());
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setReviewedBy(adminEmail);
        request.setRejectionReason(reviewDto != null ? reviewDto.getRejectionReason() : "Rejected by admin");
        request.setReviewedAt(LocalDateTime.now());
        TiffinRequest saved = tiffinRequestRepository.save(request);

        auditService.logAction("REJECT_TIFFIN_REQUEST", "TiffinRequest", requestId.toString(), adminEmail,
                "Rejected request for " + saved.getUser().getEmail() + " on " + saved.getServiceDate() + ". Reason: " + saved.getRejectionReason());

        // Async Email Notification to customer
        try {
            emailService.sendRequestStatusEmail(saved.getUser(), saved);
        } catch (Exception e) {
            // graceful non-blocking
        }

        return mapToDto(saved);
    }

    @Override
    @Transactional
    public TiffinRequestDto cancelRequest(Long requestId, String userEmail) {
        TiffinRequest request = tiffinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Only pending requests can be cancelled. Current status: " + request.getStatus());
        }

        if (!request.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new BadRequestException("You do not have permission to cancel this request");
        }

        request.setStatus(RequestStatus.CANCELLED);
        TiffinRequest saved = tiffinRequestRepository.save(request);

        auditService.logAction("CANCEL_TIFFIN_REQUEST", "TiffinRequest", requestId.toString(), userEmail,
                "Cancelled tiffin request for " + saved.getServiceDate());

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TiffinRequestDto> getRequests(Long userId, RequestStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return tiffinRequestRepository.searchRequests(userId, status, startDate, endDate, pageable)
                .map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TiffinRequestDto> getMyRequests(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        return tiffinRequestRepository.findByUserIdOrderByServiceDateDesc(user.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TiffinRequestDto getRequestById(Long id) {
        return tiffinRequestRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));
    }

    private TiffinRequestDto mapToDto(TiffinRequest req) {
        String comboName = null;
        if (req.getSpecialInstructions() != null && req.getSpecialInstructions().startsWith("[")) {
            int closing = req.getSpecialInstructions().indexOf("]");
            if (closing > 1) {
                comboName = req.getSpecialInstructions().substring(1, closing);
            }
        }

        return TiffinRequestDto.builder()
                .id(req.getId())
                .userId(req.getUser().getId())
                .userName(req.getUser().getFullName())
                .userEmail(req.getUser().getEmail())
                .userDepartment(req.getUser().getDepartment())
                .serviceDate(req.getServiceDate())
                .tiffinType(req.getTiffinType())
                .comboName(comboName)
                .specialInstructions(req.getSpecialInstructions())
                .status(req.getStatus())
                .reviewedBy(req.getReviewedBy())
                .rejectionReason(req.getRejectionReason())
                .reviewedAt(req.getReviewedAt())
                .createdAt(req.getCreatedAt())
                .build();
    }
}