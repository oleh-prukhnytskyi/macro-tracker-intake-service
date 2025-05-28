package com.olehprukhnytskyi.macrotrackerintakeservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.FoodDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeResponseDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.NutrimentsDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Intake;
import com.olehprukhnytskyi.macrotrackerintakeservice.repository.IntakeRepository;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.FoodClientService;
import com.olehprukhnytskyi.macrotrackerintakeservice.util.CustomHeaders;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntakeControllerTest {
    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;
    @MockitoBean
    private ValueOperations<String, String> valueOperations;
    @MockitoBean
    private FoodClientService foodClientService;
    @MockitoSpyBean
    private IntakeRepository intakeRepository;

    @BeforeAll
    static void beforeAll(
            @Autowired WebApplicationContext applicationContext
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .build();
    }

    @BeforeEach
    void cleanDb() {
        intakeRepository.deleteAll();
    }

    @Test
    @DisplayName("When request is not duplicated, should create intake and return 201 with body")
    void addIntake_whenNotDuplicated_shouldReturnCreatedWithBody() throws Exception {
        // Given
        IntakeRequestDto requestDto = new IntakeRequestDto();
        requestDto.setFoodId("food-1");
        requestDto.setAmount(150);

        IntakeResponseDto responseDto = new IntakeResponseDto();
        responseDto.setId(1L);
        responseDto.setAmount(100);
        responseDto.setNutriments(new NutrimentsDto());
        responseDto.setFoodName("Oatmeal");

        FoodDto foodDto = FoodDto.builder()
                .productName("Oatmeal")
                .nutriments(new NutrimentsDto())
                .build();

        String jsonRequest = objectMapper.writeValueAsString(new IntakeRequestDto("food-1", 100));

        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(foodClientService.getFoodById(anyString())).thenReturn(foodDto);

        // When
        MvcResult mvcResult = mockMvc.perform(post("/api/intake")
                        .header(CustomHeaders.X_USER_ID, 1L)
                        .header(CustomHeaders.X_REQUEST_ID, "req-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        IntakeResponseDto intakeResponseDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(), IntakeResponseDto.class);
        if (intakeResponseDto != null) {
            responseDto.setDate(intakeResponseDto.getDate());
        }

        assertEquals(responseDto, intakeResponseDto);
        verify(intakeRepository).save(any(Intake.class));
        verify(redisTemplate.opsForValue()).set(anyString(), eq("1"), eq(1L), eq(TimeUnit.HOURS));
        verify(foodClientService).getFoodById("food-1");
    }

    @Test
    @DisplayName("When request is duplicated, should return 200 with null body")
    void addIntake_whenDuplicated_shouldReturnOkWithNullBody() throws Exception {
        // Given
        String requestJson = objectMapper.writeValueAsString(new IntakeRequestDto("food-2", 200));
        String expected = "";

        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        // When
        MvcResult mvcResult = mockMvc.perform(post("/api/intake")
                        .header(CustomHeaders.X_USER_ID, 1L)
                        .header(CustomHeaders.X_REQUEST_ID, "req-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        assertEquals(expected, mvcResult.getResponse().getContentAsString());
    }
}
