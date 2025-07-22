package tn.ucar.enicar.middleware.config;

import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tn.ucar.enicar.middleware.model.TraceRecord;
import tn.ucar.enicar.middleware.repository.TraceRecordRepository;

import java.time.Instant;
import java.util.Collection;

@Component
public class MongoSpanExporter implements SpanExporter {

    private static final Logger logger = LoggerFactory.getLogger(MongoSpanExporter.class);
    private final TraceRecordRepository traceRecordRepository;

    @Autowired
    public MongoSpanExporter(TraceRecordRepository traceRecordRepository) {
        this.traceRecordRepository = traceRecordRepository;
        logger.info("MongoSpanExporter initialized");
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        logger.info("Exporting {} spans to MongoDB", spans.size());
        for (SpanData span : spans) {
            try {
                TraceRecord record = new TraceRecord();
                record.setTraceId(span.getTraceId());
                record.setSpanId(span.getSpanId());
                record.setParentSpanId(span.getParentSpanId());
                record.setName(span.getName());
                record.setStartTime(Instant.ofEpochSecond(span.getStartEpochNanos() / 1_000_000_000, (int) (span.getStartEpochNanos() % 1_000_000_000)));
                record.setEndTime(Instant.ofEpochSecond(span.getEndEpochNanos() / 1_000_000_000, (int) (span.getEndEpochNanos() % 1_000_000_000)));
                record.setDurationMs((span.getEndEpochNanos() - span.getStartEpochNanos()) / 1_000_000);

                // Définir le statut basé sur StatusData
                StatusData status = span.getStatus();
                if (status.getStatusCode() == StatusCode.ERROR) {
                    record.setStatus("FAILURE" + (status.getDescription().isEmpty() ? "" : ": " + status.getDescription()));
                } else {
                    record.setStatus("SUCCESS");
                }
                record.setServiceName("middleware-api");

                span.getAttributes().forEach((key, value) -> {
                    logger.debug("Attribute: {} = {}", key.getKey(), value);
                    switch (key.getKey()) {
                        case "http.method":
                            record.setHttpMethod(value.toString());
                            break;
                        case "http.url":
                            record.setHttpUrl(value.toString());
                            break;
                        case "http.status_code":
                            // Vérifier et extraire la valeur entière
                            if (value instanceof Long || value instanceof Integer) {
                                record.setHttpStatusCode(((Number) value).intValue());
                            } else {
                                record.setHttpStatusCode(0); // Valeur par défaut si non numérique
                            }
                            break;
                    }
                });

                traceRecordRepository.save(record);
                logger.info("Saved trace record with traceId: {} and status: {}", span.getTraceId(), record.getStatus());
            } catch (Exception e) {
                logger.error("Failed to save trace record: {}", e.getMessage(), e);
            }
        }
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
        logger.info("Flushing MongoSpanExporter");
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        logger.info("Shutting down MongoSpanExporter");
        return CompletableResultCode.ofSuccess();
    }
}