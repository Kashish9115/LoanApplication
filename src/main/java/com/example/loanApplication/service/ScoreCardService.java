package com.example.loanApplication.service;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.ScoreCardResponse;

public interface ScoreCardService {

    ResponseApi<ScoreCardResponse> generateScoreCard(
            Integer customerId
    );

    ResponseApi<ScoreCardResponse> getLatestScoreCard(
            Integer customerId
    );

}