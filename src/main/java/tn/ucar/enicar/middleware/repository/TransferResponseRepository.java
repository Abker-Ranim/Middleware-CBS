package tn.ucar.enicar.middleware.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.ucar.enicar.middleware.model.TransferResponse;

public interface TransferResponseRepository extends MongoRepository<TransferResponse, String> {
}