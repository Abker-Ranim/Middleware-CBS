package tn.ucar.enicar.middleware.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import tn.ucar.enicar.middleware.repository.TraceRecordRepository;

import java.util.concurrent.TimeUnit;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public OpenTelemetry openTelemetry(
            @Value("${otel.exporter.zipkin.endpoint:http://localhost:9411/api/v2/spans}") String zipkinEndpoint,
            TraceRecordRepository traceRecordRepository) {
        SpanExporter zipkinExporter = ZipkinSpanExporter.builder()
                .setEndpoint(zipkinEndpoint)
                .build();
        SpanExporter mongoExporter = new MongoSpanExporter(traceRecordRepository);

        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(AttributeKey.stringKey("service.name"), "middleware-api")));
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(BatchSpanProcessor.builder(zipkinExporter).build())
                .addSpanProcessor(BatchSpanProcessor.builder(mongoExporter).build())
                .build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();
    }
    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("middleware-api");
    }
}