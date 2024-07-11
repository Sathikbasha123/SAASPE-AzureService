package saaspe.azure.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import saaspe.azure.document.CostManagementQueryQuaterlyDocument;

public interface CostManagementQueryQuaterlyRepository
        extends MongoRepository<CostManagementQueryQuaterlyDocument, Long> {

    @Query("{'name' : ?0}")
    CostManagementQueryQuaterlyDocument findByName(String name);

    @Query("{'subscriptionId' : ?0}")
    CostManagementQueryQuaterlyDocument findBySubscriptionId(String subscriptionId);

}
