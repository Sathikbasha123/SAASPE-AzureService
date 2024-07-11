package saaspe.azure.model;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import saaspe.azure.document.ResourcesDocument;

@Data
@JsonInclude(Include.NON_NULL)
public class ResourceGroupReourceResponse{
	private String resourceGroupName;

	private List<ResourcesDocument> resources;
}
