package tn.ucar.enicar.middleware.controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.ucar.enicar.middleware.client.CbsClient;
import tn.ucar.enicar.middleware.model.*;
import tn.ucar.enicar.middleware.repository.TraceRecordRepository;
import tn.ucar.enicar.middleware.repository.TransferRecordRepository;

import java.util.List;


@RestController
@RequestMapping("/api")
public class MiddlewareController {

    private static final Logger logger = LoggerFactory.getLogger(MiddlewareController.class);
    private final CbsClient cbsClient;
    private final TransferRecordRepository transferRecordRepository;
    private final TraceRecordRepository traceRecordRepository;

    public MiddlewareController(CbsClient cbsClient, TransferRecordRepository transferRecordRepository ,TraceRecordRepository traceRecordRepository) {
        this.cbsClient = cbsClient;
        this.transferRecordRepository = transferRecordRepository;
        this.traceRecordRepository = traceRecordRepository;
    }

    // Récupère les informations d'un compte
    @GetMapping("/consult-account")
    public ResponseEntity<?> consultAccount(@RequestParam String id) {
        ResponseEntity<?> responseEntity = cbsClient.getAccount(id);
        return responseEntity;
    }

    // Récupère les informations d'un client
    @GetMapping("/consult-customer")
    public ResponseEntity<?> consultCustomer(@RequestParam String id) {
        ResponseEntity<?> responseEntity = cbsClient.getCustomer(id);
        return responseEntity;
    }

    // Récupère l'historique des transactions d'un compte
    @GetMapping("/consult-history")
    public ResponseEntity<?> consultHistory(@RequestParam String id) {
        ResponseEntity<?> responseEntity = cbsClient.getHistory(id);

        return responseEntity;
    }

    // Effectue un transfert d'argent entre deux comptes
    @PostMapping("/do-transfer")
    public ResponseEntity<TransferResponse> doTransfer(@RequestBody TransferRequest request) {

        // Appel au CBS pour effectuer le transfert
        TransferResponse response = cbsClient.doTransfer(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount()
        );

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

        // Retour de la réponse exacte du CBS avec le statut HTTP approprié
        return "success".equalsIgnoreCase(response.getStatus())
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(400).body(response);
    }

// Récupère l'historique des transactions
    @GetMapping("/traces")
    public ResponseEntity<List<TraceRecord>> getTraces() {
        List<TraceRecord> traces = traceRecordRepository.findAll(); // Récupère tous les enregistrements
        return ResponseEntity.ok(traces);
    }
}