package com.cruvs.backend.controller;

import com.cruvs.backend.dto.task.TaskCompletionDto;
import com.cruvs.backend.dto.task.TaskDto;
import com.cruvs.backend.response.ApiResponse;
import com.cruvs.backend.service.TaskService;
import com.cruvs.backend.util.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/v1/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    private UUID getAuthenticatedUserId() {
        return (UUID) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskDto>>> getTasks() {
        List<TaskDto> list = taskService.getTasksByUserId(getAuthenticatedUserId());
        return ResponseEntity.ok(ApiResponseUtil.success("Tasks retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDto>> getTask(@PathVariable("id") UUID id) {
        TaskDto task = taskService.getTaskById(getAuthenticatedUserId(), id);
        return ResponseEntity.ok(ApiResponseUtil.success("Task details retrieved", task));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskDto>> createTask(@RequestBody TaskDto dto) {
        TaskDto task = taskService.createTask(getAuthenticatedUserId(), dto);
        return ResponseEntity.ok(ApiResponseUtil.success("Task created successfully", task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDto>> updateTask(@PathVariable("id") UUID id, @RequestBody TaskDto dto) {
        TaskDto task = taskService.updateTask(getAuthenticatedUserId(), id, dto);
        return ResponseEntity.ok(ApiResponseUtil.success("Task updated successfully", task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable("id") UUID id) {
        taskService.deleteTask(getAuthenticatedUserId(), id);
        return ResponseEntity.ok(ApiResponseUtil.success("Task deleted successfully", null));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<TaskDto>> completeTask(
            @PathVariable("id") UUID id,
            @RequestBody(required = false) String notesEncrypted
    ) {
        TaskDto task = taskService.completeTask(getAuthenticatedUserId(), id, notesEncrypted);
        return ResponseEntity.ok(ApiResponseUtil.success("Task marked complete", task));
    }

    @GetMapping("/{id}/completions")
    public ResponseEntity<ApiResponse<List<TaskCompletionDto>>> getCompletions(@PathVariable("id") UUID id) {
        List<TaskCompletionDto> completions = taskService.getCompletionsByTaskId(getAuthenticatedUserId(), id);
        return ResponseEntity.ok(ApiResponseUtil.success("Completion history retrieved", completions));
    }
}