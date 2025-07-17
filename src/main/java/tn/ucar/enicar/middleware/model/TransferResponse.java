package tn.ucar.enicar.middleware.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "transfer_responses")
@Data
public class TransferResponse {
    @Id
    private String id;
    private String status;
    private double fromBalance;
    private double toBalance;
    private String message;
}
