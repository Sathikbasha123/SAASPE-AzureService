package saaspe.azure.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import saaspe.azure.document.BudgetsDocument;

public interface BudgetRepository extends MongoRepository<BudgetsDocument, Long> {

	@Query("{ 'name' : ?0 }")
	BudgetsDocument findByBudgetName(String name);
	
	@Query("{'subscriptionId': ?0 }")
	List<BudgetsDocument> findBySubscriptionsId(String subId);

}
