package tn.ucar.enicar.middleware.service;

import org.springframework.stereotype.Service;
import tn.ucar.enicar.middleware.model.TraceRecord;
import tn.ucar.enicar.middleware.repository.TraceRecordRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiMonitoringService {

    private final TraceRecordRepository traceRecordRepository;

    public ApiMonitoringService(TraceRecordRepository traceRecordRepository) {
        this.traceRecordRepository = traceRecordRepository;
    }

    public List<TraceRecord> getAllTraces() {
        return traceRecordRepository.findAll();
    }

    public List<Map<String, Object>> getEvolutionData(String range) {
        List<TraceRecord> traces = traceRecordRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startTime;
        ChronoUnit unit;
        switch (range) {
            case "24h":
                startTime = now.minusHours(24);
                unit = ChronoUnit.HOURS;
                break;
            case "7d":
                startTime = now.minusDays(7);
                unit = ChronoUnit.DAYS;
                break;
            case "30d":
                startTime = now.minusDays(30);
                unit = ChronoUnit.DAYS;
                break;
            default:
                return Collections.emptyList();
        }

        List<TraceRecord> filtered = traces.stream()
                .filter(tr -> tr.getStartTime() != null &&
                        LocalDateTime.ofInstant(tr.getStartTime(), ZoneId.systemDefault()).isAfter(startTime))
                .collect(Collectors.toList());

        return filtered.stream()
                .collect(Collectors.groupingBy(
                        tr -> LocalDateTime.ofInstant(tr.getStartTime(), ZoneId.systemDefault()).truncatedTo(unit),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    Map<String, Object> dataPoint = new HashMap<>();
                                    LocalDateTime time = LocalDateTime.ofInstant(list.get(0).getStartTime(), ZoneId.systemDefault());
                                    dataPoint.put("time", time.toString());
                                    dataPoint.put("success", list.stream().filter(record -> record.getHttpStatusCode() >= 200 && record.getHttpStatusCode() < 300).count());
                                    dataPoint.put("errors", list.stream().filter(record -> record.getHttpStatusCode() >= 400).count());
                                    dataPoint.put("total", (long) list.size());
                                    return dataPoint;
                                }
                        )
                )).entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getEndpointCalls(String range) {
        List<TraceRecord> traces = traceRecordRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startTime;
        switch (range) {
            case "24h":
                startTime = now.minusHours(24);
                break;
            case "7d":
                startTime = now.minusDays(7);
                break;
            case "30d":
                startTime = now.minusDays(30);
                break;
            default:
                return Collections.emptyList();
        }

        List<TraceRecord> filtered = traces.stream()
                .filter(tr -> tr.getStartTime() != null &&
                        LocalDateTime.ofInstant(tr.getStartTime(), ZoneId.systemDefault()).isAfter(startTime))
                .collect(Collectors.toList());

        return filtered.stream()
                .filter(tr -> tr.getName() != null)
                .collect(Collectors.groupingBy(
                        TraceRecord::getName,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("name", list.get(0).getName());
                                    data.put("total", (long) list.size());
                                    return data;
                                }
                        )
                ))
                .values()
                .stream()
                .collect(Collectors.toList());
    }
}
