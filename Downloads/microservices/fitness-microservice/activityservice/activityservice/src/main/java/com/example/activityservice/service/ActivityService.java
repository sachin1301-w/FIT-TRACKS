package com.example.activityservice.service;

import com.example.activityservice.ActivityRepository;
import com.example.activityservice.dto.ActivityRequest;
import com.example.activityservice.dto.ActivityResponse;
import com.example.activityservice.model.Activity;
//import com.fitness.userservice.service.UserValidationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Data
@Service
@RequiredArgsConstructor
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final WebClient userServiceWebClient;
    private final KafkaTemplate<String,Activity> activityKafkaTemplate;
    @Value("${kafka.topic.name}")
    private String topicName;
    public ActivityResponse trackActivity(ActivityRequest request) {
        Boolean valid = userServiceWebClient.get()
                .uri("/api/users/{id}/validate", request.getUserId()) // must match userservice's real endpoint
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        if (!Boolean.TRUE.equals(valid)) {
            throw new RuntimeException("Invalid User: " + request.getUserId());
        }

        // ...rest of the method unchanged
        Activity activity=Activity.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .duration(request.getDuration())
                .caloriesBurned(request.getCaloriesBurned())
                .startTime(request.getStartTime())
                .additionalMetrics(request.getAdditionalMetrics())
                .build();
    Activity savedActivity=activityRepository.save(activity);
    try{
        activityKafkaTemplate.send(topicName,savedActivity.getUserId(),savedActivity);
    }
    catch (Exception e){
        e.printStackTrace();
    }
    return mapToRespnse(savedActivity);
    }

    public List<ActivityResponse> getActivities(String userId) {
        return activityRepository.findByUserId(userId).stream()
                .map(this::mapToRespnse)
                .collect(Collectors.toList());
    }

    public ActivityResponse getActivity(String id) {
        return activityRepository.findById(id)
                .map(this::mapToRespnse)
                .orElseThrow(() -> new RuntimeException("Activity not found: " + id));
    }

    private ActivityResponse mapToRespnse(Activity activity) {
        ActivityResponse response=new ActivityResponse();
        response.setId(activity.getId());
        response.setUserId(activity.getUserId());
        response.setType(activity.getType());
        response.setDuration(activity.getDuration());
        response.setCaloriesBurned(activity.getCaloriesBurned());
        response.setStartTime(activity.getStartTime());
        response.setAdditionalMetrics(activity.getAdditionalMetrics());
        response.setCreateTime(activity.getCreatedAt());
        response.setUpdatedAt(activity.getUpdatedAt());
        return  response;
    }
}
