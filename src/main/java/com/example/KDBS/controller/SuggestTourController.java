package com.example.KDBS.controller;

import com.example.KDBS.model.Tour;
import com.example.KDBS.service.SuggestTourByArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tour")
@RequiredArgsConstructor
public class SuggestTourController {

    private final SuggestTourByArticleService suggestTourByArticleService;

    @GetMapping("/suggestByArticle")
    public List<Tour> suggestToursByArticle(@RequestParam(required = false) String email) {
        return suggestTourByArticleService.suggestToursForUser(email);
    }

    @GetMapping("/suggestViaBehavior")
    public List<Tour> suggestToursViaBehavior(@RequestParam(required = false) String email){
        return suggestTourByArticleService.suggestToursForUser(email);
    }
}
