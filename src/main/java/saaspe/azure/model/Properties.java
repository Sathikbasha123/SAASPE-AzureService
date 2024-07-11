package saaspe.azure.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class Properties {

	private List<String> data;

	private long total;

	private String category;

	private String description;

	private String usageStart;

	private String usageEnd;

	private boolean filterEnabled;

	private boolean groupingEnabled;

	private double amount;

	private Filter filter;

	private Notifications notifications;

	private String impact;
	private String impactedField;
	private String impactedValue;
	private Date lastUpdated;
	private String recommendationTypeID;
	private ShortDescription shortDescription;
	private ExtendedProperties extendedProperties;
	private ResourceMetadata resourceMetadata;
	private String[] suppressionIDS;

}
