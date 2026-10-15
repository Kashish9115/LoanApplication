package com.example.loanApplication.dto.dashboard;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StepperStepDto {

    private String stepName;
    private String stepStatus;
    private Boolean isCompleted;
    private Boolean isCurrent;
}

