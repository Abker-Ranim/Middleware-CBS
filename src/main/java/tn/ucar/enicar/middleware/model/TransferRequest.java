package tn.ucar.enicar.middleware.model;

import lombok.Data;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "transfer_requests")
@Data
public class TransferRequest {
    @Id
    private String fromAccountId;
    private String toAccountId;
    private double amount;


    public TransferRequest() {}

    public TransferRequest(String fromAccountId, String toAccountId, double amount) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
    }
}