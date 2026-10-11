package com.example.loanApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartialClosureRequestDto {

    private Integer months;

    private LocalDate expectedClosureDate;

    private String reason;
}