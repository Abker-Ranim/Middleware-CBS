package tn.ucar.enicar.middleware.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.ucar.enicar.middleware.model.TraceRecord;
import tn.ucar.enicar.middleware.service.ApiMonitoringService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiMonitoringController {

    private final ApiMonitoringService apiMonitoringService;

    public ApiMonitoringController(ApiMonitoringService apiMonitoringService) {
        this.apiMonitoringService = apiMonitoringService;
    }

    @GetMapping("/traces")
    public ResponseEntity<List<TraceRecord>> getTraces() {
        return ResponseEntity.ok(apiMonitoringService.getAllTraces());
    }

    @GetMapping("/evolution")
    public ResponseEntity<Map<String, Object>> getEvolutionData(@RequestParam String range) {
        List<Map<String, Object>> data = apiMonitoringService.getEvolutionData(range);
        if (data.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid range"));
        }
        return ResponseEntity.ok(Map.of("data", data, "status", 200));
    }

    @GetMapping("/endpoints")
    public ResponseEntity<Map<String, Object>> getEndpointCalls(@RequestParam String range) {
        List<Map<String, Object>> data = apiMonitoringService.getEndpointCalls(range);
        if (data.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid range"));
        }
        return ResponseEntity.ok(Map.of("data", data, "status", 200));
    }
}
