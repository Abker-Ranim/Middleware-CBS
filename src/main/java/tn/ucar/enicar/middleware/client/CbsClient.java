package tn.ucar.enicar.middleware.client;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final Tracer tracer;

    @Autowired
    public CbsClient(RestTemplate restTemplate, @Value("${cbs.base-url}") String cbsBaseUrl, Tracer tracer) {
        this.restTemplate = restTemplate;
        this.cbsBaseUrl = cbsBaseUrl;
        this.tracer = tracer;
    }

    public ResponseEntity<Object> getAccount(String accountId) {
        Span span = tracer.spanBuilder("CbsClient.getAccount").startSpan();
        try (var scope = span.makeCurrent()) {
            String url = cbsBaseUrl + "/account/{id}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response;
            try {
                response = restTemplate.exchange(
                        url, HttpMethod.GET, entity, Object.class, accountId
                );
            } catch (HttpClientErrorException e) {
                response = ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
            }

            span.setAttribute("http.status_code", response.getStatusCodeValue());
            span.setStatus(response.getStatusCode().is2xxSuccessful() ? StatusCode.OK : StatusCode.ERROR);

            return response;
        } finally {
            span.end();
        }
    }

    public ResponseEntity<Object> getCustomer(String customerId) {
        Span span = tracer.spanBuilder("CbsClient.getCustomer").startSpan();
        try (var scope = span.makeCurrent()) {
            String url = cbsBaseUrl + "/customer/{id}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response;
            try {
                response = restTemplate.exchange(
                        url, HttpMethod.GET, entity, Object.class, customerId
                );
            } catch (HttpClientErrorException e) {
                response = ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
            }

            span.setAttribute("http.status_code", response.getStatusCodeValue());
            span.setStatus(response.getStatusCode().is2xxSuccessful() ? StatusCode.OK : StatusCode.ERROR);

            return response;
        } finally {
            span.end();
        }
    }

    public ResponseEntity<Object> getHistory(String accountId) {
        Span span = tracer.spanBuilder("CbsClient.getHistory").startSpan();
        try (var scope = span.makeCurrent()) {
            String url = cbsBaseUrl + "/history/{id}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response;
            try {
                response = restTemplate.exchange(
                        url, HttpMethod.GET, entity, Object.class, accountId
                );
            } catch (HttpClientErrorException e) {
                response = ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
            }

            span.setAttribute("http.status_code", response.getStatusCodeValue());
            span.setStatus(response.getStatusCode().is2xxSuccessful() ? StatusCode.OK : StatusCode.ERROR);

            return response;
        } finally {
            span.end();
        }
    }

    public TransferResponse doTransfer(String fromAccountId, String toAccountId, double amount) {
        Span span = tracer.spanBuilder("CbsClient.doTransfer").startSpan();
        try (var scope = span.makeCurrent()) {
            String url = cbsBaseUrl + "/transfer";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            TransferRequest requestBody = new TransferRequest(fromAccountId, toAccountId, amount);
            HttpEntity<TransferRequest> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<TransferResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, TransferResponse.class
            );
            span.setAttribute("http.status_code", response.getStatusCodeValue());
            if (response.getStatusCode().is2xxSuccessful()) {
                span.setStatus(StatusCode.OK);
            } else {
                span.setStatus(StatusCode.ERROR);
            }
            return response.getBody();
        } catch (HttpClientErrorException e) {
            span.setAttribute("http.status_code", e.getStatusCode().value());
            span.setStatus(StatusCode.ERROR);
            return e.getResponseBodyAs(TransferResponse.class);
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
            throw new RuntimeException("Error performing transfer: " + e.getMessage(), e);
        } finally {
            span.end();
        }
    }
}