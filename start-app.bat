@echo off
java -javaagent:C:\Users\ranim\OneDrive\Desktop\OpenTelemetry\opentelemetry-javaagent.jar ^
-Dotel.resource.attributes=service.name=middleware-api ^
-Dotel.traces.exporter=zipkin ^
-Dotel.exporter.zipkin.endpoint=http://localhost:9411/api/v2/spans ^
-Dotel.traces.sampler=parentbased_always_on ^
-Dotel.metrics.exporter=none ^
-Dotel.exporter.zipkin.compression=enabled ^
-Dotel.exporter.zipkin.max.export.batch.size=256 ^
-Dotel.exporter.zipkin.max.queue.size=1024 ^
-Dotel.exporter.zipkin.max.export.batch.delay.millis=1000 ^
-jar target\Middleware-0.0.1-SNAPSHOT.jar
pause