package tn.ucar.enicar.middleware.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.ucar.enicar.middleware.client.CbsClient;
import tn.ucar.enicar.middleware.model.*;
import tn.ucar.enicar.middleware.repository.ApiLogRepository;
import tn.ucar.enicar.middleware.repository.TransferRecordRepository;
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

    private final TransferRecordRepository transferRecordRepository;

    public MiddlewareController(CbsClient cbsClient, ApiLogRepository apiLogRepository,
                                TransferRequestRepository transferRequestRepository,
                                TransferRecordRepository transferRecordRepository) {
        this.cbsClient = cbsClient;
        this.apiLogRepository = apiLogRepository;
        this.transferRequestRepository = transferRequestRepository;
        this.transferRecordRepository = transferRecordRepository;
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

    // Appel au CBS pour effectuer le transfert
    TransferResponse response = cbsClient.doTransfer(
            request.getFromAccountId(),
            request.getToAccountId(),
            request.getAmount()
    );
    long executionTime = System.currentTimeMillis() - startTime;

    // Enregistrement dans MongoDB uniquement si le transfert réussit
    if ("success".equalsIgnoreCase(response.getStatus())) {
        TransferRecord record = new TransferRecord();
        record.setFromAccountId(request.getFromAccountId());
        record.setToAccountId(request.getToAccountId());
        record.setStatus(response.getStatus());
        record.setFromBalance(response.getFromBalance());
        record.setToBalance(response.getToBalance());
        transferRecordRepository.save(record);
    }

    // Création et enregistrement du log
    ApiLog log = new ApiLog();
    log.setEndpoint("/do-transfer");
    log.setStatus("success".equalsIgnoreCase(response.getStatus()) ? "SUCCESS" : "FAILURE");
    log.setExecutionTime(executionTime);
    log.setTimestamp(LocalDateTime.now().toString());
    apiLogRepository.save(log);

    // Retour de la réponse exacte du CBS avec le statut HTTP approprié
    return "success".equalsIgnoreCase(response.getStatus())
            ? ResponseEntity.ok(response)
            : ResponseEntity.status(400).body(response);
}


}