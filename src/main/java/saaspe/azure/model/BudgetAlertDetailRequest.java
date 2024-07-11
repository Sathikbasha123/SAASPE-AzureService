package saaspe.azure.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BudgetAlertDetailRequest {
	private String type;
	private String threshold;

}
