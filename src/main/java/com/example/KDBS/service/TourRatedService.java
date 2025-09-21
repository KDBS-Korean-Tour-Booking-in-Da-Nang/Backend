package com.example.KDBS.service;

import com.example.KDBS.dto.request.TourRatedRequest;
import com.example.KDBS.dto.response.TourRatedResponse;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.TourRatedMapper;
import com.example.KDBS.model.*;
import com.example.KDBS.repository.TourRatedRepository;
import com.example.KDBS.repository.TourRepository;
import com.example.KDBS.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class TourRatedService {

    @Autowired
    private TourRatedRepository tourRatedRepository;
    @Autowired
    private TourRepository tourRepository;
    @Autowired
    private TourRatedMapper tourRatedMapper;
    @Autowired
    private UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    /** Create tourRated */
    @Transactional
    public TourRatedResponse createTourRated(TourRatedRequest tourRatedRequest) throws IOException{
        Tour tour = tourRepository.findById(tourRatedRequest.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND,tourRatedRequest.getTourId()));

        User user = userRepository.findByEmail(tourRatedRequest.getUserEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

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
                .map(tourRatedMapper::toTourRatedResponse)
                .toList();
    }

    /** Get tourRated by id */
    public TourRatedResponse getTourRatedById(Long id) {
        TourRated rated = tourRatedRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND,id));
        return tourRatedMapper.toTourRatedResponse(rated);
    }

    /** Get tourRated theo tourId */
    public List<TourRatedResponse> getByTour(Long tourId) {
        return tourRatedRepository.findByTour_TourId(tourId)
                .stream().map(tourRatedMapper::toTourRatedResponse).toList();
    }

    /** Update tourRated */
    @Transactional
    public TourRatedResponse updateTourRated(Long id, TourRatedRequest request) throws IOException {
        TourRated existing = tourRatedRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND,id));

        tourRatedMapper.updateTourRatedFromRequest(request, existing);

        TourRated saved = tourRatedRepository.save(existing);
        return tourRatedMapper.toTourRatedResponse(saved);
    }

    /** Delete tourRated */
    public void delete(Long id) {
        if (!tourRatedRepository.existsById(id)) {
            throw new AppException(ErrorCode.TOUR_NOT_FOUND,id);
        }
        tourRatedRepository.deleteById(id);
    }

}
