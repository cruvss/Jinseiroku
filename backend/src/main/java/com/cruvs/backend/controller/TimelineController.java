package com.cruvs.backend.controller;

import com.cruvs.backend.dto.timeline.TimelineEventDto;
import com.cruvs.backend.response.ApiResponse;
import com.cruvs.backend.service.TimelineService;
import com.cruvs.backend.util.ApiResponseUtil;
import com.cruvs.backend.util.GetAuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/timeline")
public class TimelineController {
    private final TimelineService timelineService;
    private final GetAuthUser authUser;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TimelineEventDto>>> getEvents(){
        List<TimelineEventDto> events = timelineService.getEventsByUserId(authUser.getAuthenticatedUserId());
        return ResponseEntity.ok(ApiResponseUtil.success("Timeline events retrieved",events));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TimelineEventDto>> getEvent(@PathVariable("id") UUID id){
        TimelineEventDto event = timelineService.getEventsById(authUser.getAuthenticatedUserId(),id);

        return ResponseEntity.ok(ApiResponseUtil.success("Event details retrieved",event));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TimelineEventDto>> createEvent(@RequestBody TimelineEventDto dto){
        TimelineEventDto event = timelineService.createEvent(authUser.getAuthenticatedUserId(),dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseUtil.created("Event created sucessfully",event));

    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TimelineEventDto>> updateEvent(@PathVariable("id") UUID id,@RequestBody TimelineEventDto dto){

        TimelineEventDto event = timelineService.updateEvent(authUser.getAuthenticatedUserId(),id,dto);

        return ResponseEntity.ok(ApiResponseUtil.success("Event updated successfully",event));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable("id") UUID id){
        timelineService.deleteEvent(authUser.getAuthenticatedUserId(),id);
        return ResponseEntity.ok(ApiResponseUtil.success("Event deleted successfully",null));
    }


}
