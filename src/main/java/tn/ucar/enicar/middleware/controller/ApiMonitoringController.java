package tn.ucar.enicar.middleware.controller;
import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.ucar.enicar.middleware.model.TraceRecord;
import tn.ucar.enicar.middleware.repository.TraceRecordRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiMonitoringController {

    private static final Logger logger = LoggerFactory.getLogger(ApiMonitoringController.class);
    private final TraceRecordRepository traceRecordRepository;

    public ApiMonitoringController(TraceRecordRepository traceRecordRepository) {
        this.traceRecordRepository = traceRecordRepository;
    }

    @RequestMapping("/traces")
    @Hidden
    public ResponseEntity<List<TraceRecord>> getTraces() {
        List<TraceRecord> traces = traceRecordRepository.findAll();
        return ResponseEntity.ok(traces);
    }

    @GetMapping("/evolution")
    public ResponseEntity<Map<String, Object>> getEvolutionData(@RequestParam String range) {
        List<TraceRecord> traces = traceRecordRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> evolutionData = new ArrayList<>();

        switch (range) {
            case "24h":
                LocalDateTime start24h = now.minusHours(24);
                List<TraceRecord> filtered24h = traces.stream()
                        .filter(tr -> tr.getStartTime() != null && LocalDateTime.ofInstant(tr.getStartTime(), ZoneId.systemDefault()).isAfter(start24h))
                        .collect(Collectors.toList());
                evolutionData = filtered24h.stream()
                        .collect(Collectors.groupingBy(
                                tr -> LocalDateTime.ofInstant(tr.getStartTime(), ZoneId.systemDefault()).truncatedTo(ChronoUnit.HOURS),
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> {
                                            if (list.isEmpty()) {
                                                return new HashMap<String, Object>();
                                            }
                                            Map<String, Object> dataPoint = new HashMap<>();
                                            LocalDateTime time = LocalDateTime.ofInstant(list.get(0).getStartTime(), ZoneId.systemDefault());
                                            dataPoint.put("time", time.toString());
                                            dataPoint.put("success", list.stream().filter(record -> record.getHttpStatusCode() >= 200 && record.getHttpStatusCode() < 300).count());
                                            dataPoint.put("errors", list.stream().filter(record -> record.getHttpStatusCode() >= 400).count());
                                            dataPoint.put("total", (long) list.size());
                                            return dataPoint;
                                        }
                                )
                        )).entrySet().stream()
                        .sorted(Map.Entry.comparingByKey()) // Trie par LocalDateTime
                        .map(Map.Entry::getValue)
                        .filter(map -> !map.isEmpty()) // Supprime les maps vides
                        .map(map -> (Map<String, Object>) map)
                        .collect(Collectors.toList());
                break;
            case "7d":
                LocalDateTime start7d = now.minusDays(7);
                List<TraceRecord> filtered7d = traces.stream()
                        .filter(tr -> tr.getStartTime() != null && LocalDateTime.ofInstant(tr.getStartTime(), ZoneId.systemDefault()).isAfter(start7d))
                        .collect(Collectors.toList());
                evolutionData = filtered7d.stream()
                        .collect(Collectors.groupingBy(
                                tr -> LocalDateTime.ofInstant(tr.getStartTime(), ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS),
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> {
                                            if (list.isEmpty()) {
                                                return new HashMap<String, Object>();
                                            }
                                            Map<String, Object> dataPoint = new HashMap<>();
                                            LocalDateTime time = LocalDateTime.ofInstant(list.get(0).getStartTime(), ZoneId.systemDefault());
                                            dataPoint.put("time", time.toString());
                                            dataPoint.put("success", list.stream().filter(record -> record.getHttpStatusCode() >= 200 && record.getHttpStatusCode() < 300).count());
                                            dataPoint.put("errors", list.stream().filter(record -> record.getHttpStatusCode() >= 400).count());
                                            dataPoint.put("total", (long) list.size());
                                            return dataPoint;
                                        }
                                )
                        )).entrySet().stream()
                        .sorted(Map.Entry.comparingByKey()) // Trie par LocalDateTime
                        .map(Map.Entry::getValue)
                        .filter(map -> !map.isEmpty())
                        .map(map -> (Map<String, Object>) map)
                        .collect(Collectors.toList());
                break;
            case "30d":
                LocalDateTime start30d = now.minusDays(30);
                List<TraceRecord> filtered30d = traces.stream()
                        .filter(tr -> tr.getStartTime() != null && LocalDateTime.ofInstant(tr.getStartTime(), ZoneId.systemDefault()).isAfter(start30d))
                        .collect(Collectors.toList());
                evolutionData = filtered30d.stream()
                        .collect(Collectors.groupingBy(
                                tr -> LocalDateTime.ofInstant(tr.getStartTime(), ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS),
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> {
                                            if (list.isEmpty()) {
                                                return new HashMap<String, Object>();
                                            }
                                            Map<String, Object> dataPoint = new HashMap<>();
                                            LocalDateTime time = LocalDateTime.ofInstant(list.get(0).getStartTime(), ZoneId.systemDefault());
                                            dataPoint.put("time", time.toString());
                                            dataPoint.put("success", list.stream().filter(record -> record.getHttpStatusCode() >= 200 && record.getHttpStatusCode() < 300).count());
                                            dataPoint.put("errors", list.stream().filter(record -> record.getHttpStatusCode() >= 400).count());
                                            dataPoint.put("total", (long) list.size());
                                            return dataPoint;
                                        }
                                )
                        )).entrySet().stream()
                        .sorted(Map.Entry.comparingByKey()) // Trie par LocalDateTime
                        .map(Map.Entry::getValue)
                        .filter(map -> !map.isEmpty())
                        .map(map -> (Map<String, Object>) map)
                        .collect(Collectors.toList());
                break;
            default:
                return ResponseEntity.badRequest().body(null);
        }

        response.put("data", evolutionData);
        response.put("status", 200);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/endpoints")
    public ResponseEntity<Map<String, Object>> getEndpointCalls(@RequestParam String range) {
        List<TraceRecord> traces = traceRecordRepository.findAll();

        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> endpointData = new ArrayList<>();

        // Filtrer par période
        List<TraceRecord> filteredTraces;
        switch (range) {
            case "24h":
                LocalDateTime start24h = now.minusHours(24);
                filteredTraces = traces.stream()
                        .filter(tr -> tr.getStartTime() != null && LocalDateTime.ofInstant(tr.getStartTime(), ZoneId.systemDefault()).isAfter(start24h))
                        .collect(Collectors.toList());
                break;
            case "7d":
                LocalDateTime start7d = now.minusDays(7);
                filteredTraces = traces.stream()
                        .filter(tr -> tr.getStartTime() != null && LocalDateTime.ofInstant(tr.getStartTime(), ZoneId.systemDefault()).isAfter(start7d))
                        .collect(Collectors.toList());
                break;
            case "30d":
                LocalDateTime start30d = now.minusDays(30);
                filteredTraces = traces.stream()
                        .filter(tr -> tr.getStartTime() != null && LocalDateTime.ofInstant(tr.getStartTime(), ZoneId.systemDefault()).isAfter(start30d))
                        .collect(Collectors.toList());
                break;
            default:
                return ResponseEntity.badRequest().body(null);
        }

        // Agréger par endpoint avec uniquement total et name
        Map<String, Map<String, Object>> endpointDetails = filteredTraces.stream()
                .filter(tr -> tr.getName() != null)
                .collect(Collectors.groupingBy(
                        TraceRecord::getName,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("name", list.get(0).getName());
                                    data.put("total", (long) list.size());
                                    return data;
                                }
                        )
                ));

        endpointData.addAll(endpointDetails.values());

        response.put("data", endpointData);
        response.put("status", 200);
        return ResponseEntity.ok(response);
    }

}