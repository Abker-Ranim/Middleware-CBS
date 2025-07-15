package tn.ucar.enicar.middleware.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.ucar.enicar.middleware.model.ApiLog;

public interface ApiLogRepository extends MongoRepository<ApiLog, String> {
}
