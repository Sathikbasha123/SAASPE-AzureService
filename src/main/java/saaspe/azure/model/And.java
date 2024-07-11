package saaspe.azure.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class And {
	
	private Dimensions dimensions;
    private Dimensions tags;

}
