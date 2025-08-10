package tn.ucar.enicar.middleware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tn.ucar.enicar.middleware.client.CbsClient;
import tn.ucar.enicar.middleware.model.TransferRecord;
import tn.ucar.enicar.middleware.model.TransferRequest;
import tn.ucar.enicar.middleware.repository.TraceRecordRepository;
import tn.ucar.enicar.middleware.repository.TransferRecordRepository;
import tn.ucar.enicar.middleware.controller.MiddlewareController;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MiddlewareControllerTest {

    @Mock
    private CbsClient cbsClient;

    @Mock
    private TransferRecordRepository transferRecordRepository;

    @Mock
    private TraceRecordRepository traceRecordRepository;

    @InjectMocks
    private MiddlewareController middlewareController;

    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        transferRequest = new TransferRequest();
        transferRequest.setFromAccountId("A123");
        transferRequest.setToAccountId("B456");
        transferRequest.setAmount(100.0);
    }

    @Test
    void consultAccount_ShouldReturnResponseFromCbsClient() {
        ResponseEntity<Object> mockResponse = ResponseEntity.ok("account-data");
        when(cbsClient.getAccount(eq("1001"))).thenReturn(mockResponse);

        ResponseEntity<?> result = middlewareController.consultAccount("1001");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("account-data", result.getBody());
        verify(cbsClient, times(1)).getAccount("1001");
    }


    @Test
    void consultCustomer_ShouldReturnResponseFromCbsClient() {
        ResponseEntity<Object> mockResponse = ResponseEntity.ok("customer-data");
        when(cbsClient.getCustomer(eq("C001"))).thenReturn(mockResponse);

        ResponseEntity<?> result = middlewareController.consultCustomer("C001");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("customer-data", result.getBody());
        verify(cbsClient, times(1)).getCustomer("C001");
    }

    @Test
    void consultHistory_ShouldReturnResponseFromCbsClient() {
        ResponseEntity<Object> mockResponse = ResponseEntity.ok("history-data");
        when(cbsClient.getHistory(eq("1001"))).thenReturn(mockResponse);

        ResponseEntity<?> result = middlewareController.consultHistory("1001");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("history-data", result.getBody());
        verify(cbsClient, times(1)).getHistory("1001");
    }


    @Test
    void doTransfer_Success_ShouldSaveRecord() {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "success");
        body.put("fromBalance", 900.0);
        body.put("toBalance", 1100.0);

        ResponseEntity<Object> mockResponse = ResponseEntity.ok(body);
        when(cbsClient.doTransfer(anyString(), anyString(), anyDouble())).thenReturn(mockResponse);

        ResponseEntity<?> result = middlewareController.doTransfer(transferRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(body, result.getBody());
        verify(transferRecordRepository, times(1)).save(any(TransferRecord.class));
    }

}
