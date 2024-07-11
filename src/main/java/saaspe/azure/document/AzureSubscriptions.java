package saaspe.azure.document;

import java.util.Date;

import javax.persistence.Id;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
@Document(collection = "AzureSubscriptions")
public class AzureSubscriptions {

	@Transient
	public static final String SEQUENCE_NAME = "azureSubscriptionDocumentsequence";
	
	@Id
	public Long id;

	private String subscriptionId;

	private String displayName;

	private String clientId;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;
	
	private String opID;
	
	private String buID;
	
	private long amigoId;

}
