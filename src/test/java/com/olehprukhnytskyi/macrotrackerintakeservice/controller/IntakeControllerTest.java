package com.olehprukhnytskyi.macrotrackerintakeservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olehprukhnytskyi.dto.PagedResponse;
import com.olehprukhnytskyi.exception.BadRequestException;
import com.olehprukhnytskyi.macrotrackerintakeservice.config.AbstractIntegrationTest;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.FoodDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.IntakeResponseDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.NutrimentsDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.dto.UpdateIntakeRequestDto;
import com.olehprukhnytskyi.macrotrackerintakeservice.model.Intake;
import com.olehprukhnytskyi.macrotrackerintakeservice.repository.IntakeRepository;
import com.olehprukhnytskyi.macrotrackerintakeservice.service.FoodClientService;
import com.olehprukhnytskyi.util.CustomHeaders;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Sql(scripts = "classpath:database/add-intake.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/remove-intake.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class IntakeControllerTest extends AbstractIntegrationTest {
    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;
    @MockitoBean
    private ValueOperations<String, String> valueOperations;
    @MockitoBean
    private FoodClientService foodClientService;

    @Autowired
    private IntakeRepository intakeRepository;

    @BeforeAll
    static void beforeAll(
            @Autowired WebApplicationContext applicationContext
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .build();
    }

    @Test
    @DisplayName("When no intakes on given date, should return empty list")
    void findByDate_whenNoIntakes_shouldReturnEmptyList() throws Exception {
        // Given
        String date = "2030-01-01";

        // When
        MvcResult mvcResult = mockMvc.perform(
                        get("/api/intake")
                                .header(CustomHeaders.X_USER_ID, 1L)
                                .param("date", date)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        PagedResponse<IntakeResponseDto> response = objectMapper.readValue(
                jsonResponse,
                objectMapper.getTypeFactory()
                        .constructParametricType(PagedResponse.class, IntakeResponseDto.class)
        );

        assertThat(response.getData()).isEmpty();
    }

    @Test
    @DisplayName("When valid date, should return intakes for that day")
    void findByDate_whenValidDate_shouldReturnIntakesForThatDay() throws Exception {
        // Given
        String date = "2025-09-06";

        // When
        MvcResult mvcResult = mockMvc.perform(
                        get("/api/intake")
                                .header(CustomHeaders.X_USER_ID, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("date", date)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        PagedResponse<IntakeResponseDto> response = objectMapper.readValue(
                jsonResponse,
                objectMapper.getTypeFactory()
                        .constructParametricType(PagedResponse.class, IntakeResponseDto.class)
        );

        assertThat(response.getData())
                .extracting(IntakeResponseDto::getFoodName)
                .contains("Potato");
        assertThat(response.getData())
                .extracting(IntakeResponseDto::getDate)
                .contains(LocalDate.parse(date));
        assertThat(response.getPagination().getLimit()).isEqualTo(20);
    }

    @Test
    @DisplayName("When date=today, should return today's intakes")
    void findByDate_whenToday_shouldReturnTodayIntakes() throws Exception {
        // Given
        String today = LocalDate.now().toString();

        // When
        MvcResult mvcResult = mockMvc.perform(
                        get("/api/intake")
                                .header(CustomHeaders.X_USER_ID, 1L)
                                .param("date", "today")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        PagedResponse<IntakeResponseDto> response = objectMapper.readValue(
                jsonResponse,
                objectMapper.getTypeFactory()
                        .constructParametricType(PagedResponse.class, IntakeResponseDto.class)
        );

        assertThat(response.getData())
                .allMatch(dto -> dto.getDate().equals(LocalDate.parse(today)));
    }

    @Test
    @DisplayName("When invalid date, should return BAD_REQUEST")
    void findByDate_whenInvalidDate_shouldReturnBadRequest() throws Exception {
        // Given
        String invalidDate = "06-09-2025";

        // When
        mockMvc.perform(
                        get("/api/intake")
                                .header(CustomHeaders.X_USER_ID, 1L)
                                .param("date", invalidDate)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertThat(result.getResolvedException())
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining(
                                        "Invalid date format. Use 'today' or yyyy-MM-dd")
                );
    }

    @Test
    @DisplayName("When no date, should return all intakes for user")
    void findByDate_whenNoDate_shouldReturnAllIntakesForUser() throws Exception {
        // When
        MvcResult mvcResult = mockMvc.perform(
                        get("/api/intake")
                                .header(CustomHeaders.X_USER_ID, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        PagedResponse<IntakeResponseDto> response = objectMapper.readValue(
                jsonResponse,
                objectMapper.getTypeFactory()
                        .constructParametricType(PagedResponse.class, IntakeResponseDto.class)
        );

        assertThat(response.getData()).isNotEmpty();
    }

    @Test
    @DisplayName("When request is not duplicated, should return created intake")
    void addIntake_whenNotDuplicated_shouldReturnCreatedWithBody() throws Exception {
        // Given
        IntakeResponseDto responseDto = IntakeResponseDto.builder()
                .id(1L)
                .amount(100)
                .nutriments(new NutrimentsDto())
                .date(LocalDate.now())
                .foodName("Oatmeal")
                .build();

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
        assertThat(intakeResponseDto)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(responseDto);

        verify(foodClientService).getFoodById("food-1");
    }

    @Test
    @DisplayName("When request is duplicated, should return ok")
    void addIntake_whenDuplicated_shouldReturnOkBody() throws Exception {
        // Given
        String requestJson = objectMapper.writeValueAsString(new IntakeRequestDto("food-2", 200));

        FoodDto foodDto = FoodDto.builder()
                .productName("Oatmeal")
                .nutriments(new NutrimentsDto())
                .build();

        when(redisTemplate.hasKey(anyString())).thenReturn(true);
        when(foodClientService.getFoodById(anyString())).thenReturn(foodDto);

        // When
        mockMvc.perform(
                post("/api/intake")
                        .header(CustomHeaders.X_USER_ID, 1L)
                        .header(CustomHeaders.X_REQUEST_ID, "req-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.foodName").value("Oatmeal"))
                .andExpect(jsonPath("$.amount").value(200));
    }

    @Test
    @DisplayName("When request is valid, should update intake")
    void updateIntake_whenValidRequest_shouldUpdateIntake() throws Exception {
        // Given
        Long userId = 1L;
        Long intakeId = getRandomIntakeFromDb().getId();

        UpdateIntakeRequestDto requestDto = UpdateIntakeRequestDto.builder()
                .amount(100)
                .build();
        String requestJson = objectMapper.writeValueAsString(requestDto);

        IntakeResponseDto responseDto = IntakeResponseDto.builder()
                .id(intakeId)
                .foodName("Potato")
                .date(LocalDate.parse("2025-09-06"))
                .amount(100)
                .build();
        String expected = objectMapper.writeValueAsString(responseDto);

        // When
        mockMvc.perform(
                        patch("/api/intake/{id}", intakeId)
                                .header(CustomHeaders.X_USER_ID, userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(content().json(expected));
    }

    @Test
    @DisplayName("When request is valid, should delete intake")
    void deleteById_whenValidRequest_shouldDeleteIntake() throws Exception {
        // Given
        Long userId = 1L;
        Long intakeId = getRandomIntakeFromDb().getId();

        assertThat(intakeRepository.findById(intakeId)).isPresent();

        // When
        mockMvc.perform(
                delete("/api/intake/{id}", intakeId)
                        .header(CustomHeaders.X_USER_ID, userId)
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // Then
        assertThat(intakeRepository.findById(intakeId)).isEmpty();
    }

    private Intake getRandomIntakeFromDb() {
        return intakeRepository.findAll().stream()
                .findFirst()
                .orElseThrow();
    }
}
