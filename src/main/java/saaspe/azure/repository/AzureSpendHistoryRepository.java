package saaspe.azure.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import saaspe.azure.document.AzureSpendingHistoryDocument;

public interface AzureSpendHistoryRepository extends MongoRepository<AzureSpendingHistoryDocument, String>{

	@Query("{'cloudProvider' : ?0}")
	AzureSpendingHistoryDocument findByCloudProvider(String cloudProvider);

	@Query("{'monthName' : ?0, 'year' : ?1}")
	AzureSpendingHistoryDocument findByMonthAndYear(String monthName, int year);

}