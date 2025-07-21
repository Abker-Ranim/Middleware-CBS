package tn.ucar.enicar.middleware.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
public class TransferResponse {

    private String status;
    private double fromBalance;
    private double toBalance;
}
