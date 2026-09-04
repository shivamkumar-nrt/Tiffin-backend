package com.tiffin.system.service.impl;

import com.tiffin.system.dto.*;
import com.tiffin.system.entity.User;
import com.tiffin.system.entity.enums.*;
import com.tiffin.system.exception.ResourceNotFoundException;
import com.tiffin.system.repository.*;
import com.tiffin.system.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final TiffinRequestRepository tiffinRequestRepository;
    private final TiffinRecordRepository tiffinRecordRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final MenuService menuService;
    private final PriceService priceService;
    private final UserService userService;
    private final TiffinRequestService tiffinRequestService;
    private final TiffinRecordService tiffinRecordService;
    private final PaymentService paymentService;
    private final InvoiceService invoiceService;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDto getAdminDashboardStats() {
        LocalDate today = LocalDate.now();

        long totalEmployees = userRepository.count();
        long activeEmployees = userRepository.findByStatus(UserStatus.ACTIVE).size();
        long pendingRequests = tiffinRequestRepository.countByStatus(RequestStatus.PENDING);
        long totalTiffinsToday = tiffinRecordRepository.countByServiceDate(today);

        BigDecimal totalRevenueCollected = paymentRepository.sumAmountByStatus(PaymentStatus.SUCCESS);
        BigDecimal todayRevenue = tiffinRecordRepository.sumChargedAmountByServiceDate(today);

        // Calculate all unpaid across the system
        List<User> users = userRepository.findByRole(RoleType.ROLE_EMPLOYEE);
        BigDecimal totalOutstandingDues = BigDecimal.ZERO;
        for (User u : users) {
            totalOutstandingDues = totalOutstandingDues.add(userService.getUserOutstandingBalance(u.getId()));
        }

        Map<String, PriceConfigDto> prices = priceService.getCurrentPrices();
        MenuDto todayMenu = menuService.getMenuByDate(today);

        List<TiffinRequestDto> recentPending = tiffinRequestService.getRequests(null, RequestStatus.PENDING, null, null, PageRequest.of(0, 5)).getContent();
        List<PaymentDto> recentPayments = paymentService.searchPayments(null, null, PageRequest.of(0, 5)).getContent();

        return DashboardStatsDto.builder()
                .totalEmployees(totalEmployees)
                .activeEmployees(activeEmployees)
                .pendingRequests(pendingRequests)
                .approvedRequestsToday(totalTiffinsToday)
                .totalTiffinsToday(totalTiffinsToday)
                .totalOutstandingDues(totalOutstandingDues)
                .totalRevenueCollected(totalRevenueCollected != null ? totalRevenueCollected : BigDecimal.ZERO)
                .todayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO)
                .currentFullPrice(prices.get("FULL"))
                .currentHalfPrice(prices.get("HALF"))
                .todayMenu(todayMenu)
                .recentPendingRequests(recentPending)
                .recentPayments(recentPayments)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDashboardStatsDto getUserDashboardStats(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        LocalDate today = LocalDate.now();
        BigDecimal balance = userService.getUserOutstandingBalance(user.getId());
        long totalConsumed = tiffinRecordRepository.findByUserIdOrderByServiceDateDesc(user.getId()).size();
        
        List<TiffinRequestDto> myRequests = tiffinRequestService.getMyRequests(userEmail);
        long pendingCount = myRequests.stream().filter(r -> r.getStatus() == RequestStatus.PENDING).count();

        Map<String, PriceConfigDto> prices = priceService.getCurrentPrices();
        MenuDto todayMenu = menuService.getMenuByDate(today);

        List<TiffinRecordDto> recentRecords = tiffinRecordService.getMyRecords(userEmail).stream().limit(5).toList();
        List<InvoiceDto> recentInvoices = invoiceService.getMyInvoices(userEmail).stream().limit(5).toList();

        return UserDashboardStatsDto.builder()
                .outstandingBalance(balance)
                .totalTiffinsConsumed(totalConsumed)
                .pendingRequestsCount(pendingCount)
                .todayMenu(todayMenu)
                .currentFullPrice(prices.get("FULL"))
                .currentHalfPrice(prices.get("HALF"))
                .recentRequests(myRequests.stream().limit(5).toList())
                .recentRecords(recentRecords)
                .recentInvoices(recentInvoices)
                .build();
    }
}
