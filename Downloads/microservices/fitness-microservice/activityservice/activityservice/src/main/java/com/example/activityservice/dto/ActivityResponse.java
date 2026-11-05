package com.example.activityservice.dto;

import com.example.activityservice.model.ActivityType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
@Data
public class ActivityResponse {
    private String id;
    private String userId;
    private ActivityType type;
    private Integer duration;
    private Integer caloriesBurned;
    private LocalDateTime startTime;
    private LocalDateTime createTime;

    private LocalDateTime updatedAt;
    private Map<String,Object> additionalMetrics;


}
