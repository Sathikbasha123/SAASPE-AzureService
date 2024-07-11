package saaspe.azure.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import saaspe.azure.document.AzureSubscriptions;

public interface AzureSubscriptionsRepository extends MongoRepository<AzureSubscriptions, Integer> {

	@Query("{ 'subscriptionId' : ?0 }")
	AzureSubscriptions existBySubscriptionId(String subscriptionId);

	@Query("{'clientId' : :#{#clientid}}")
	List<AzureSubscriptions> getByClientId(String clientid);

}
