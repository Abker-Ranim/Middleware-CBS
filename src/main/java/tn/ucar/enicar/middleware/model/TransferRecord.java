package tn.ucar.enicar.middleware.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "transfer_records") // Nouvelle collection
@Data
public class TransferRecord {
    @Id
    private String id; // Clé primaire générée par MongoDB
    private String fromAccountId;
    private String toAccountId;
    private String status;
    private double fromBalance;
    private double toBalance;
}