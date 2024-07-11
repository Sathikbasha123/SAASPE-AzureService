package saaspe.azure.model;

import java.util.List;

import lombok.Data;

@Data
public class ListOfMultiCloudSpendResponse {
	
	private List<MultiCloudSpendResponse> value;

}
