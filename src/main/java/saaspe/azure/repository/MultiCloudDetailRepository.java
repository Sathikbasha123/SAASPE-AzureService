package saaspe.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.azure.entity.MultiCloudDetails;

public interface MultiCloudDetailRepository extends JpaRepository<MultiCloudDetails, String> {

    @Query("select a from MultiCloudDetails a where a.providerName =:provider")
    MultiCloudDetails findByProviderName(String provider);
}
