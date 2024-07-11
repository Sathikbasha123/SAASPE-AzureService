package saaspe.azure.service;

import java.io.IOException;

import org.json.JSONObject;
import java.text.ParseException;
import saaspe.azure.model.BudgetRequest;
import saaspe.azure.model.CommonResponse;
import saaspe.azure.model.MultiCloudCountResponse;
import saaspe.azure.utils.DataValidationException;

public interface AzureService {

	CommonResponse getAllSubscriptions();

	CommonResponse getAllResourceGroups();

	CommonResponse getAllResources();

	MultiCloudCountResponse getCountBasedOnSubscriptionType();

	CommonResponse getBudgets();

	CommonResponse getActualCostBySerivceName() throws DataValidationException;

	CommonResponse getForecastUsage() throws DataValidationException, InterruptedException;

	CommonResponse getRecommendations();

	CommonResponse getAllResourceGroupsActualCost() throws DataValidationException;

	void initialHit() throws DataValidationException, InterruptedException, IOException;

	JSONObject getActualCostQuarterly() throws DataValidationException;

	JSONObject getActualCostMonthly() throws DataValidationException;

	JSONObject getActualCostYearly() throws DataValidationException;

	CommonResponse createBudget(BudgetRequest budgetRequest);

	void updateRecommendations();
	
	CommonResponse getMonthlySpendingHistory() throws DataValidationException, InterruptedException, ParseException, IOException;
}
