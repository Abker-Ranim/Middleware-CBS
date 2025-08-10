package tn.ucar.enicar.middleware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import tn.ucar.enicar.middleware.model.TraceRecord;
import tn.ucar.enicar.middleware.service.ApiMonitoringService;
import tn.ucar.enicar.middleware.controller.ApiMonitoringController;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiMonitoringControllerTest {

    @Mock
    private ApiMonitoringService apiMonitoringService;

    @InjectMocks
    private ApiMonitoringController apiMonitoringController;

    private TraceRecord traceRecord;

    @BeforeEach
    void setUp() {
        traceRecord = new TraceRecord();
        traceRecord.setHttpStatusCode(200);
        traceRecord.setStartTime(Instant.now());
        traceRecord.setName("/test-endpoint");
    }

    @Test
    void getTraces_ReturnsListOfTraces() {
        // Arrange
        when(apiMonitoringService.getAllTraces()).thenReturn(List.of(traceRecord));

        // Act
        ResponseEntity<List<TraceRecord>> response = apiMonitoringController.getTraces();

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals(traceRecord, response.getBody().get(0));
        verify(apiMonitoringService, times(1)).getAllTraces();
    }

    @Test
    void getEvolutionData_ValidRange_ReturnsData() {
        // Arrange
        List<Map<String, Object>> mockData = List.of(
                Map.of("time", "2025-08-10T10:00", "success", 5, "errors", 1, "total", 6)
        );
        when(apiMonitoringService.getEvolutionData("24h")).thenReturn(mockData);

        // Act
        ResponseEntity<Map<String, Object>> response = apiMonitoringController.getEvolutionData("24h");

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().containsKey("data"));
        assertEquals(mockData, response.getBody().get("data"));
        verify(apiMonitoringService, times(1)).getEvolutionData("24h");
    }

    @Test
    void getEvolutionData_InvalidRange_ReturnsBadRequest() {
        // Arrange
        when(apiMonitoringService.getEvolutionData(anyString())).thenReturn(List.of());

        // Act
        ResponseEntity<Map<String, Object>> response = apiMonitoringController.getEvolutionData("invalid");

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().containsKey("error"));
        verify(apiMonitoringService, times(1)).getEvolutionData("invalid");
    }

    @Test
    void getEndpointCalls_ValidRange_ReturnsData() {
        // Arrange
        List<Map<String, Object>> mockData = List.of(
                Map.of("name", "/test", "total", 10)
        );
        when(apiMonitoringService.getEndpointCalls("7d")).thenReturn(mockData);

        // Act
        ResponseEntity<Map<String, Object>> response = apiMonitoringController.getEndpointCalls("7d");

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockData, response.getBody().get("data"));
        assertEquals(200, response.getBody().get("status"));
        verify(apiMonitoringService, times(1)).getEndpointCalls("7d");
    }

    @Test
    void getEndpointCalls_InvalidRange_ReturnsBadRequest() {
        // Arrange
        when(apiMonitoringService.getEndpointCalls(anyString())).thenReturn(List.of());

        // Act
        ResponseEntity<Map<String, Object>> response = apiMonitoringController.getEndpointCalls("bad");

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().containsKey("error"));
        verify(apiMonitoringService, times(1)).getEndpointCalls("bad");
    }
}
