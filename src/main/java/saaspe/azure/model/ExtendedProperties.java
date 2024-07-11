package saaspe.azure.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ExtendedProperties {

	private String location;
	private String vmSize;
	private String targetResourceCount;
	private String term;
	private String savingsPercentage;
	private String reservationType;
	private String savingsAmount;
	private String annualSavingsAmount;
	private String savingsCurrency;
	private String scope;
	private String maxCPUP95;
	private String maxTotalNetworkP95;
	private String maxMemoryP95;
	private String deploymentID;
	private String roleName;
	private String currentSku;
	private String targetSku;
	private String recommendationMessage;
	private String recommendationType;
	private String regionID;
	private String subscriptionID;
	private String billingIDToken;
	private String region;
	private String isSQLIaaSExtensionPresent;
	private String isWindowsVM;
	private String vmLocation;
	private String rpTenant;

}
