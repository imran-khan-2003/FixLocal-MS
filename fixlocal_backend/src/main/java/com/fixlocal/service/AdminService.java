package com.fixlocal.service;

import com.fixlocal.dto.AdminStatsDTO;
import com.fixlocal.model.*;
import com.fixlocal.repository.UserRepository;
import com.fixlocal.repository.BookingRepository;
import com.fixlocal.repository.ConversationRepository;
import com.fixlocal.exception.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ConversationRepository conversationRepository;

    public Page<User> getAllUsers(Pageable pageable, String search) {
        if (search == null || search.isBlank()) {
            return userRepository.findByRole(Role.USER, pageable);
        }

        String regex = buildPrefixRegex(search);
        return userRepository.searchByRoleAndNameOrEmailRegex(Role.USER, regex, pageable);
    }

    public Page<User> getAllTradespersons(Pageable pageable, String search) {
        if (search == null || search.isBlank()) {
            return userRepository.findByRole(Role.TRADESPERSON, pageable);
        }

        String regex = buildPrefixRegex(search);
        return userRepository.searchByRoleAndNameOrEmailRegex(Role.TRADESPERSON, regex, pageable);
    }

    @Transactional
    public void blockUser(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Cannot block admin");
        }

        if (user.isBlocked()) {
            throw new ConflictException("User already blocked");
        }

        user.setBlocked(true);
        userRepository.save(user);

        log.warn("Admin blocked user {}", userId);
    }

    @Transactional
    public void unblockUser(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isBlocked()) {
            throw new ConflictException("User is not blocked");
        }

        user.setBlocked(false);
        userRepository.save(user);

        log.warn("Admin unblocked user {}", userId);
    }

    @Transactional
    public void verifyTradesperson(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.TRADESPERSON) {
            throw new BadRequestException("User is not a tradesperson");
        }

        if (user.isVerified()) {
            throw new ConflictException("Tradesperson already verified");
        }

        user.setVerified(true);
        userRepository.save(user);

        log.info("Admin verified tradesperson {}", userId);
    }

    public Page<Booking> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable);
    }

    public AdminStatsDTO getAdminStats() {

        AdminStatsDTO stats = new AdminStatsDTO();

        stats.setTotalUsers(
                userRepository.countByRole(Role.USER)
        );

        stats.setTotalTradespersons(
                userRepository.countByRole(Role.TRADESPERSON)
        );

        stats.setTotalBookings(
                bookingRepository.count()
        );

        stats.setCompletedBookings(
                bookingRepository.countByStatus(BookingStatus.COMPLETED)
        );

        stats.setPendingBookings(
                bookingRepository.countByStatus(BookingStatus.PENDING)
        );

        stats.setCancelledBookings(
                bookingRepository.countByStatus(BookingStatus.CANCELLED)
        );

        stats.setRejectedBookings(
                bookingRepository.countByStatus(BookingStatus.REJECTED)
        );

        stats.setAveragePlatformRating(
                userRepository.calculateAverageRating().orElse(0.0)
        );

        stats.setActiveConversations(
                conversationRepository.count()
        );

        stats.setPendingVerifications(
                userRepository.countByRoleAndVerifiedFalse(Role.TRADESPERSON)
        );

        stats.setBlockedAccounts(
                userRepository.countByBlockedTrue()
        );

        return stats;
    }

    private String buildPrefixRegex(String prefix) {
        return "^" + Pattern.quote(prefix);
    }
}