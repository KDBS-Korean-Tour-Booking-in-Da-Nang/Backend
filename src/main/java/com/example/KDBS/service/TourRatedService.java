package com.example.KDBS.service;

import com.example.KDBS.dto.request.TourRatedRequest;
import com.example.KDBS.dto.response.TourRatedResponse;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.TourRatedMapper;
import com.example.KDBS.model.Tour;
import com.example.KDBS.model.TourRated;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.TourRatedRepository;
import com.example.KDBS.repository.TourRepository;
import com.example.KDBS.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TourRatedService {
    private final TourRatedRepository tourRatedRepository;
    private final TourRepository tourRepository;
    private final TourRatedMapper tourRatedMapper;
    private final UserRepository userRepository;

    /** Create tourRated */
    @Transactional
    public TourRatedResponse createTourRated(TourRatedRequest tourRatedRequest) {
        Tour tour = tourRepository.findById(tourRatedRequest.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        User user = userRepository.findByEmail(tourRatedRequest.getUserEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (tourRatedRepository.findByTour_TourIdAndUser_UserId(
                tour.getTourId(), user.getUserId()
        ).isPresent()) {
            throw new AppException(ErrorCode.TOUR_RATED_IS_EXISTED);
        }

        TourRated tourRated = tourRatedMapper.toTourRated(tourRatedRequest);
        tourRated.setTour(tour);
        tourRated.setUser(user);
        tourRated = tourRatedRepository.save(tourRated);
        return tourRatedMapper.toTourRatedResponse(tourRated);
    }

    /** Get all tourRateds */
    public List<TourRatedResponse> getAllTourRated() {
        return tourRatedRepository.findAll()
                .stream()
                .map(tourRated -> {
                    TourRatedResponse tourRatedResponse = tourRatedMapper.toTourRatedResponse(tourRated);
                    tourRatedResponse.setUsername(tourRated.getUser().getUsername());
                    return tourRatedResponse;
                })
                .toList();
    }

    /** Get tourRated by id */
    public TourRatedResponse getTourRatedById(Long id) {
        TourRated tourRated = tourRatedRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        TourRatedResponse tourRatedResponse = tourRatedMapper.toTourRatedResponse(tourRated);
        tourRatedResponse.setUsername(tourRated.getUser().getUsername());
        return tourRatedResponse;
    }

    /** Get tourRated theo tourId */
    public List<TourRatedResponse> getByTour(Long tourId) {
        return tourRatedRepository.findByTour_TourId(tourId)
                .stream()
                .map(tourRated -> {
                    TourRatedResponse tourRatedResponse = tourRatedMapper.toTourRatedResponse(tourRated);
                    tourRatedResponse.setUsername(tourRated.getUser().getUsername());
                    return tourRatedResponse;
                })
                .toList();
    }


    /** Update tourRated */
    @Transactional
    public TourRatedResponse updateTourRated(Long id, TourRatedRequest request) {
        TourRated existing = tourRatedRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        tourRatedMapper.updateTourRatedFromRequest(request, existing);

        TourRated saved = tourRatedRepository.save(existing);
        return tourRatedMapper.toTourRatedResponse(saved);
    }

    /** Delete tourRated */
    public void delete(Long id) {
        if (!tourRatedRepository.existsById(id)) {
            throw new AppException(ErrorCode.TOUR_NOT_FOUND);
        }
        tourRatedRepository.deleteById(id);
    }

}
