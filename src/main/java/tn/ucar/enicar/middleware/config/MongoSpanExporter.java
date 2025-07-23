package tn.ucar.enicar.middleware.config;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.api.trace.SpanContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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
        for (SpanData span : spans) {
            try {
                // Extraire le contexte de la span
                SpanContext spanContext = span.getSpanContext();
                String traceId = spanContext.getTraceId();
                String spanId = spanContext.getSpanId();

                // Définir manuellement les identifiants dans le MDC pour les logs
                MDC.put("trace_id", traceId);
                MDC.put("span_id", spanId);

                TraceRecord record = new TraceRecord();
                record.setTraceId(traceId);
                record.setSpanId(spanId);
                record.setName(span.getName());
                record.setStartTime(Instant.ofEpochSecond(span.getStartEpochNanos() / 1_000_000_000, (int) (span.getStartEpochNanos() % 1_000_000_000)));
                record.setEndTime(Instant.ofEpochSecond(span.getEndEpochNanos() / 1_000_000_000, (int) (span.getEndEpochNanos() % 1_000_000_000)));
                record.setDurationMs((span.getEndEpochNanos() - span.getStartEpochNanos()) / 1_000_000);

                StatusData status = span.getStatus();
                if (status.getStatusCode() == io.opentelemetry.api.trace.StatusCode.ERROR) {
                    record.setStatus("FAILURE" + (status.getDescription().isEmpty() ? "" : ": " + status.getDescription()));
                } else {
                    record.setStatus("SUCCESS");
                }
                record.setServiceName("middleware-api");

                span.getAttributes().forEach((key, value) -> {
                    switch (key.getKey()) {
                        case "http.method":
                            record.setHttpMethod(value.toString());
                            break;
                        case "http.url":
                            record.setHttpUrl(value.toString());
                            break;
                        case "http.status_code":
                            if (value instanceof Long || value instanceof Integer) {
                                record.setHttpStatusCode(((Number) value).intValue());
                            } else {
                                record.setHttpStatusCode(0);
                            }
                            break;
                    }
                });

                traceRecordRepository.save(record);
                logger.info("Saved trace record with status: {}", record.getStatus()); // Supprimé traceId et spanId
            } catch (Exception e) {
                logger.error("Failed to save trace record: {}", e.getMessage(), e);
            } finally {
                // Nettoyer le MDC après chaque span
                MDC.remove("trace_id");
                MDC.remove("span_id");
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