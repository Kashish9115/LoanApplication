package com.example.loanApplication.dto.dashboard;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentStatusDto {

    private Integer documentId;
    private String documentType;
    private String status;
}

