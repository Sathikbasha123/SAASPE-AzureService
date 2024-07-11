package saaspe.azure.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class MultiCloudCountResponse {

	private String vendorName;
	private String logo;
	private List<MultiCloudOverviewResponse> service;
}
