package tn.ucar.enicar.middleware.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tn.ucar.enicar.middleware.model.*;
import org.springframework.http.HttpHeaders;

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

    public TransactionHistory getHistory(String accountId) {
        return restTemplate.getForObject(cbsBaseUrl + "/history/{id}", TransactionHistory.class, accountId);
    }

    public TransferResponse doTransfer(String fromAccountId, String toAccountId, double amount) {
        String url = cbsBaseUrl + "/transfer";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        TransferRequest requestBody = new TransferRequest();
        requestBody.setFromAccountId(fromAccountId);
        requestBody.setToAccountId(toAccountId);
        requestBody.setAmount(amount);

        HttpEntity<TransferRequest> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<TransferResponse> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.POST,
                    requestEntity,
                    TransferResponse.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                TransferResponse errorResponse = new TransferResponse();
                errorResponse.setStatus("error");
                String errorMessage = e.getResponseBodyAsString().replaceAll("[{}\"]", "").trim();
                return errorResponse;
            }
            throw e;
        }
    }
}