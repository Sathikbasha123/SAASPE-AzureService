package saaspe.azure.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import saaspe.azure.document.ResourceGroupDocument;

public interface ResourceGroupRepository extends MongoRepository<ResourceGroupDocument, Long> {

	@Query("{ 'resourceGroupId' : ?0 }")
	ResourceGroupDocument findByResourceGroupId(String id);

	@Query("{'subscriptionId' : :#{#subscriptionId}}")
	List<ResourceGroupDocument> getResourceBySubscriptionId(String subscriptionId);

	@Query("{ 'owner_uuid' : ?0 }")
	ResourceGroupDocument findByOwnerId(String owner_uuid);

}
