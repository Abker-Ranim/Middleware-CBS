package tn.ucar.enicar.middleware.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.ucar.enicar.middleware.client.CbsClient;
import tn.ucar.enicar.middleware.model.*;
import tn.ucar.enicar.middleware.repository.ApiLogRepository;
import tn.ucar.enicar.middleware.repository.TransferRequestRepository;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class MiddlewareController {

    private static final Logger logger = LoggerFactory.getLogger(MiddlewareController.class);
    private final CbsClient cbsClient;
    private final ApiLogRepository apiLogRepository;
    private final TransferRequestRepository transferRequestRepository;


    public MiddlewareController(CbsClient cbsClient, ApiLogRepository apiLogRepository, TransferRequestRepository transferRequestRepository) {
        this.cbsClient = cbsClient;
        this.apiLogRepository = apiLogRepository;
        this.transferRequestRepository = transferRequestRepository;
    }

    @GetMapping("/consult-account")
    public ResponseEntity<Account> consultAccount(@RequestParam String id) {
        logger.debug("Consulting account with id: {}", id);
        long startTime = System.currentTimeMillis();
        Account account = cbsClient.getAccount(id);
        long executionTime = System.currentTimeMillis() - startTime;

        ApiLog log = new ApiLog();
        log.setEndpoint("/consult-account");
        log.setStatus(account != null ? "SUCCESS" : "FAILURE");
        log.setExecutionTime(executionTime);
        log.setTimestamp(LocalDateTime.now().toString());
        apiLogRepository.save(log);

        return ResponseEntity.ok(account);
    }

    @GetMapping("/consult-customer")
    public ResponseEntity<Customer> consultCustomer(@RequestParam String id) {
        logger.debug("Consulting customer with id: {}", id);
        long startTime = System.currentTimeMillis();
        Customer customer = cbsClient.getCustomer(id);
        long executionTime = System.currentTimeMillis() - startTime;

        ApiLog log = new ApiLog();
        log.setEndpoint("/consult-customer");
        log.setStatus(customer != null ? "SUCCESS" : "FAILURE");
        log.setExecutionTime(executionTime);
        log.setTimestamp(LocalDateTime.now().toString());
        apiLogRepository.save(log);

        return ResponseEntity.ok(customer);
    }

    @GetMapping("/consult-history")
    public ResponseEntity<TransactionHistory> consultHistory(@RequestParam String id) {
        logger.debug("Consulting history for account with id: {}", id);
        long startTime = System.currentTimeMillis();
        TransactionHistory history = cbsClient.getHistory(id);
        long executionTime = System.currentTimeMillis() - startTime;

        ApiLog log = new ApiLog();
        log.setEndpoint("/consult-history");
        log.setStatus(history != null ? "SUCCESS" : "FAILURE");
        log.setExecutionTime(executionTime);
        log.setTimestamp(LocalDateTime.now().toString());
        apiLogRepository.save(log);

        return ResponseEntity.ok(history);
    }

    @PostMapping("/do-transfer")
    public ResponseEntity<TransferResponse> doTransfer(@RequestBody TransferRequest request) {
        logger.debug("Processing transfer: {}", request);
        long startTime = System.currentTimeMillis();


        TransferResponse response;
        try {
            response = cbsClient.doTransfer(
                    request.getFromAccountId(),
                    request.getToAccountId(),
                    request.getAmount()
            );
        } catch (Exception e) {
            logger.error("Error during transfer: {}", e.getMessage());
            transferRequestRepository.save(request);
            ApiLog log = new ApiLog();
            log.setEndpoint("/do-transfer");
            log.setStatus("FAILURE");
            log.setExecutionTime(System.currentTimeMillis() - startTime);
            log.setTimestamp(LocalDateTime.now().toString());
            apiLogRepository.save(log);
            TransferResponse errorResponse = new TransferResponse();
            errorResponse.setStatus("error");
            return ResponseEntity.status(400).body(errorResponse);
        }
        long executionTime = System.currentTimeMillis() - startTime;

        String status = (response != null && "success".equalsIgnoreCase(response.getStatus())) ? "SUCCESS" : "FAILURE";

        transferRequestRepository.save(request);

        ApiLog log = new ApiLog();
        log.setEndpoint("/do-transfer");
        log.setStatus(status);
        log.setExecutionTime(executionTime);
        log.setTimestamp(LocalDateTime.now().toString());
        apiLogRepository.save(log);

        return ResponseEntity.ok(response != null ? response : new TransferResponse());
    }
}