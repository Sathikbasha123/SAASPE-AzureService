package saaspe.azure.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import saaspe.azure.document.ResourceGroupsActualCostDocument;

public interface ResourceGroupsActualCostRepository extends MongoRepository<ResourceGroupsActualCostDocument, Long> {

	@Query("{'subscriptionId' : ?0}")
	ResourceGroupsActualCostDocument findBySubscriptionId(String subscriptionId);

}
