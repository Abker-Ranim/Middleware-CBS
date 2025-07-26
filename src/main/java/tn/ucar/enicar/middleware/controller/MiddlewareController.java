package tn.ucar.enicar.middleware.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.ucar.enicar.middleware.client.CbsClient;
import tn.ucar.enicar.middleware.model.TraceRecord;
import tn.ucar.enicar.middleware.model.TransferRecord;
import tn.ucar.enicar.middleware.model.TransferRequest;
import tn.ucar.enicar.middleware.repository.TraceRecordRepository;
import tn.ucar.enicar.middleware.repository.TransferRecordRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MiddlewareController {

    private static final Logger logger = LoggerFactory.getLogger(MiddlewareController.class);
    private final CbsClient cbsClient;
    private final TransferRecordRepository transferRecordRepository;
    private final TraceRecordRepository traceRecordRepository;

    public MiddlewareController(CbsClient cbsClient, TransferRecordRepository transferRecordRepository, TraceRecordRepository traceRecordRepository) {
        this.cbsClient = cbsClient;
        this.transferRecordRepository = transferRecordRepository;
        this.traceRecordRepository = traceRecordRepository;
    }

    @GetMapping("/consult-account")
    public ResponseEntity<?> consultAccount(@RequestParam String id) {
        ResponseEntity<?> responseEntity = cbsClient.getAccount(id);
        return responseEntity;
    }

    @GetMapping("/consult-customer")
    public ResponseEntity<?> consultCustomer(@RequestParam String id) {
        ResponseEntity<?> responseEntity = cbsClient.getCustomer(id);
        return responseEntity;
    }

    @GetMapping("/consult-history")
    public ResponseEntity<?> consultHistory(@RequestParam String id) {
        ResponseEntity<?> responseEntity = cbsClient.getHistory(id);
        return responseEntity;
    }

    @PostMapping("/do-transfer")
    public ResponseEntity<?> doTransfer(@RequestBody TransferRequest request) {
        ResponseEntity<Object> response = cbsClient.doTransfer(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount()
        );

        // Mapper la réponse brute en TransferRecord si possible
        TransferRecord record = null;
        if (response.getBody() instanceof Map) {
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            record = new TransferRecord();
            record.setFromAccountId(request.getFromAccountId());
            record.setToAccountId(request.getToAccountId());
            record.setAmount(request.getAmount());
            record.setStatus(body.getOrDefault("status", "unknown").toString());
            if (body.get("fromBalance") instanceof Number) {
                record.setFromBalance(((Number) body.get("fromBalance")).doubleValue());
            }
            if (body.get("toBalance") instanceof Number) {
                record.setToBalance(((Number) body.get("toBalance")).doubleValue());
            }
        }

        // Enregistrement dans MongoDB uniquement si le transfert réussit
        if (record != null && "success".equalsIgnoreCase(record.getStatus())) {
            transferRecordRepository.save(record);
        }

        // Retourner la réponse brute avec le statut HTTP approprié
        return response;
    }
    @GetMapping("/traces")
    @Hidden
    public ResponseEntity<List<TraceRecord>> getTraces() {
        List<TraceRecord> traces = traceRecordRepository.findAll();
        return ResponseEntity.ok(traces);
    }

    @GetMapping("/evolution")
    public ResponseEntity<Map<String, Object>> getEvolutionData(@RequestParam String range) {
        List<TraceRecord> traces = traceRecordRepository.findAll();

        traces.forEach(tr -> logger.info("Trace startTime: {}, status: {}, httpStatusCode: {}", tr.getStartTime(), tr.getStatus(), tr.getHttpStatusCode()));

        Map<String, Object> response = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> evolutionData = new ArrayList<>();

        switch (range) {
            case "24h":
                LocalDateTime start24h = now.minusHours(24);
                List<TraceRecord> filtered24h = traces.stream()
                        .filter(tr -> tr.getStartTime() != null && LocalDateTime.ofInstant(tr.getStartTime(), ZoneId.systemDefault()).isAfter(start24h))
                        .peek(tr -> logger.info("Filtered trace startTime: {}, httpStatusCode: {}", tr.getStartTime(), tr.getHttpStatusCode()))
                        .collect(Collectors.toList());
                logger.info("Number of traces after 24h filter: {}", filtered24h.size());
                evolutionData = filtered24h.stream()
                        .collect(Collectors.groupingBy(
                                tr -> LocalDateTime.ofInstant(tr.getStartTime(), ZoneId.systemDefault()).truncatedTo(ChronoUnit.HOURS).toString(),
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> {
                                            if (list.isEmpty()) {
                                                return new HashMap<String, Object>();
                                            }
                                            Map<String, Object> dataPoint = new HashMap<>();
                                            dataPoint.put("time", LocalDateTime.ofInstant(list.get(0).getStartTime(), ZoneId.systemDefault()).toString());
                                            dataPoint.put("success", (long) list.stream().filter(record -> record.getHttpStatusCode() >= 200 && record.getHttpStatusCode() < 300).count());
                                            dataPoint.put("errors", (long) list.stream().filter(record -> record.getHttpStatusCode() >= 400).count());
                                            dataPoint.put("total", (long) list.size());
                                            return dataPoint;
                                        }
                                )
                        )).values().stream()
                        .map(map -> (Map<String, Object>) map)
                        .collect(Collectors.toList());
                break;
            case "7d":
                LocalDateTime start7d = now.minusDays(7);
                List<TraceRecord> filtered7d = traces.stream()
                        .filter(tr -> tr.getStartTime() != null && LocalDateTime.ofInstant(tr.getStartTime(), ZoneId.systemDefault()).isAfter(start7d))
                        .peek(tr -> logger.info("Filtered trace startTime: {}, httpStatusCode: {}", tr.getStartTime(), tr.getHttpStatusCode()))
                        .collect(Collectors.toList());
                evolutionData = filtered7d.stream()
                        .collect(Collectors.groupingBy(
                                tr -> LocalDateTime.ofInstant(tr.getStartTime(), ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).toString(),
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> {
                                            if (list.isEmpty()) {
                                                return new HashMap<String, Object>();
                                            }
                                            logger.info("Group size for {}: {}", list.get(0).getStartTime(), list.size());
                                            Map<String, Object> dataPoint = new HashMap<>();
                                            dataPoint.put("time", LocalDateTime.ofInstant(list.get(0).getStartTime(), ZoneId.systemDefault()).toString());
                                            dataPoint.put("success", (long) list.stream().filter(record -> record.getHttpStatusCode() >= 200 && record.getHttpStatusCode() < 300).count());
                                            dataPoint.put("errors", (long) list.stream().filter(record -> record.getHttpStatusCode() >= 400).count());
                                            dataPoint.put("total", (long) list.size());
                                            return dataPoint;
                                        }
                                )
                        )).values().stream()
                        .map(map -> (Map<String, Object>) map)
                        .collect(Collectors.toList());
                break;
            case "30d":
                LocalDateTime start30d = now.minusDays(30);
                List<TraceRecord> filtered30d = traces.stream()
                        .filter(tr -> tr.getStartTime() != null && LocalDateTime.ofInstant(tr.getStartTime(), ZoneId.systemDefault()).isAfter(start30d))
                        .peek(tr -> logger.info("Filtered trace startTime: {}, httpStatusCode: {}", tr.getStartTime(), tr.getHttpStatusCode()))
                        .collect(Collectors.toList());
                evolutionData = filtered30d.stream()
                        .collect(Collectors.groupingBy(
                                tr -> LocalDateTime.ofInstant(tr.getStartTime(), ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).toString(),
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> {
                                            if (list.isEmpty()) {
                                                return new HashMap<String, Object>();
                                            }
                                            Map<String, Object> dataPoint = new HashMap<>();
                                            dataPoint.put("time", LocalDateTime.ofInstant(list.get(0).getStartTime(), ZoneId.systemDefault()).toString());
                                            dataPoint.put("success", (long) list.stream().filter(record -> record.getHttpStatusCode() >= 200 && record.getHttpStatusCode() < 300).count());
                                            dataPoint.put("errors", (long) list.stream().filter(record -> record.getHttpStatusCode() >= 400).count());
                                            dataPoint.put("total", (long) list.size());
                                            return dataPoint;
                                        }
                                )
                        )).values().stream()
                        .map(map -> (Map<String, Object>) map)
                        .collect(Collectors.toList());
                break;
            default:
                return ResponseEntity.badRequest().body(null);
        }

        response.put("data", evolutionData);
        response.put("status", 200);
        return ResponseEntity.ok(response);
    }}