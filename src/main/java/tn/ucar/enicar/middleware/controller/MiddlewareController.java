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

import java.util.List;
import java.util.Map;

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
}