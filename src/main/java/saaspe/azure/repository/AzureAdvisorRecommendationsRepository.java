package saaspe.azure.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import saaspe.azure.document.AdvisorDocument;

public interface AzureAdvisorRecommendationsRepository extends MongoRepository<AdvisorDocument, Long> {

	@Query("{'name' : ?0}")
	AdvisorDocument findByName(String name);

}
