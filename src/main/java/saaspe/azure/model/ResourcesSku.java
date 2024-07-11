package saaspe.azure.model;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ResourcesSku{

	private String Name;

	private String Tier;

	private String size;

	private String family;

	private String model;

	private Integer capacity;

	

}
