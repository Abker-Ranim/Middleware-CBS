package tn.ucar.enicar.middleware.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tn.ucar.enicar.middleware.model.*;

import java.util.List;

@Component
public class CbsClient {
    private final RestTemplate restTemplate;
    private final String cbsBaseUrl;

    public CbsClient(RestTemplate restTemplate, @Value("${cbs.base-url}") String cbsBaseUrl) {
        this.restTemplate = restTemplate;
        this.cbsBaseUrl = cbsBaseUrl;
    }


// Récupère les informations d'un compte par son ID
    public ResponseEntity<Object> getAccount(String accountId) {
        String url = cbsBaseUrl + "/account/{id}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Object.class,
                    accountId
            );
            Object body = response.getBody();
            if (body instanceof Account) {
                return ResponseEntity.ok((Account) body);
            }
            return response;
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching account: " + e.getMessage(), e);
        }
    }


    // Récupère les informations d'un client par son ID
    public ResponseEntity<Object> getCustomer(String customerId) {
        String url = cbsBaseUrl + "/customer/{id}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Object.class,
                    customerId
            );
            Object body = response.getBody();
            if (body instanceof Customer) {
                return ResponseEntity.ok((Customer) body);
            }
            return response;
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching customer: " + e.getMessage(), e);
        }
    }


// récupère l'historique des transactions d'un compte
    public ResponseEntity<Object> getHistory(String accountId) {
        String url = cbsBaseUrl + "/history/{id}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Object.class,
                    accountId
            );
            Object body = response.getBody();
            if (body instanceof List) {
                return ResponseEntity.ok(new TransactionHistory((List<Transaction>) body));
            }
            return response;
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching history: " + e.getMessage(), e);
        }
    }

    // Effectue un transfert entre deux comptes
    public TransferResponse doTransfer(String fromAccountId, String toAccountId, double amount) {
        String url = cbsBaseUrl + "/transfer";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        TransferRequest requestBody = new TransferRequest();
        requestBody.setFromAccountId(fromAccountId);
        requestBody.setToAccountId(toAccountId);
        requestBody.setAmount(amount);

        HttpEntity<TransferRequest> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<TransferResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    TransferResponse.class
            );
            TransferResponse body = response.getBody();
            if (body != null && "error".equalsIgnoreCase(body.getStatus())) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Transfer failed: " + (body.getMessage() != null ? body.getMessage() : "Unknown error"));
            }
            return body;
        } catch (HttpClientErrorException e) {
            TransferResponse errorResponse = new TransferResponse();
            errorResponse.setStatus("error");
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                errorResponse.setMessage(e.getResponseBodyAsString().replaceAll("[{}\"]", "").trim());
            } else {
                errorResponse.setMessage("Server error: " + e.getStatusCode());
            }
            return errorResponse;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during transfer: " + e.getMessage(), e);
        }
    }}