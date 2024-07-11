package saaspe.azure.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import saaspe.azure.document.CostManagementQueryYearlyDocument;

public interface CostManagementQueryYearlyRepository
        extends MongoRepository<CostManagementQueryYearlyDocument, Long> {

    @Query("{'name' : ?0}")
    CostManagementQueryYearlyDocument findByName(String name);

    @Query("{'subscriptionId' : ?0}")
    CostManagementQueryYearlyDocument findBySubscriptionId(String subscriptionId);

}
