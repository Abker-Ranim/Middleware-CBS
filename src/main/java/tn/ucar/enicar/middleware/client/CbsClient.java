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

    public Account getAccount(String accountId) {
        return restTemplate.getForObject(cbsBaseUrl + "/account/{id}", Account.class, accountId);
    }

    public Customer getCustomer(String customerId) {
        return restTemplate.getForObject(cbsBaseUrl + "/customer/{id}", Customer.class, customerId);
    }

    public ResponseEntity<Object> getHistory(String accountId) { // Changement de retour à ResponseEntity<Object>
        String url = cbsBaseUrl + "/history/{id}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Object.class, // Retourne un objet générique pour analyser la réponse
                    accountId
            );
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\": \"No history found for this account\"}");
            }
            Object body = response.getBody();
            if (body instanceof List) {
                return ResponseEntity.ok(new TransactionHistory((List<Transaction>) body));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Unexpected response format from CBS\"}");
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\": \"No history found for this account\"}");
            }
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching history: " + e.getMessage(), e);
        }
    }

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
    }
}