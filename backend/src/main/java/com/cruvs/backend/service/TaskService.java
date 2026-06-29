package com.cruvs.backend.service;

import com.cruvs.backend.dto.task.TaskCompletionDto;
import com.cruvs.backend.dto.task.TaskDto;
import com.cruvs.backend.entity.Task;
import com.cruvs.backend.entity.TaskCompletion;
import com.cruvs.backend.entity.VaultDocument;
import com.cruvs.backend.repository.TaskCompletionRepository;
import com.cruvs.backend.repository.TaskRepository;
import com.cruvs.backend.repository.VaultDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepo;
    private final TaskCompletionRepository completionRepo;
    private final VaultDocumentRepository vaultRepo;

    public List<TaskDto> getTasksByUserId(UUID userId) {
        return taskRepo.findByUserIdOrderByDueDateAsc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public TaskDto getTaskById(UUID userId, UUID taskId) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        if (!task.getUserId().equals(userId)) {
            throw new SecurityException("Access denied");
        }
        return mapToDto(task);
    }

    @Transactional
    public TaskDto createTask(UUID userId, TaskDto dto) {
        VaultDocument document = null;
        if (dto.getLinkedDocumentId() != null) {
            document = vaultRepo.findById(dto.getLinkedDocumentId())
                    .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        }

        Task task = Task.builder()
                .userId(userId)
                .titleEncrypted(dto.getTitleEncrypted())
                .descriptionEncrypted(dto.getDescriptionEncrypted())
                .category(dto.getCategory())
                .isRecurring(dto.getIsRecurring())
                .cycleType(dto.getCycleType())
                .cycleInterval(dto.getCycleInterval())
                .dueDate(dto.getDueDate())
                .leadTimeDays(dto.getLeadTimeDays() != null ? dto.getLeadTimeDays() : 7)
                .status("pending")
                .linkedDocument(document)
                .build();

        task = taskRepo.save(task);
        return mapToDto(task);
    }

    @Transactional
    public TaskDto updateTask(UUID userId, UUID taskId, TaskDto dto) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        if (!task.getUserId().equals(userId)) {
            throw new SecurityException("Access denied");
        }

        VaultDocument document = null;
        if (dto.getLinkedDocumentId() != null) {
            document = vaultRepo.findById(dto.getLinkedDocumentId())
                    .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        }

        task.setTitleEncrypted(dto.getTitleEncrypted());
        task.setDescriptionEncrypted(dto.getDescriptionEncrypted());
        task.setCategory(dto.getCategory());
        task.setRecurring(dto.getIsRecurring());
        task.setCycleType(dto.getCycleType());
        task.setCycleInterval(dto.getCycleInterval());
        task.setDueDate(dto.getDueDate());
        task.setLeadTimeDays(dto.getLeadTimeDays());
        task.setStatus(dto.getStatus());
        task.setLinkedDocument(document);

        task = taskRepo.save(task);
        return mapToDto(task);
    }

    @Transactional
    public void deleteTask(UUID userId, UUID taskId) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        if (!task.getUserId().equals(userId)) {
            throw new SecurityException("Access denied");
        }
        taskRepo.delete(task);
    }

    @Transactional
    public TaskDto completeTask(UUID userId, UUID taskId, String notesEncrypted) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        if (!task.getUserId().equals(userId)) {
            throw new SecurityException("Access denied");
        }

        // 1. Log completion record
        TaskCompletion completion = TaskCompletion.builder()
                .task(task)
                .notesEncrypted(notesEncrypted)
                .build();
        completionRepo.save(completion);

        // 2. Adjust state
        if (task.isRecurring()) {
            LocalDate nextDue = calculateNextDueDate(task.getDueDate(), task.getCycleType(), task.getCycleInterval());
            task.setDueDate(nextDue);
            task.setStatus("pending");
        } else {
            task.setStatus("completed");
        }

        task = taskRepo.save(task);
        return mapToDto(task);
    }

    public List<TaskCompletionDto> getCompletionsByTaskId(UUID userId, UUID taskId) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        if (!task.getUserId().equals(userId)) {
            throw new SecurityException("Access denied");
        }

        return completionRepo.findByTaskIdOrderByCompletedAtDesc(taskId)
                .stream()
                .map(this::mapCompletionToDto)
                .collect(Collectors.toList());
    }

    private LocalDate calculateNextDueDate(LocalDate currentDue, String cycleType, Integer interval) {
        LocalDate start = currentDue != null ? currentDue : LocalDate.now();
        if (cycleType == null || interval == null) return start;

        return switch (cycleType.toUpperCase()) {
            case "DAYS" -> start.plusDays(interval);
            case "WEEKS" -> start.plusWeeks(interval);
            case "MONTHS" -> start.plusMonths(interval);
            case "YEARS" -> start.plusYears(interval);
            default -> start;
        };
    }

    private TaskDto mapToDto(Task entity) {
        return TaskDto.builder()
                .id(entity.getId())
                .titleEncrypted(entity.getTitleEncrypted())
                .descriptionEncrypted(entity.getDescriptionEncrypted())
                .category(entity.getCategory())
                .isRecurring(entity.isRecurring())
                .cycleType(entity.getCycleType())
                .cycleInterval(entity.getCycleInterval())
                .dueDate(entity.getDueDate())
                .leadTimeDays(entity.getLeadTimeDays())
                .status(entity.getStatus())
                .linkedDocumentId(entity.getLinkedDocument() != null ? entity.getLinkedDocument().getId() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private TaskCompletionDto mapCompletionToDto(TaskCompletion entity) {
        return TaskCompletionDto.builder()
                .id(entity.getId())
                .taskId(entity.getTask().getId())
                .completedAt(entity.getCompletedAt())
                .notesEncrypted(entity.getNotesEncrypted())
                .build();
    }
}