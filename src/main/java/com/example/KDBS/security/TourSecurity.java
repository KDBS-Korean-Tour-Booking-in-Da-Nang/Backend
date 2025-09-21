package com.example.KDBS.security;

import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.model.Tour;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.TourRepository;
import com.example.KDBS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("tourSecurity")
public class TourSecurity {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TourRepository tourRepository;

    public boolean canDeleteTour(Long tourId, String userEmail){
        // Lấy user request
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        switch (currentUser.getRole()) {
            case ADMIN, STAFF -> {
                return true;
            }
            case COMPANY -> {
                // Nếu là COMPANY thì chỉ được xóa tour của mình
                Tour tour = tourRepository.findById(tourId)
                        .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
                return tour.getCompanyId() == currentUser.getUserId();
            }
            default -> {
                return false;
            }
        }
    }
}
