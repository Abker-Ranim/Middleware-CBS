package tn.ucar.enicar.middleware.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "trace_records")
@Data
public class TraceRecord {
    @Id
    private String id;
    private String traceId;
    private String spanId;
    private String name;
    private Instant startTime;
    private Instant endTime;
    private long durationMs;
    private String status;
    private String serviceName;
    private String httpMethod;
    private String httpUrl;
    private int httpStatusCode;
}