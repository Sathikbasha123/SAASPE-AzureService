package saaspe.azure.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import saaspe.azure.document.CostManagementQueryMonthlyDocument;

public interface CostManagementQueryMonthlyRepository
		extends MongoRepository<CostManagementQueryMonthlyDocument, Long> {

	@Query("{'name' : ?0}")
	CostManagementQueryMonthlyDocument findByName(String name);

	@Query("{'subscriptionId' : ?0}")
	CostManagementQueryMonthlyDocument findBySubscriptionId(String subscriptionId);

}
