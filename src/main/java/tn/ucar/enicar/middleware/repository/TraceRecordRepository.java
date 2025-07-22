package tn.ucar.enicar.middleware.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.ucar.enicar.middleware.model.TraceRecord;

public interface TraceRecordRepository extends MongoRepository<TraceRecord, String> {
}