package saaspe.azure.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import saaspe.azure.document.CostManagementUsageByServiceNameDocument;

public interface CostManagementUsageByServiceNameRepository
		extends MongoRepository<CostManagementUsageByServiceNameDocument, Long> {

	@Query("{'subscriptionId' : ?0}")
	CostManagementUsageByServiceNameDocument findBySubscriptionId(String susbcriptionid);

}
