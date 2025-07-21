package tn.ucar.enicar.middleware.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.ucar.enicar.middleware.model.TransferRecord;

public interface TransferRecordRepository extends MongoRepository<TransferRecord, String> {
}