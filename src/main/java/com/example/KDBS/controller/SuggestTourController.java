package com.example.KDBS.controller;

import com.example.KDBS.model.Tour;
import com.example.KDBS.service.SuggestTourByArticleService;
import com.example.KDBS.service.SuggestTourViaBehaviorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tour")
@RequiredArgsConstructor
public class SuggestTourController {

    private final SuggestTourByArticleService suggestTourByArticleService;
    private final SuggestTourViaBehaviorService suggestTourViaBehaviorService;

    @GetMapping("/suggestByArticle")
    public List<Tour> suggestToursByArticle(@RequestParam(required = false) int userId) {
        return suggestTourByArticleService.suggestToursForUser(userId);
    }

    @GetMapping("/suggestViaBehavior")
    public List<Tour> suggestToursViaBehavior(@RequestParam(required = false) int userId){
        return suggestTourViaBehaviorService.suggestTours(userId);
    }
}
