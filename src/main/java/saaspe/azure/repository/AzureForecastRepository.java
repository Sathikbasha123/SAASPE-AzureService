package saaspe.azure.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import saaspe.azure.document.AzureForecastDocument;

public interface AzureForecastRepository extends MongoRepository<AzureForecastDocument, Long> {

	@Query("{'subscriptionId' : ?0}")
	AzureForecastDocument findBySubscriptionId(String susbcriptionid);

	@Query("{'subscriptionId' : :#{#subscriptionId}, 'resourceName' : :#{#resourceName}}")
	AzureForecastDocument findBySubscriptionIdAndResourceName(String susbcriptionid, String resourceName);

}
