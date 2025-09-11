package com.example.KDBS.service;

import com.example.KDBS.dto.request.TourRequest;
import com.example.KDBS.dto.response.TourResponse;
import com.example.KDBS.enums.TourStatus;
import com.example.KDBS.mapper.TourMapper;
import com.example.KDBS.model.Tour;
import com.example.KDBS.model.TourContent;
import com.example.KDBS.model.TourContentImg;
import com.example.KDBS.repository.TourContentImgRepository;
import com.example.KDBS.repository.TourContentRepository;
import com.example.KDBS.repository.TourRepository;
import com.example.KDBS.repository.UserRepository;
import com.example.KDBS.utils.FileUtils;
import jakarta.transaction.Transactional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class TourService {

    private final TourRepository tourRepository;
    private final TourContentRepository tourContentRepository;
    private final TourContentImgRepository tourContentImgRepository;
    private final UserRepository userRepository;
    private final TourMapper tourMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public TourService(TourRepository tourRepository,
                       TourContentRepository tourContentRepository,
                       TourContentImgRepository tourContentImgRepository,
                       UserRepository userRepository,
                       TourMapper tourMapper) {
        this.tourRepository = tourRepository;
        this.tourContentRepository = tourContentRepository;
        this.tourContentImgRepository = tourContentImgRepository;
        this.userRepository = userRepository;
        this.tourMapper = tourMapper;
    }

    /** Handle TinyMCE inline image uploads */
    public String saveEditorImage(MultipartFile file) throws IOException {
        return FileUtils.convertFileToPath(file, uploadDir, "/tours/content");
    }

    /** Create a tour with required banner image and extract content images */
    @Transactional
    public TourResponse createTour(TourRequest request, MultipartFile tourImg) throws IOException {
        if (tourImg == null || tourImg.isEmpty()) {
            throw new IllegalArgumentException("Main tour image (tourImg) is required");
        }

        var company = userRepository.findByEmail(request.getCompanyEmail())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Company email not found: " + request.getCompanyEmail()));

        Tour tour = tourMapper.toEntity(request);
        tour.setCompanyId(company.getUserId());
        tour.setTourStatus(TourStatus.NOT_APPROVED);
        tour.setTourImgPath(FileUtils.convertFileToPath(tourImg, uploadDir, "/tours/thumbnails"));

        tourRepository.save(tour);
        saveContents(request, tour);
        // Fetch the tour with contents to ensure they are included in the response
        Tour savedTour = tourRepository.findByIdWithContents(tour.getTourId())
                .orElseThrow(() -> new IllegalStateException("Failed to retrieve saved tour"));
        return tourMapper.toResponse(savedTour);
    }

    /** Get all tours */
    public List<TourResponse> getAllTours() {
        return tourRepository.findAllWithContents()
                .stream()
                .map(tourMapper::toResponse)
                .toList();
    }

    /** Get one tour */
    public TourResponse getTourById(Long id) {
        Tour tour = tourRepository.findByIdWithContents(id)
                .orElseThrow(() -> new IllegalArgumentException("Tour not found: " + id));
        return tourMapper.toResponse(tour);
    }

    /** Update tour */
    @Transactional
    public TourResponse updateTour(Long id, TourRequest request, MultipartFile tourImg) throws IOException {
        // Load existing tour
        Tour existing = tourRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tour not found: " + id));

        // Update basic fields
        tourMapper.updateEntityFromRequest(request, existing);

        // Optional new main image
        if (tourImg != null && !tourImg.isEmpty()) {
            String newPath = FileUtils.convertFileToPath(tourImg, uploadDir, "tours/thumbnails");
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
                .orElseThrow(() -> new IllegalStateException("Failed to retrieve updated tour"));
        return tourMapper.toResponse(fetchedTour);
    }

    /** Delete tour and cascade its contents & images */
    @Transactional
    public void deleteTour(Long id) {
        if (!tourRepository.existsById(id)) {
            throw new IllegalArgumentException("Tour not found: " + id);
        }
        tourRepository.deleteById(id);
    }

    /** Helper to save nested content + extract image paths */
    private void saveContents(TourRequest request, Tour tour) {
        if (request.getContents() == null) return;

        // Initialize tour contents if null
        if (tour.getContents() == null) {
            tour.setContents(new ArrayList<>());
        }

        for (TourRequest.TourContentRequest c : request.getContents()) {
            TourContent content = tourMapper.toContentEntity(c);
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