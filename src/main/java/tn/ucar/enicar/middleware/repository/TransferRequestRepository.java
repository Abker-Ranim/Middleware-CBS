package tn.ucar.enicar.middleware.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import tn.ucar.enicar.middleware.model.TransferRequest;

public interface TransferRequestRepository extends MongoRepository<TransferRequest, String> {
}
