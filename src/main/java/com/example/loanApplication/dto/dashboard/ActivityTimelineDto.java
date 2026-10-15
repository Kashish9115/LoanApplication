package com.example.loanApplication.dto.dashboard;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityTimelineDto {

    private String activityDate;
    private String activityTime;
    private String title;
    private String description;
}
