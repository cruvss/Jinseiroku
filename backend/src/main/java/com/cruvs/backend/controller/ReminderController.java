package com.cruvs.backend.controller;

import com.cruvs.backend.dto.reminder.ScheduledNotificationDto;
import com.cruvs.backend.response.ApiResponse;
import com.cruvs.backend.service.ReminderService;
import com.cruvs.backend.util.ApiResponseUtil;
import com.cruvs.backend.util.GetAuthUser;
import lombok.RequiredArgsConstructor;
import org.simpleframework.xml.Path;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/reminders")
public class ReminderController {
    private final ReminderService reminderService;
    private final GetAuthUser authUser;

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<ScheduledNotificationDto>>> getUpcoming(){
        List<ScheduledNotificationDto> notifications = reminderService.getUpcomingNotifications(authUser.getAuthenticatedUserId());

        return ResponseEntity.ok(ApiResponseUtil.success("Upcoming reminders retrieved",notifications));
    }

    @PatchMapping("/{id}/dismiss")
    public ResponseEntity<ApiResponse<Void>> dismiss(@PathVariable UUID id){
        reminderService.dismissNotification(authUser.getAuthenticatedUserId(),id);
        return ResponseEntity.ok(ApiResponseUtil.success("Reminder dismissed",null));
    }

    @PatchMapping("/{id}/snooze")
    public ResponseEntity<ApiResponse<Void>> snooze(@PathVariable UUID id, @RequestParam int days){
        reminderService.snoozeNotification(authUser.getAuthenticatedUserId(),id,days);
        return ResponseEntity.ok(ApiResponseUtil.success("Reminder snoozed",null));
    }

}
