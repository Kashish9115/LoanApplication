package com.example.loanApplication.apiResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseApi<T> {
// APi Response
    private boolean success;
    private String message;
    private T data;
}
