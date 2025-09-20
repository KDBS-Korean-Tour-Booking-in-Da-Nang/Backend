package com.example.KDBS.controller;

import com.example.KDBS.dto.request.ForumPostRequest;
import com.example.KDBS.dto.response.ForumPostResponse;
import com.example.KDBS.service.ForumPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/posts")
public class ForumPostController {

        @Autowired
        private ForumPostService forumPostService;

        @PostMapping
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ForumPostResponse> createPost(@ModelAttribute ForumPostRequest forumPostRequest) throws IOException {
                // @ModelAttribute is needed since you're uploading MultipartFile(s)
                ForumPostResponse response = forumPostService.createPost(forumPostRequest);
                return ResponseEntity.ok(response);
        }

        @PutMapping("/{id}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ForumPostResponse> updatePost(
                        @PathVariable Long id,
                        @ModelAttribute ForumPostRequest updateRequest) throws IOException {
                ForumPostResponse response = forumPostService.updatePost(id, updateRequest);
                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("isAuthenticated() and @forumPostSecurity.canDeletePost(#id, #userEmail)")
        public ResponseEntity<Void> deletePost(
                        @PathVariable Long id,
                        @RequestParam String userEmail) {
                forumPostService.deletePost(id);
                return ResponseEntity.noContent().build();
        }

        @GetMapping
        public ResponseEntity<List<ForumPostResponse>> getAllPosts() {
                return ResponseEntity.ok(forumPostService.getAllPosts());
        }

        @GetMapping("/{id}")
        public ResponseEntity<ForumPostResponse> getPostById(@PathVariable Long id) {
                return ResponseEntity.ok(forumPostService.getPostById(id));
        }

        @GetMapping("/search")
        public ResponseEntity<Page<ForumPostResponse>> searchPosts(
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) List<String> hashtags,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        // sort by createdAt(flexible), default = desc(newest)
                        @RequestParam(defaultValue = "createdAt,desc") String sort) {
                String[] parts = sort.split(",");
                String field = parts[0]; // tên field entity
                Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC
                                : Sort.Direction.DESC;

                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, field));

                return ResponseEntity.ok(forumPostService.searchPosts(keyword, hashtags, pageable));
        }

        @GetMapping("/my-posts")
        public ResponseEntity<Page<ForumPostResponse>> getMyPosts(
                        @RequestHeader("User-Email") String userEmail,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt,desc") String sort) {
                String[] parts = sort.split(",");
                String field = parts[0];
                Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC
                                : Sort.Direction.DESC;

                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, field));

                return ResponseEntity.ok(forumPostService.getPostsByUser(userEmail, pageable));
        }

}
