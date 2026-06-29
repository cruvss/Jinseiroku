package com.cruvs.backend.service;


import com.cruvs.backend.dto.timeline.TimelineEventDto;
import com.cruvs.backend.entity.TimelineEvent;
import com.cruvs.backend.repository.TimelineEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimelineService {

    private final TimelineEventRepository timelineEventRepository;

    public List<TimelineEventDto> getEventsByUserId(UUID userId){

        return timelineEventRepository.findAllByUserIdOrderByEventDateDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public TimelineEventDto getEventsById(UUID userId, UUID id){
        TimelineEvent event = timelineEventRepository.findById(id)
                .orElseThrow();
        if (!event.getUserId().equals(userId)){
            throw new SecurityException("Access Denied");
        }

        return mapToDto(event);
    }
    @Transactional
    public TimelineEventDto createEvent(UUID userId, TimelineEventDto dto){

        TimelineEvent event = TimelineEvent.builder()
                .userId(userId)
                .titleEncrypted(dto.getTitleEncrypted())
                .descriptionEncrypted(dto.getDescriptionEncrypted())
                .eventDate(dto.getEventDate())
                .endDate(dto.getEndDate())
                .category(dto.getCategory())
                .linkedDocumentIds(dto.getLinkedDocumentIds())
                .build();

        event = timelineEventRepository.save(event);
        return mapToDto(event);
    }
    @Transactional
    public TimelineEventDto updateEvent(UUID userId, UUID eventId,TimelineEventDto dto){

        TimelineEvent event = timelineEventRepository.findById(eventId)
                .orElseThrow();

        if (!event.getUserId().equals(userId)){
            throw new SecurityException("Access Denied");
        }
        event.setTitleEncrypted(dto.getTitleEncrypted());
        event.setDescriptionEncrypted(dto.getDescriptionEncrypted());
        event.setEventDate(dto.getEventDate());
        event.setEndDate(dto.getEndDate());
        event.setCategory(dto.getCategory());
        event.setLinkedDocumentIds(dto.getLinkedDocumentIds());

        event = timelineEventRepository.save(event);

        return mapToDto(event);
    }

    @Transactional
    public void deleteEvent(UUID userId, UUID eventId){

        TimelineEvent event = timelineEventRepository.findById(eventId)
                .orElseThrow();
        if (!event.getUserId().equals(userId)){
            throw new SecurityException("Access Denied");
        }

        timelineEventRepository.delete(event);

    }

    public TimelineEventDto mapToDto(TimelineEvent entity) {
        return TimelineEventDto.builder()
                .id(entity.getId())
                .titleEncrypted(entity.getTitleEncrypted())
                .descriptionEncrypted(entity.getDescriptionEncrypted())
                .eventDate(entity.getEventDate())
                .endDate(entity.getEndDate())
                .category(entity.getCategory())
                .linkedDocumentIds(entity.getLinkedDocumentIds())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
