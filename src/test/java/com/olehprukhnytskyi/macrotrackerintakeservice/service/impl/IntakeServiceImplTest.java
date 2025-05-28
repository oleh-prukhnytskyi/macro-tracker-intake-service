package com.olehprukhnytskyi.macrotrackerintakeservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.olehprukhnytskyi.macrotrackerintakeservice.dto.FoodDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeResponseDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.event.RequestProcessedEvent;
import com.olehprukhnytskyi.macrotrackerintakeservice.exception.NotFoundException;
import com.olehprukhnytskyi.macrotrackerintakeservice.mapper.IntakeMapper;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Intake;
import com.olehprukhnytskyi.macrotrackerintakeservice.repository.IntakeRepository;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.FoodClientService;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.RequestDeduplicationService;
import com.olehprukhnytskyi.macrotrackerintakeservice.util.ProcessedEntityType;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class IntakeServiceImplTest {
    @Mock
    private FoodClientService foodClientService;
    @Mock
    private IntakeRepository intakeRepository;
    @Mock
    private RequestDeduplicationService requestDeduplicationService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private IntakeMapper intakeMapper;

    @InjectMocks
    private IntakeServiceImpl intakeService;

    private final Long userId = 456L;
    private final String requestId = "req1";
    private final String requestKey = "intake:" + requestId + ":" + userId;

    @Test
    @DisplayName("When valid request with existing food, should save intake and return DTO")
    void save_whenFoodExists_shouldSaveAndReturnDto() {
        // Given
        IntakeRequestDto requestDto = new IntakeRequestDto("food123");
        FoodDto foodDto = FoodDto.builder()
                .id("food123")
                .productName("Apple")
                .build();

        Intake intake = new Intake();
        Intake savedIntake = new Intake();
        savedIntake.setId(1L);
        IntakeResponseDto responseDto = IntakeResponseDto.builder()
                .id(1L)
                .foodName("Apple")
                .build();

        when(foodClientService.getFoodById("food123")).thenReturn(foodDto);
        when(intakeMapper.toModel(requestDto)).thenReturn(intake);
        doAnswer(inv -> {
            inv.<Intake>getArgument(0).setFoodName((foodDto.getProductName()));
            return null;
        }).when(intakeMapper).updateIntakeFromFoodDto(intake, foodDto);
        when(intakeRepository.save(intake)).thenReturn(savedIntake);
        when(intakeMapper.toDto(savedIntake)).thenReturn(responseDto);
        when(requestDeduplicationService.buildRequestKey(
                ProcessedEntityType.INTAKE, requestId, userId)
        ).thenReturn(requestKey);

        // When
        final IntakeResponseDto result = intakeService.save(requestDto, userId, requestId);

        // Then
        verify(intakeMapper).updateIntakeFromFoodDto(intake, foodDto);
        verify(intakeRepository).save(intake);

        ArgumentCaptor<RequestProcessedEvent> eventCaptor = ArgumentCaptor
                .forClass(RequestProcessedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(requestKey, eventCaptor.getValue().getRequestKey());

        assertEquals(responseDto, result);
        assertEquals(userId, intake.getUserId());
        assertEquals(requestDto.getFoodId(), intake.getFoodId());
        assertEquals("Apple", intake.getFoodName());
    }

    @Test
    @DisplayName("When food not found, should throw BAD_REQUEST")
    void save_whenFoodNotFound_shouldThrowBadRequest() {
        // Given
        IntakeRequestDto requestDto = new IntakeRequestDto("invalid");
        Intake intake = new Intake();

        when(intakeMapper.toModel(requestDto)).thenReturn(intake);
        when(foodClientService.getFoodById("invalid")).thenThrow(new FeignException
                .NotFound("Not found", mock(Request.class), null, null));

        // When & Then
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> intakeService.save(requestDto, userId, requestId));

        assertEquals("Food not found", ex.getMessage());
        verify(intakeRepository, never()).save(any());
    }

    @Test
    @DisplayName("When food service unavailable, should throw SERVICE_UNAVAILABLE")
    void save_whenFoodServiceUnavailable_shouldThrowServiceUnavailable() {
        // Given
        IntakeRequestDto requestDto = new IntakeRequestDto("food123");
        Intake intake = new Intake();

        when(intakeMapper.toModel(requestDto)).thenReturn(intake);
        when(foodClientService.getFoodById("food123"))
                .thenThrow(new FeignException.InternalServerError(
                        "Service Unavailable",
                        mock(Request.class),
                        null,
                        null
                ));

        // When & Then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> intakeService.save(requestDto, userId, requestId));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
        assertEquals("Food service is unavailable", ex.getReason());
        verify(intakeRepository, never()).save(any());
        verify(intakeMapper).toModel(requestDto);
    }

    @Test
    @DisplayName("Should publish event with deduplication key")
    void save_shouldPublishDeduplicationEvent() {
        // Given
        when(foodClientService.getFoodById(any())).thenReturn(new FoodDto());
        when(intakeMapper.toModel(any())).thenReturn(new Intake());
        when(requestDeduplicationService.buildRequestKey(any(), any(), any()))
                .thenReturn(requestKey);

        // When
        intakeService.save(new IntakeRequestDto("food123"), userId, requestId);

        // Then
        ArgumentCaptor<RequestProcessedEvent> captor = ArgumentCaptor
                .forClass(RequestProcessedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(requestKey, captor.getValue().getRequestKey());
    }
}
