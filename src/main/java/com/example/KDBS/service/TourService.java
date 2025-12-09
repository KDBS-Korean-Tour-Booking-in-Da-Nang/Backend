package com.example.KDBS.service;

import com.example.KDBS.dto.request.TourRequest;
import com.example.KDBS.dto.response.TourPreviewResponse;
import com.example.KDBS.dto.response.TourResponse;
import com.example.KDBS.enums.Role;
import com.example.KDBS.enums.StaffTask;
import com.example.KDBS.enums.TourStatus;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.TourMapper;
import com.example.KDBS.model.Tour;
import com.example.KDBS.model.TourContent;
import com.example.KDBS.model.TourContentImg;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.*;
import com.example.KDBS.utils.FileStorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TourService {
    private final TourRepository tourRepository;
    private final TourContentRepository tourContentRepository;
    private final TourContentImgRepository tourContentImgRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final TourMapper tourMapper;
    private final StaffService staffService;
    private final FileStorageService fileStorageService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /** Handle TinyMCE inline image uploads */
    public String saveEditorImage(MultipartFile file) throws IOException {
        return fileStorageService.uploadFile(file, "/tours/content");
    }

    /** Create a tour with required banner image and extract content images */
    @Transactional
    public TourResponse createTour(TourRequest request, MultipartFile tourImg) throws IOException {
        if (tourImg == null || tourImg.isEmpty()) {
            throw new AppException(ErrorCode.MAIN_TOUR_IMAGE_IS_REQUIRED);
        }
        var company = userRepository.findByEmailAndRole(request.getCompanyEmail(), Role.COMPANY)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND_WITH_EMAIL));


        //Tour expiration date must be after tour deadline + 1 day(so if is 7 days, must be at least 8 days later)
        if (LocalDate.now().plusDays(request.getTourCheckDays() + 1).isAfter(request.getTourExpirationDate())){
            throw new AppException(ErrorCode.TOUR_DEADLINE_EXCEEDS_EXPIRATION_DATE);
        }

        Tour tour = tourMapper.toTour(request);
        tour.setCompanyId(company.getUserId());
        tour.setTourImgPath(fileStorageService.uploadFile(tourImg, "/tours/thumbnails"));
        tour.setTourStatus(TourStatus.NOT_APPROVED);
        tourRepository.save(tour);
        saveContents(request, tour);
        // Fetch the tour with contents to ensure they are included in the response
        Tour savedTour = tourRepository.findByIdWithContents(tour.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.MAIN_TOUR_IMAGE_IS_REQUIRED));

        return tourMapper.toTourResponse(savedTour);
    }

    /** Get all tours */
    public List<TourResponse> getAllTours() {
        return tourRepository.findAllWithContents()
                .stream()
                .map(tourMapper::toTourResponse)
                .toList();
    }

    public List<TourResponse> getAllPublicTours() {
        return tourRepository.findAllPublicTours(TourStatus.PUBLIC)
                .stream()
                .map(tourMapper::toTourResponse)
                .toList();
    }


    /** Get one tour */
    public TourResponse getTourById(Long tourId) {
        Tour tour = tourRepository.findByIdWithContents(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        return tourMapper.toTourResponse(tour);
    }

    //**Get tours for preview */
    public TourPreviewResponse getTourPreviewById(Long tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        TourPreviewResponse tourPreviewResponse = tourMapper.toTourPreviewResponse(tour);
        tourPreviewResponse.setTourUrl(frontendUrl + "/tour/" + tour.getTourId());
        return tourPreviewResponse;
    }

    public List<TourResponse> getAllToursByCompanyId(int companyId) {
        return tourRepository.findAllByCompanyId(companyId)
                .stream()
                .map(tourMapper::toTourResponse)
                .toList();
    }

    /** Search tours */
    public Page<TourResponse> searchToursWithFilters(String keyword,
                                                     BigDecimal minPrice,
                                                     BigDecimal maxPrice,
                                                     Double minRating,
                                                     Pageable pageable) {
        String normalized = (keyword == null) ? null : keyword.toLowerCase();
        return tourRepository.searchByKeywordAndFilters(normalized, minPrice, maxPrice, minRating, pageable)
                .map(tourMapper::toTourResponse);
    }

    /** Update tour */
    @Transactional
    public TourResponse updateTour(Long tourId, TourRequest request, MultipartFile tourImg) throws IOException {
        // Load existing tour
        Tour existing = tourRepository.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        // Update basic fields
        tourMapper.updateTourFromRequest(request, existing);

        // Optional new main image
        if (tourImg != null && !tourImg.isEmpty()) {
            // Ensure leading slash so returned path is "/uploads/tours/thumbnails/..."
            String newPath = fileStorageService.uploadFile(tourImg, "/tours/thumbnails");
            existing.setTourImgPath(newPath);
        }

        // Clear the tour's contents list to ensure no old contents remain
        if (existing.getContents() != null) {
            existing.getContents().clear();
        }

        // Save new contents + images
        saveContents(request, existing);
        existing.setTourStatus(TourStatus.NOT_APPROVED);

        // Save the tour
        Tour saved = tourRepository.save(existing);
        // Fetch the tour with contents to ensure they are included in the response
        Tour fetchedTour = tourRepository.findByIdWithContents(saved.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.FAILED_TO_RETRIVE_UPDATED_TOUR));
        return tourMapper.toTourResponse(fetchedTour);
    }

    /** Delete tour and cascade its contents & images */
    @Transactional
    public void deleteTour(Long tourId, String userEmail) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Check authorization
        if (currentUser.getRole() == Role.COMPANY &&
                tour.getCompanyId() != (currentUser.getUserId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        boolean hasBookings = bookingRepository.existsByTour_TourId(tourId);

        if (hasBookings) {
            tour.setTourStatus(TourStatus.DISABLED);
        }
        else tourRepository.deleteById(tourId);
    }

    @Transactional
    public TourResponse changeTourStatus(Long tourId, TourStatus tourStatus) {
        staffService.getAuthorizedStaff(StaffTask.APPROVE_TOUR_BOOKING_AND_APPROVE_ARTICLE);

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        tour.setTourStatus(tourStatus);
        return tourMapper.toTourResponse(tour);
    }

    /** Helper to save nested content + extract image paths */
    private void saveContents(TourRequest request, Tour tour) {
        if (request.getContents() == null)
            return;

        // Initialize tour contents if null
        if (tour.getContents() == null) {
            tour.setContents(new ArrayList<>());
        }

        for (TourRequest.TourContentRequest c : request.getContents()) {
            TourContent content = tourMapper.toTourContent(c);
            content.setTour(tour);
            tourContentRepository.save(content);
            tour.getContents().add(content);

            for (String path : extractImagePaths(c.getTourContentDescription())) {
                TourContentImg img = new TourContentImg();
                img.setTourContent(content);
                img.setImgPath(path);
                tourContentImgRepository.save(img);
            }
        }
    }

    /** Extract <img src="..."> paths from HTML */
    private List<String> extractImagePaths(String html) {
        List<String> paths = new ArrayList<>();
        if (html != null && !html.isBlank()) {
            Document doc = Jsoup.parse(html);
            for (Element img : doc.select("img[src]")) {
                paths.add(img.attr("src"));
            }
        }
        return paths;
    }
}