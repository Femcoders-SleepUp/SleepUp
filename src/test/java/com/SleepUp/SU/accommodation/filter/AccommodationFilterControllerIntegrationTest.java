package com.SleepUp.SU.accommodation.filter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class AccommodationFilterControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetAllFilteredAccommodationsWithPagination() throws Exception {

        mockMvc.perform(get("/api/accommodations/filter")
                        .param("name", "Hotel ABC")
                        .param("page", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Hotel ABC"))
                .andExpect(jsonPath("$.content[0].price").value(150.0))
                .andExpect(jsonPath("$.content[0].guestNumber").value(2))
                .andExpect(jsonPath("$.content[0].location").value("New York"))
                .andExpect(jsonPath("$.content[0].imageUrl").value("http://example.com/images/hotel_abc.jpg"));

    }
}

