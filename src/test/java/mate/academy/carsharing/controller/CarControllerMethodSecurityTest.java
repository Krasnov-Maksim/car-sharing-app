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
import lombok.SneakyThrows;
import mate.academy.carsharing.dto.CreateCarRequestDto;
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

    @BeforeEach
    public void beforeEach() {
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

    @SneakyThrows
    private void setupDatabase(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("sql/controller/cars/fill-cars-table.sql")
            );
        }
    }

    @Test
    @DisplayName("User with role 'Admin' can create new car")
    @WithMockUser(roles = {"ADMIN"})
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
    @DisplayName("User without role 'Admin' can't create new car")
    @WithMockUser(roles = {"NOT_ADMIN"})
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
    @DisplayName("User with role 'Admin' can delete car")
    @WithMockUser(roles = {"ADMIN"})
    void deleteById_WithRoleAdmin_ThenSuccess() throws Exception {
        mockMvc.perform(delete("/api/cars/{id}", 100))
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    @DisplayName("User without role 'Admin' can't delete car")
    @WithMockUser(roles = {"NOT_ADMIN"})
    void deleteById_InvalidRole_ThenError() throws Exception {
        mockMvc.perform(delete("/api/cars/{id}", 100))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn();
    }

    @Test
    @DisplayName("User with role 'Admin' can update car")
    @WithMockUser(roles = {"ADMIN"})
    void updateById_WithRoleAdmin_ThenSuccess() throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(CREATE_AUDI_Q7_REQUEST_DTO);
        mockMvc.perform(put("/api/cars/{id}", 1)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
    }

    @Test
    @DisplayName("User without role 'Admin' can't update car")
    @WithMockUser(roles = {"NOT_ADMIN"})
    void updateById_InvalidRole_ThenError() throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(CREATE_AUDI_Q7_REQUEST_DTO);
        mockMvc.perform(put("/api/cars/{id}", 1)
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
        String jsonRequest = objectMapper.writeValueAsString(CREATE_AUDI_Q7_REQUEST_DTO);
        mockMvc.perform(get("/api/cars/{id}", 1)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
    }

}
