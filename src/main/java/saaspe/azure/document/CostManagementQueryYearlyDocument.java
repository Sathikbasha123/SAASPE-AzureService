package saaspe.azure.document;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.azure.resourcemanager.costmanagement.fluent.models.QueryResultInner;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import saaspe.azure.model.Properties;
import saaspe.azure.model.QueryColumnsResponse;

@Data
@Document(collection = "AzureCostManagementQueryYearlyDocument")
public class CostManagementQueryYearlyDocument {

    @Transient
    public static final String SEQUENCE_NAME = "costManagementQuerysequence";

    @Id
    private long id;

    private String name;

    private String type;

    private Properties properties;

    private String userId;

    private String subscriptionId;

    private String category;

    private List<QueryColumnsResponse> column;

    private List<List<Object>> rows;

    private QueryResultInner inner;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date createdOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date updatedOn;
    
	private String opID;
	
	private String buID;
	
	private long amigoId;

}
