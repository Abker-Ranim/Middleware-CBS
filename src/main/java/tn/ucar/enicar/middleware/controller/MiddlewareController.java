package tn.ucar.enicar.middleware.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.ucar.enicar.middleware.client.CbsClient;
import tn.ucar.enicar.middleware.model.*;
import tn.ucar.enicar.middleware.repository.ApiLogRepository;
import tn.ucar.enicar.middleware.repository.TransferRequestRepository;
import tn.ucar.enicar.middleware.repository.TransferResponseRepository;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class MiddlewareController {

    private static final Logger logger = LoggerFactory.getLogger(MiddlewareController.class);
    private final CbsClient cbsClient;
    private final ApiLogRepository apiLogRepository;
    private final TransferRequestRepository transferRequestRepository;
    private final TransferResponseRepository transferResponseRepository;


    public MiddlewareController(CbsClient cbsClient, ApiLogRepository apiLogRepository, TransferRequestRepository transferRequestRepository, TransferResponseRepository transferResponseRepository) {
        this.cbsClient = cbsClient;
        this.apiLogRepository = apiLogRepository;
        this.transferRequestRepository = transferRequestRepository;
        this.transferResponseRepository = transferResponseRepository;
    }
// Récupère les informations d'un compte
    @GetMapping("/consult-account")
    public ResponseEntity<?> consultAccount(@RequestParam String id) {
        logger.debug("Consulting account with id: {}", id);
        long startTime = System.currentTimeMillis();
        ResponseEntity<?> responseEntity = cbsClient.getAccount(id);
        long executionTime = System.currentTimeMillis() - startTime;

        Object body = responseEntity.getBody();
        ApiLog log = new ApiLog();
        log.setEndpoint("/consult-account");
        log.setStatus(responseEntity.getStatusCode().is2xxSuccessful() ? "SUCCESS" : "FAILURE");
        log.setExecutionTime(executionTime);
        log.setTimestamp(LocalDateTime.now().toString());
        apiLogRepository.save(log);

        return responseEntity;
    }
// Récupère les informations d'un client
    @GetMapping("/consult-customer")
    public ResponseEntity<?> consultCustomer(@RequestParam String id) {
        logger.debug("Consulting customer with id: {}", id);
        long startTime = System.currentTimeMillis();
        ResponseEntity<?> responseEntity = cbsClient.getCustomer(id);
        long executionTime = System.currentTimeMillis() - startTime;

        Object body = responseEntity.getBody();
        ApiLog log = new ApiLog();
        log.setEndpoint("/consult-customer");
        log.setStatus(responseEntity.getStatusCode().is2xxSuccessful() ? "SUCCESS" : "FAILURE");
        log.setExecutionTime(executionTime);
        log.setTimestamp(LocalDateTime.now().toString());
        apiLogRepository.save(log);

        return responseEntity;
    }
// Récupère l'historique des transactions d'un compte
    @GetMapping("/consult-history")
    public ResponseEntity<?> consultHistory(@RequestParam String id) {
        logger.debug("Consulting history for account with id: {}", id);
        long startTime = System.currentTimeMillis();
        ResponseEntity<?> responseEntity = cbsClient.getHistory(id);
        long executionTime = System.currentTimeMillis() - startTime;

        Object body = responseEntity.getBody();
        ApiLog log = new ApiLog();
        log.setEndpoint("/consult-history");
        log.setStatus(responseEntity.getStatusCode().is2xxSuccessful() ? "SUCCESS" : "FAILURE");
        log.setExecutionTime(executionTime);
        log.setTimestamp(LocalDateTime.now().toString());
        apiLogRepository.save(log);

        return responseEntity;
    }


// Effectue un transfert d'argent entre deux comptes
    @PostMapping("/do-transfer")
    public ResponseEntity<TransferResponse> doTransfer(@RequestBody TransferRequest request) {
        logger.debug("Processing transfer: {}", request);
        long startTime = System.currentTimeMillis();

        TransferResponse response = cbsClient.doTransfer(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount()
        );
        long executionTime = System.currentTimeMillis() - startTime;

        transferResponseRepository.save(response);

        ApiLog log = new ApiLog();
        log.setEndpoint("/do-transfer");
        log.setStatus("error".equalsIgnoreCase(response.getStatus()) ? "FAILURE" : "SUCCESS");
        log.setExecutionTime(executionTime);
        log.setTimestamp(LocalDateTime.now().toString());
        apiLogRepository.save(log);

        return "error".equalsIgnoreCase(response.getStatus())
                ? ResponseEntity.status(400).body(response)
                : ResponseEntity.ok(response);
    }
}