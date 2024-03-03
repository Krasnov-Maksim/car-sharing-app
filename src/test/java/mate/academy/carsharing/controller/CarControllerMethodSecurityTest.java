package mate.academy.carsharing.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import mate.academy.carsharing.dto.car.CreateCarRequestDto;
import mate.academy.carsharing.model.Car;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CarControllerMethodSecurityTest {
    private static final CreateCarRequestDto CREATE_BMV_X5_REQUEST_DTO =
            CreateCarRequestDto.builder()
                    .model("X5")
                    .brand("BMW")
                    .type(Car.Type.SUV)
                    .inventory(10)
                    .dailyFee(BigDecimal.valueOf(125.15))
                    .build();

    private static final CreateCarRequestDto CREATE_AUDI_Q7_REQUEST_DTO =
            CreateCarRequestDto.builder()
                    .model("Q7")
                    .brand("Audi")
                    .type(Car.Type.SUV)
                    .inventory(5)
                    .dailyFee(BigDecimal.valueOf(100.75))
                    .build();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void beforeEach() throws SQLException {
        setupDatabase(dataSource);
    }

    @AfterEach
    public void afterEach() throws SQLException {
        teardown();
    }

    private void teardown() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("sql/controller/cars/clear-cars-table.sql")
            );
        }
    }

    private void setupDatabase(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("sql/controller/cars/fill-cars-table.sql")
            );
        }
    }

    @Test
    @DisplayName("User with role 'Manager' can create new car")
    @WithMockUser(roles = {"MANAGER"})
    void create_WithRoleAdmin_ThenSuccess() throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(CREATE_BMV_X5_REQUEST_DTO);
        mockMvc.perform(post("/api/cars")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();
    }

    @Test
    @DisplayName("User without role 'Manager' can't create new car")
    @WithMockUser(roles = {"NOT_MANAGER"})
    void create_WithInvalidRole_ThenError() throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(CREATE_BMV_X5_REQUEST_DTO);
        mockMvc.perform(post("/api/cars")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn();
    }

    @Test
    @DisplayName("User with role 'Manager' can delete car")
    @WithMockUser(roles = {"MANAGER"})
    void deleteById_WithRoleAdmin_ThenSuccess() throws Exception {
        Long carId = getLastCarId();
        mockMvc.perform(delete("/api/cars/{id}", carId))
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    @DisplayName("User without role 'Manager' can't delete car")
    @WithMockUser(roles = {"NOT_MANAGER"})
    void deleteById_InvalidRole_ThenError() throws Exception {
        Long carId = getLastCarId();
        mockMvc.perform(delete("/api/cars/{id}", carId))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn();
    }

    @Test
    @DisplayName("User with role 'Manager' can update car")
    @WithMockUser(roles = {"MANAGER"}, username = "manager@mail.com")
    void updateById_WithRoleAdmin_ThenSuccess() throws Exception {
        Long carId = getLastCarId();
        String jsonRequest = objectMapper.writeValueAsString(CREATE_AUDI_Q7_REQUEST_DTO);
        mockMvc.perform(put("/api/cars/{id}", carId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
    }

    @Test
    @DisplayName("User without role 'Manager' can't update car")
    @WithMockUser(roles = {"NOT_MANAGER"})
    void updateById_InvalidRole_ThenError() throws Exception {
        Long carId = getLastCarId();
        String jsonRequest = objectMapper.writeValueAsString(CREATE_AUDI_Q7_REQUEST_DTO);
        mockMvc.perform(put("/api/cars/{id}", carId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn();
    }

    @Test
    @DisplayName("Anonymous user without any role can list all cars")
    @WithMockUser(roles = {"UNKNOWN-ANONYMOUS-USER"})
    void getAll_WithAnyRole_ThenSuccess() throws Exception {
        mockMvc.perform(get("/api/cars"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @DisplayName("Anonymous user without any role can get info about any cars")
    @WithMockUser(roles = {"ANONYMOUS"})
    void getById_WithOneOfRoleAdminUserAnonymous_ThenSuccess() throws Exception {
        Long carId = getLastCarId();
        String jsonRequest = objectMapper.writeValueAsString(CREATE_AUDI_Q7_REQUEST_DTO);
        mockMvc.perform(get("/api/cars/{id}", carId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
    }

    private Long getLastCarId() {
        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM cars", Long.class);
    }
}
