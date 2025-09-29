package com.SleepUp.SU.security;

import com.SleepUp.SU.exceptions.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomAccessDeniedHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AccessDeniedException accessDeniedException;

    private CustomAccessDeniedHandler accessDeniedHandler;

    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        accessDeniedHandler = new CustomAccessDeniedHandler(objectMapper);

        responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        when(accessDeniedException.getMessage()).thenReturn("Forbidden access");
        when(request.getRequestURI()).thenReturn("/test/uri");
    }

    @Test
    void handle_givenAccessDeniedException_shouldWriteJsonAndSetStatus() throws Exception {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN,
                "Access Denied. Forbidden access",
                request);
        String jsonResponse = "{\"status\":\"FORBIDDEN\",\"message\":\"Access Denied. Forbidden access\",\"path\":\"/test/uri\"}";

        when(objectMapper.writeValueAsString(any(ErrorResponse.class))).thenReturn(jsonResponse);

        accessDeniedHandler.handle(request, response, accessDeniedException);

        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);

        verify(objectMapper).writeValueAsString(any(ErrorResponse.class));

        String responseOutput = responseWriter.toString();
        assertEquals(jsonResponse, responseOutput);
    }
}
