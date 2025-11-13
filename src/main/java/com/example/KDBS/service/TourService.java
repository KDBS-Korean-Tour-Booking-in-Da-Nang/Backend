package com.example.KDBS.service;

import com.example.KDBS.dto.request.TourRequest;
import com.example.KDBS.dto.response.TourPreviewResponse;
import com.example.KDBS.dto.response.TourResponse;
import com.example.KDBS.enums.Role;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.TourMapper;
import com.example.KDBS.model.Tour;
import com.example.KDBS.model.TourContent;
import com.example.KDBS.model.TourContentImg;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.TourContentImgRepository;
import com.example.KDBS.repository.TourContentRepository;
import com.example.KDBS.repository.TourRepository;
import com.example.KDBS.repository.UserRepository;
import com.example.KDBS.utils.FileUtils;
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
    private final TourMapper tourMapper;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${file.upload-dir}")
    private String uploadDir;

    /** Handle TinyMCE inline image uploads */
    public String saveEditorImage(MultipartFile file) throws IOException {
        return FileUtils.convertFileToPath(file, uploadDir, "/tours/content");
    }

    /** Create a tour with required banner image and extract content images */
    @Transactional
    public TourResponse createTour(TourRequest request, MultipartFile tourImg) throws IOException {
        if (tourImg == null || tourImg.isEmpty()) {
            throw new AppException(ErrorCode.MAIN_TOUR_IMAGE_IS_REQUIRED);
        }

        var company = userRepository.findByEmail(request.getCompanyEmail())
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND_WITH_EMAIL));

        //Tour expiration date must be after tour deadline + 1 day(so if is 7 days, must be at least 8 days later)
        if (LocalDate.now().plusDays(request.getTourDeadline() + 1).isAfter(request.getTourExpirationDate())){
            throw new AppException(ErrorCode.TOUR_DEADLINE_EXCEEDS_EXPIRATION_DATE);
        }

        Tour tour = tourMapper.toTour(request);
        tour.setCompanyId(company.getUserId());
        tour.setTourImgPath(FileUtils.convertFileToPath(tourImg, uploadDir, "/tours/thumbnails"));

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

    /** Get one tour */
    public TourResponse getTourById(Long id) {
        Tour tour = tourRepository.findByIdWithContents(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        return tourMapper.toTourResponse(tour);
    }

    //**Get tours for preview */
    public TourPreviewResponse getTourPreviewById(Long id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
        TourPreviewResponse tourPreviewResponse = tourMapper.toTourPreviewResponse(tour);
        tourPreviewResponse.setTourUrl(frontendUrl + "/tour/" + tour.getTourId());
        return tourPreviewResponse;
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
    public TourResponse updateTour(Long id, TourRequest request, MultipartFile tourImg) throws IOException {
        // Load existing tour
        Tour existing = tourRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        // Update basic fields
        tourMapper.updateTourFromRequest(request, existing);

        // Optional new main image
        if (tourImg != null && !tourImg.isEmpty()) {
            // Ensure leading slash so returned path is "/uploads/tours/thumbnails/..."
            String newPath = FileUtils.convertFileToPath(tourImg, uploadDir, "/tours/thumbnails");
            existing.setTourImgPath(newPath);
        }

        // Clear the tour's contents list to ensure no old contents remain
        if (existing.getContents() != null) {
            existing.getContents().clear();
        }

        // Save new contents + images
        saveContents(request, existing);

        // Save the tour
        Tour saved = tourRepository.save(existing);
        // Fetch the tour with contents to ensure they are included in the response
        Tour fetchedTour = tourRepository.findByIdWithContents(saved.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.FAILED_TO_RETRIVE_UPDATED_TOUR));
        return tourMapper.toTourResponse(fetchedTour);
    }

    /** Delete tour and cascade its contents & images */
    @Transactional
    public void deleteTour(Long id, String userEmail) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Check authorization
        if (currentUser.getRole() == Role.COMPANY &&
                tour.getCompanyId() != (currentUser.getUserId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        tourRepository.deleteById(id);
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