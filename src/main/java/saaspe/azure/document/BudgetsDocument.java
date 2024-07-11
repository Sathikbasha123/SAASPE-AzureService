package saaspe.azure.document;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Transient;

import org.springframework.data.mongodb.core.mapping.Document;

import com.azure.resourcemanager.consumption.fluent.models.BudgetInner;
import com.azure.resourcemanager.consumption.models.Notification;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@Document(collection = "AzureBudgets")
public class BudgetsDocument {

	@Transient
	public static final String SEQUENCE_NAME = "budgetsDocumentsequence";

	private long id;

	private String clientId;

	private String subscriptionId;

	private String budgetId;

	private String name;

	private String type;

	private String eTag;

	private String startDate;

	private String endDate;

	private String timeGrain;

	private BigDecimal amount;

	private BigDecimal currentSpendAmount;

	private String unit;

	private String category;

	private Object innerModel;

	private BigDecimal notifications;

	private BigDecimal threshold;

	private List<String> contactEmails;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;
	
	private String opID;
	
	private String buID;
	
	private long amigoId;

}
