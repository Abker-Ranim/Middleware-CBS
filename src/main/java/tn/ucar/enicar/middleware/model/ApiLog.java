package tn.ucar.enicar.middleware.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "api_logs")
@Data
public class ApiLog {
    @Id
    private String id;
    private String traceId;
    private String spanId;
    private String endpoint;
    private String status;
    private long executionTime;
    private String timestamp;
}
