package tn.ucar.enicar.middleware.model;
import lombok.Data;
import org.springframework.data.annotation.Id;

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