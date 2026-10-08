package com.example.loanApplication.controller;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.ScoreCardResponse;
import com.example.loanApplication.service.ScoreCardService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/scorecards")
@RequiredArgsConstructor
public class ScoreCardController {

    private final ScoreCardService scoreCardService;


    @PostMapping("/{customerId}/generate")
    public ResponseEntity<ResponseApi<ScoreCardResponse>> generateScoreCard(
            @PathVariable Integer customerId
    ) {

        ResponseApi<ScoreCardResponse> response =
                scoreCardService.generateScoreCard(customerId);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{customerId}")
    public ResponseEntity<ResponseApi<ScoreCardResponse>> getLatestScoreCard(
            @PathVariable Integer customerId
    ) {

        ResponseApi<ScoreCardResponse> response =
                scoreCardService.getLatestScoreCard(customerId);

        return ResponseEntity.ok(response);
    }
}