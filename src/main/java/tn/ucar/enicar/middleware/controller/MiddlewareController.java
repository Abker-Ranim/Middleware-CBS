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
    public ResponseEntity<?> consultHistory(@RequestParam String id) { // Changement à ResponseEntity<?>
        logger.debug("Consulting history for account with id: {}", id);
        long startTime = System.currentTimeMillis();
        ResponseEntity<?> responseEntity = cbsClient.getHistory(id); // Utilise la nouvelle méthode
        long executionTime = System.currentTimeMillis() - startTime;

        Object body = responseEntity.getBody();
        ApiLog log = new ApiLog();
        log.setEndpoint("/consult-history");
        log.setStatus(responseEntity.getStatusCode().is2xxSuccessful() ? "SUCCESS" : "FAILURE");
        log.setExecutionTime(executionTime);
        log.setTimestamp(LocalDateTime.now().toString());
        apiLogRepository.save(log);

        return responseEntity; // Retourne la réponse telle quelle
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
            // Remplacer transferRequestRepository par transferResponseRepository
            TransferResponse errorResponse = new TransferResponse();
            errorResponse.setStatus("error");
            errorResponse.setMessage("Ce transfert n'a pas été effectué en raison d'une erreur : " + e.getMessage());
            transferResponseRepository.save(errorResponse); // Sauvegarde la réponse d'erreur
            ApiLog log = new ApiLog();
            log.setEndpoint("/do-transfer");
            log.setStatus("FAILURE");
            log.setExecutionTime(System.currentTimeMillis() - startTime);
            log.setTimestamp(LocalDateTime.now().toString());
            apiLogRepository.save(log);
            return ResponseEntity.status(400).body(errorResponse);
        }
        long executionTime = System.currentTimeMillis() - startTime;

        // Vérifier si la réponse indique une erreur
        if (response != null && "error".equalsIgnoreCase(response.getStatus())) {
            transferResponseRepository.save(response); // Sauvegarde la réponse avec status "error"
            ApiLog log = new ApiLog();
            log.setEndpoint("/do-transfer");
            log.setStatus("FAILURE");
            log.setExecutionTime(executionTime);
            log.setTimestamp(LocalDateTime.now().toString());
            apiLogRepository.save(log);
            return ResponseEntity.status(400).body(response);
        }

        String status = (response != null && "success".equalsIgnoreCase(response.getStatus())) ? "SUCCESS" : "FAILURE";

        transferResponseRepository.save(response); // Sauvegarde la réponse (succès ou échec non exceptionnel)

        ApiLog log = new ApiLog();
        log.setEndpoint("/do-transfer");
        log.setStatus(status);
        log.setExecutionTime(executionTime);
        log.setTimestamp(LocalDateTime.now().toString());
        apiLogRepository.save(log);

        return ResponseEntity.ok(response != null ? response : new TransferResponse());
    }
}