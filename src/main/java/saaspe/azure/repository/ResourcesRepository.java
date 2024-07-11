package saaspe.azure.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import saaspe.azure.document.ResourcesDocument;

public interface ResourcesRepository extends MongoRepository<ResourcesDocument, Long> {

	@Query("{ 'resourceId' : ?0 }")
	ResourcesDocument findByResourceGroupId(String id);

}
