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

    // Effectue un transfert entre deux comptes
    public TransferResponse doTransfer(String fromAccountId, String toAccountId, double amount) {
        String url = cbsBaseUrl + "/transfer";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        TransferRequest requestBody = new TransferRequest(fromAccountId, toAccountId, amount);

        HttpEntity<TransferRequest> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<TransferResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    TransferResponse.class
            );
            return response.getBody(); // Retourne la réponse brute du CBS
        } catch (HttpClientErrorException e) {
            // Tente de mapper la réponse d'erreur du CBS
            return e.getResponseBodyAs(TransferResponse.class);
        } catch (Exception e) {
            return null; // Ou une gestion d'erreur spécifique si nécessaire
        }
    }










    }