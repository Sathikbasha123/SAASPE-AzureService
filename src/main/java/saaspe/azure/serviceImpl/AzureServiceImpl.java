package saaspe.azure.serviceImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Context;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.AzureResourceManager.Authenticated;
import com.azure.resourcemanager.advisor.AdvisorManager;
import com.azure.resourcemanager.advisor.models.Recommendations;
import com.azure.resourcemanager.advisor.models.ResourceRecommendationBase;
import com.azure.resourcemanager.consumption.ConsumptionManager;
import com.azure.resourcemanager.consumption.models.Budget;
import com.azure.resourcemanager.consumption.models.BudgetTimePeriod;
import com.azure.resourcemanager.consumption.models.CategoryType;
import com.azure.resourcemanager.consumption.models.CultureCode;
import com.azure.resourcemanager.consumption.models.Notification;
import com.azure.resourcemanager.consumption.models.ThresholdType;
import com.azure.resourcemanager.consumption.models.TimeGrainType;
import com.azure.resourcemanager.costmanagement.CostManagementManager;
import com.azure.resourcemanager.costmanagement.models.ExportType;
import com.azure.resourcemanager.costmanagement.models.ForecastDefinition;
import com.azure.resourcemanager.costmanagement.models.ForecastTimeframeType;
import com.azure.resourcemanager.costmanagement.models.ForecastType;
import com.azure.resourcemanager.costmanagement.models.FunctionType;
import com.azure.resourcemanager.costmanagement.models.GranularityType;
import com.azure.resourcemanager.costmanagement.models.OperatorType;
import com.azure.resourcemanager.costmanagement.models.QueryAggregation;
import com.azure.resourcemanager.costmanagement.models.QueryColumn;
import com.azure.resourcemanager.costmanagement.models.QueryColumnType;
import com.azure.resourcemanager.costmanagement.models.QueryComparisonExpression;
import com.azure.resourcemanager.costmanagement.models.QueryDataset;
import com.azure.resourcemanager.costmanagement.models.QueryDefinition;
import com.azure.resourcemanager.costmanagement.models.QueryFilter;
import com.azure.resourcemanager.costmanagement.models.QueryGrouping;
import com.azure.resourcemanager.costmanagement.models.QueryResult;
import com.azure.resourcemanager.costmanagement.models.QueryTimePeriod;
import com.azure.resourcemanager.costmanagement.models.TimeframeType;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.GenericResource;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.models.Subscription;

import saaspe.azure.configuration.SequenceGeneratorService;
import saaspe.azure.constant.Constant;
import saaspe.azure.document.AdvisorDocument;
import saaspe.azure.document.AzureForecastDocument;
import saaspe.azure.document.AzureSpendingHistoryDocument;
import saaspe.azure.document.AzureSubscriptions;
import saaspe.azure.document.BudgetsDocument;
import saaspe.azure.document.CostManagementQueryMonthlyDocument;
import saaspe.azure.document.CostManagementQueryQuaterlyDocument;
import saaspe.azure.document.CostManagementQueryYearlyDocument;
import saaspe.azure.document.CostManagementUsageByServiceNameDocument;
import saaspe.azure.document.ResourceGroupDocument;
import saaspe.azure.document.ResourceGroupsActualCostDocument;
import saaspe.azure.document.ResourcesDocument;
import saaspe.azure.entity.MultiCloudDetails;
import saaspe.azure.model.BudgetAlertDetailRequest;
import saaspe.azure.model.BudgetRequest;
import saaspe.azure.model.CloudProvider;
import saaspe.azure.model.CommonResponse;
import saaspe.azure.model.CostInfo;
import saaspe.azure.model.MonthlyCost;
import saaspe.azure.model.MonthlySpendingResponse;
import saaspe.azure.model.MultiCloudCountResponse;
import saaspe.azure.model.QueryColumnsResponse;
import saaspe.azure.model.ResourcesSku;
import saaspe.azure.model.ShortDescription;
import saaspe.azure.repository.AzureAdvisorRecommendationsRepository;
import saaspe.azure.repository.AzureForecastRepository;
import saaspe.azure.repository.AzureSpendHistoryRepository;
import saaspe.azure.repository.AzureSubscriptionsRepository;
import saaspe.azure.repository.BudgetRepository;
import saaspe.azure.repository.CostManagementQueryMonthlyRepository;
import saaspe.azure.repository.CostManagementQueryQuaterlyRepository;
import saaspe.azure.repository.CostManagementQueryYearlyRepository;
import saaspe.azure.repository.CostManagementUsageByServiceNameRepository;
import saaspe.azure.repository.MultiCloudDetailRepository;
import saaspe.azure.repository.ResourceGroupRepository;
import saaspe.azure.repository.ResourceGroupsActualCostRepository;
import saaspe.azure.repository.ResourcesRepository;
import saaspe.azure.service.AzureService;
import saaspe.azure.utils.CommonUtils;
import saaspe.azure.utils.DataValidationException;

@Service
public class AzureServiceImpl implements AzureService {

	@Autowired
	private ResourceGroupRepository resourceGroupRepository;

	@Autowired
	private ResourcesRepository resourcesRepository;

	@Autowired
	private SequenceGeneratorService sequenceGeneratorService;

	@Autowired
	private BudgetRepository budgetRepository;

	@Autowired
	private CostManagementQueryMonthlyRepository managementQueryMonthlyRepository;

	@Autowired
	private CostManagementQueryQuaterlyRepository managementQueryQuaterlyRepository;

	@Autowired
	private CostManagementQueryYearlyRepository managementQueryYearlyRepository;

	@Autowired
	private AzureSubscriptionsRepository azureSubscriptionsRepository;

	@Autowired
	private AzureForecastRepository azureForecastRepository;

	@Autowired
	private AzureAdvisorRecommendationsRepository advisorRecommendationsRepository;

	@Autowired
	private CostManagementUsageByServiceNameRepository costManagementUsageByServiceNameRepository;

	@Autowired
	private ResourceGroupsActualCostRepository resourceGroupsActualCostRepository;

	@Autowired
	private MultiCloudDetailRepository multiCloudDetailRepository;
	
	@Value("${spring.subscriptionId.url}")
	private String subscriptionID;
	
	@Autowired
	private AzureSpendHistoryRepository azureSpendHistoryRepository;
	
	@Autowired
	AsyncServiceImpl async;
	
	@Value("${saaspe.opid}")
	private String opID;

	@Value("${saaspe.buid}")
	private String buID;
	
	private MultiCloudDetails azureRecord() {
		MultiCloudDetails cloudDetails = multiCloudDetailRepository.findByProviderName("Azure");
		return cloudDetails;
	}

	@Override
	public void initialHit() throws DataValidationException, InterruptedException, IOException {
		if (azureRecord() != null) {
			if (azureRecord().getUpdatedOn() == null) {
				getAllSubscriptions();
				getAllResourceGroups();
				getAllResourceGroupsActualCost();
				getAllResources();
				getBudgets();
				getActualCostQuarterly();
				getActualCostBySerivceName();
				getForecastUsage();
				getRecommendations();
				getMonthlySpendingHistory();
				azureRecord().setUpdatedOn(new Date());
				multiCloudDetailRepository.save(azureRecord());
			}
		}

	}

	@Override
	public CommonResponse getAllSubscriptions() {
		CommonResponse commonResponse = new CommonResponse();
		AzureProfile azureProfile = new AzureProfile(AzureEnvironment.AZURE);
		ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
				.clientId(azureRecord().getClientId()).clientSecret(azureRecord().getClientSecret())
				.tenantId(azureRecord().getTenantId()).build();
		Authenticated authenticated = AzureResourceManager.authenticate(clientSecretCredential, azureProfile)
				.withTenantId(azureRecord().getTenantId());
		PagedIterable<Subscription> listSubscriptions = authenticated.subscriptions().list();
		for (Subscription subscription : listSubscriptions) {
			AzureSubscriptions subscriptions = new AzureSubscriptions();
			AzureSubscriptions document = azureSubscriptionsRepository
					.existBySubscriptionId(subscription.subscriptionId());
			if (document == null) {
				subscriptions.setId(sequenceGeneratorService.generateSequence(AzureSubscriptions.SEQUENCE_NAME));
				subscriptions.setAmigoId(sequenceGeneratorService.generateSequence(Constant.SEQUENCE_NAME));
				subscriptions.setDisplayName(subscription.displayName());
				subscriptions.setSubscriptionId(subscription.subscriptionId());
				subscriptions.setClientId(azureRecord().getClientId());
				subscriptions.setCreatedOn(new Date());
				subscriptions.setOpID(opID);
				subscriptions.setBuID(buID);
				azureSubscriptionsRepository.save(subscriptions);
			} else {
				document.setDisplayName(subscription.displayName());
				document.setSubscriptionId(subscription.subscriptionId());
				document.setClientId(azureRecord().getClientId());
				document.setUpdatedOn(new Date());
				document.setOpID(opID);
				document.setBuID(buID);
				azureSubscriptionsRepository.save(document);
			}
		}
		return commonResponse;
	}

	@Override
	public CommonResponse getAllResourceGroups() {
		CommonResponse commonResponse = new CommonResponse();
		List<AzureSubscriptions> azureSubscriptions = azureSubscriptionsRepository
				.getByClientId(azureRecord().getClientId());
		for (AzureSubscriptions subscriptions : azureSubscriptions) {
			AzureProfile azureProfile = new AzureProfile(azureRecord().getTenantId(), subscriptions.getSubscriptionId(),
					AzureEnvironment.AZURE);
			ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
					.clientId(azureRecord().getClientId()).clientSecret(azureRecord().getClientSecret())
					.tenantId(azureRecord().getTenantId()).build();
			ResourceManager manager = ResourceManager.authenticate(clientSecretCredential, azureProfile)
					.withSubscription(subscriptions.getSubscriptionId());
			PagedIterable<ResourceGroup> iterable = manager.resourceGroups().list();
			for (ResourceGroup group : iterable) {
				ResourceGroupDocument resourceGroupDocument = new ResourceGroupDocument();
				ResourceGroupDocument document = resourceGroupRepository.findByResourceGroupId(group.id());
				if (document == null) {
					resourceGroupDocument
							.setId(sequenceGeneratorService.generateSequence(ResourceGroupDocument.SEQUENCE_NAME));
					resourceGroupDocument
							.setAmigoId(sequenceGeneratorService.generateSequence(Constant.SEQUENCE_NAME));
					resourceGroupDocument.setResourceGroupId(group.id());
					resourceGroupDocument.setLocation(group.regionName());
					resourceGroupDocument.setName(group.name());
					resourceGroupDocument.setProvisioningState(group.provisioningState());
					resourceGroupDocument.setType(group.innerModel().type());
					resourceGroupDocument.setSubscriptionId(subscriptions.getSubscriptionId());
					resourceGroupDocument.setClientId(azureRecord().getClientId());
					resourceGroupDocument.setCreatedOn(new Date());
					resourceGroupDocument.setUpdatedOn(new Date());
					resourceGroupDocument.setOpID(opID);
					resourceGroupDocument.setBuID(buID);
					resourceGroupRepository.save(resourceGroupDocument);
				} else {
					document.setResourceGroupId(group.id());
					document.setLocation(group.regionName());
					document.setName(group.name());
					document.setProvisioningState(group.provisioningState());
					document.setType(group.innerModel().type());
					document.setSubscriptionId(subscriptions.getSubscriptionId());
					document.setClientId(azureRecord().getClientId());
					document.setUpdatedOn(new Date());
					document.setBuID(buID);
					document.setOpID(opID);
					resourceGroupRepository.save(document);
				}
			}
		}
		return commonResponse;
	}

	@Override
	public CommonResponse getAllResourceGroupsActualCost() throws DataValidationException {
		ResourceGroupsActualCostDocument resourceGroupsActualCostDocument = new ResourceGroupsActualCostDocument();
		AzureProfile azureProfile = new AzureProfile(AzureEnvironment.AZURE);
		ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
				.clientId(azureRecord().getClientId()).clientSecret(azureRecord().getClientSecret())
				.tenantId(azureRecord().getTenantId()).build();
		CostManagementManager costManagementManager = CostManagementManager.authenticate(clientSecretCredential,
				azureProfile);
		List<String> values = new ArrayList<>();
		values.add(Constant.AZURE);
		List<AzureSubscriptions> azureSubscriptions = azureSubscriptionsRepository.findAll();
		for (AzureSubscriptions subscription : azureSubscriptions) {
			String url = Constant.SUBSCRIPTIONS;
			url = url.replace(Constant.SUB_ID, subscription.getSubscriptionId());
			Map<String, QueryAggregation> map = new HashMap<>();
			map.put(Constant.TOTAL_COST, new QueryAggregation().withName("Cost").withFunction(FunctionType.SUM));
			map.put(Constant.TOTAL_COST_USD, new QueryAggregation().withName(Constant.COST_USD).withFunction(FunctionType.SUM));
			Response<QueryResult> result = null;
			try {
				result = costManagementManager.queries().usageWithResponse(url,
						new QueryDefinition().withType(ExportType.ACTUAL_COST).withDataset(new QueryDataset()
								.withGranularity(GranularityType.fromString("None")).withAggregation(map)
								.withGrouping(Arrays.asList(new QueryGrouping().withType(QueryColumnType.DIMENSION)
										.withName("ResourceGroupName")))
								.withFilter(new QueryFilter().withDimensions(new QueryComparisonExpression()
										.withName(Constant.PUBLISHER_TYPE).withOperator(OperatorType.IN).withValues(values))))
								.withTimeframe(TimeframeType.BILLING_MONTH_TO_DATE),
						Context.NONE);
			}catch(Exception e) {
				throw new DataValidationException(e.getMessage(), null, HttpStatus.BAD_REQUEST);
			}
			List<QueryColumnsResponse> columnsResponses = new ArrayList<>();
			for (QueryColumn s : result.getValue().columns()) {
				QueryColumnsResponse columnsResponse = new QueryColumnsResponse();
				columnsResponse.setName(s.name());
				columnsResponse.setType(s.type());
				columnsResponses.add(columnsResponse);
			}
			ResourceGroupsActualCostDocument document = resourceGroupsActualCostRepository
					.findBySubscriptionId(subscription.getSubscriptionId());
			if (document == null) {
				resourceGroupsActualCostDocument.setId(
						sequenceGeneratorService.generateSequence(ResourceGroupsActualCostDocument.SEQUENCE_NAME));
				resourceGroupsActualCostDocument.setAmigoId(
						sequenceGeneratorService.generateSequence(Constant.SEQUENCE_NAME));
				resourceGroupsActualCostDocument.setColumn(columnsResponses);
				resourceGroupsActualCostDocument.setRows(result.getValue().rows());
				resourceGroupsActualCostDocument.setName(result.getValue().name());
				resourceGroupsActualCostDocument.setType(result.getValue().type());
				resourceGroupsActualCostDocument.setSubscriptionId(subscription.getSubscriptionId());
				resourceGroupsActualCostDocument.setCreatedOn(new Date());
				resourceGroupsActualCostDocument.setUpdatedOn(new Date());
				resourceGroupsActualCostDocument.setBuID(buID);
				resourceGroupsActualCostDocument.setOpID(opID);
				resourceGroupsActualCostRepository.save(resourceGroupsActualCostDocument);
			} else {
				document.setColumn(columnsResponses);
				document.setRows(result.getValue().rows());
				document.setName(result.getValue().name());
				document.setType(result.getValue().type());
				document.setUpdatedOn(new Date());
				document.setBuID(buID);
                document.setOpID(opID);
				resourceGroupsActualCostRepository.save(document);
			}
		}
		return null;
	}

	@Override
	public CommonResponse getAllResources() {
		CommonResponse commonResponse = new CommonResponse();
		List<AzureSubscriptions> azureSubscriptions = azureSubscriptionsRepository
				.getByClientId(azureRecord().getClientId());
		for (AzureSubscriptions subscriptions : azureSubscriptions) {
			AzureProfile azureProfile = new AzureProfile(AzureEnvironment.AZURE);
			ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
					.clientId(azureRecord().getClientId()).clientSecret(azureRecord().getClientSecret())
					.tenantId(azureRecord().getTenantId()).build();
			ResourceManager manager = ResourceManager.authenticate(clientSecretCredential, azureProfile)
					.withSubscription(subscriptions.getSubscriptionId());
			List<ResourceGroupDocument> documents = resourceGroupRepository
					.getResourceBySubscriptionId(subscriptions.getSubscriptionId());
			for (ResourceGroupDocument document : documents) {
				LocalDate localDate = CommonUtils.dateToLocalDate(document.getUpdatedOn());
				if (localDate.equals(LocalDate.now())) {
					PagedIterable<GenericResource> resource = manager.genericResources()
							.listByResourceGroup(document.getName());
					for (GenericResource genericResource : resource) {
						ResourcesDocument resourcesDoc = resourcesRepository
								.findByResourceGroupId(genericResource.id());
						if (resourcesDoc == null) {
							ResourcesDocument resourcesDocument = new ResourcesDocument();
							resourcesDocument
									.setId(sequenceGeneratorService.generateSequence(ResourcesDocument.SEQUENCE_NAME));
							resourcesDocument
									.setAmigoId(sequenceGeneratorService.generateSequence(Constant.SEQUENCE_NAME));
							resourcesDocument.setLocation(genericResource.regionName());
							resourcesDocument.setResourceId(genericResource.id());
							resourcesDocument.setName(genericResource.name());
							resourcesDocument.setResourceGroupId(document.getName());
							resourcesDocument.setType(genericResource.type());
							resourcesDocument.setManagedBy(genericResource.managedBy());
							resourcesDocument.setSubscriptionId(subscriptions.getSubscriptionId());
							resourcesDocument.setTags(genericResource.tags());
							resourcesDocument.setBuID(buID);
							resourcesDocument.setOpID(opID);
							if (!StringUtils.isEmpty(genericResource.sku())) {
								ResourcesSku resourcesSku = new ResourcesSku();
								resourcesSku.setTier(genericResource.sku().tier());
								resourcesSku.setName(genericResource.sku().name());
								resourcesSku.setCapacity(genericResource.sku().capacity());
								resourcesSku.setFamily(genericResource.sku().family());
								resourcesSku.setSize(genericResource.sku().size());
								resourcesDocument.setSku(resourcesSku);
							}
							resourcesDocument.setCreatedOn(new Date());
							resourcesDocument.setUpdatedOn(new Date());
							resourcesDocument.setClientId(azureRecord().getClientId());
							resourcesRepository.save(resourcesDocument);
						} else {
							resourcesDoc.setLocation(genericResource.regionName());
							resourcesDoc.setResourceId(genericResource.id());
							resourcesDoc.setName(genericResource.name());
							resourcesDoc.setResourceGroupId(document.getName());
							resourcesDoc.setType(genericResource.type());
							resourcesDoc.setManagedBy(genericResource.managedBy());
							resourcesDoc.setSubscriptionId(subscriptions.getSubscriptionId());
							resourcesDoc.setTags(genericResource.tags());
							resourcesDoc.setBuID(buID);
							resourcesDoc.setOpID(opID);
							if (!StringUtils.isEmpty(genericResource.sku())) {
								ResourcesSku resourcesSku = new ResourcesSku();
								resourcesSku.setTier(genericResource.sku().tier());
								resourcesSku.setName(genericResource.sku().name());
								resourcesSku.setCapacity(genericResource.sku().capacity());
								resourcesSku.setFamily(genericResource.sku().family());
								resourcesSku.setSize(genericResource.sku().size());
								resourcesDoc.setSku(resourcesSku);
							}
							resourcesDoc.setUpdatedOn(new Date());
							resourcesDoc.setClientId(azureRecord().getClientId());
							resourcesRepository.save(resourcesDoc);
						}
					}
				}

			}
		}
		return commonResponse;
	}

	@Override
	public MultiCloudCountResponse getCountBasedOnSubscriptionType() {
		return null;
	}
	// AzureProfile azureProfile = new AzureProfile(AzureEnvironment.AZURE);
	// ClientSecretCredential clientSecretCredential = new
	// ClientSecretCredentialBuilder().clientId(azureRecord().getClientId())
	// .clientSecret(azureRecord().getClientSecret()).tenantId(azureRecord().getTenantId()).build();
	// List<AzureSubscriptions> azureSubscriptions =
	// azureSubscriptionsRepository.findAll();
	// for (AzureSubscriptions subscription : azureSubscriptions) {
	// ResourceManager manager =
	// ResourceManager.authenticate(clientSecretCredential, azureProfile)
	// .withSubscription(subscription.getSubscriptionId());
	// PagedIterable<ResourceGroup> iterable = manager.resourceGroups().list();
	// List<String> str = new ArrayList<>();
	// List<MultiCloudOverviewResponse> resp1 = new ArrayList<>();
	// MultiCloudCountResponse cloudResponse = new MultiCloudCountResponse();
	// for (ResourceGroup document : iterable) {
	// PagedIterable<GenericResource> resource =
	// manager.genericResources().listByResourceGroup(document.name());
	// for (GenericResource genericResource : resource) {
	// if (genericResource != null) {
	// str.add(genericResource.type().substring(genericResource.type().indexOf("/")
	// + 1));
	// }
	// }
	// }
	// Set<String> st = new HashSet<>(str);
	// for (String s : st) {
	// MultiCloudOverviewResponse response2 = new MultiCloudOverviewResponse();
	// response2.setServiceName(s.substring(0, 1).toUpperCase() +
	// s.substring(1).toLowerCase());
	// response2.setCount(Collections.frequency(str, s));
	// resp1.add(response2);
	// }
	// cloudResponse.setVendorName("Azure");
	// cloudResponse.setLogo("https://saaspemedia.blob.core.windows.net/images/logos/svg/azure-ad.svg");
	// cloudResponse.setService(resp1);
	// return cloudResponse;
	// }
	//

	@Override
	public CommonResponse getBudgets() {
		CommonResponse commonResponse = new CommonResponse();
		AzureProfile azureProfile = new AzureProfile(AzureEnvironment.AZURE);
		ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
				.clientId(azureRecord().getClientId()).clientSecret(azureRecord().getClientSecret())
				.tenantId(azureRecord().getTenantId()).build();
		ConsumptionManager manager = ConsumptionManager.authenticate(clientSecretCredential, azureProfile);
		List<AzureSubscriptions> azureSubscriptions = azureSubscriptionsRepository.findAll();
		for (AzureSubscriptions subscription : azureSubscriptions) {
			String scope = Constant.SUBSCRIPTIONS;
			scope = scope.replace(Constant.SUB_ID, subscription.getSubscriptionId());
			PagedIterable<Budget> budgets = manager.budgets().list(scope, Context.NONE);
			async.deleteBudgets(budgets,subscription.getSubscriptionId());
			for (Budget budget : budgets) {
				BudgetsDocument document = budgetRepository.findByBudgetName(budget.name());
				if (document == null) {
					BudgetsDocument budgetsDocument = new BudgetsDocument();
					budgetsDocument.setId(sequenceGeneratorService.generateSequence(BudgetsDocument.SEQUENCE_NAME));
					budgetsDocument.setAmigoId(sequenceGeneratorService.generateSequence(Constant.SEQUENCE_NAME));
					budgetsDocument.setBudgetId(budget.id());
					budgetsDocument.setSubscriptionId(subscription.getSubscriptionId());
					budgetsDocument.setName(budget.name());
					budgetsDocument.setETag(budget.etag());
					budgetsDocument.setType(budget.type());
					budgetsDocument.setStartDate(budget.timePeriod().startDate().toString());
					budgetsDocument.setEndDate(budget.timePeriod().endDate().toString());
					budgetsDocument.setTimeGrain(budget.timeGrain().toString());
					budgetsDocument.setAmount(budget.amount());
					budgetsDocument.setCurrentSpendAmount(budget.currentSpend().amount());
					budgetsDocument.setUnit(budget.currentSpend().unit());
					budgetsDocument.setCategory(budget.category().toString());
					budgetsDocument.setOpID(opID);
					budgetsDocument.setBuID(buID);
					Map<String, Notification> notification = budget.innerModel().notifications();
					for (Entry<String, Notification> entry : notification.entrySet()) {
						Notification notify = notification.get(entry.getKey());
						budgetsDocument.setThreshold(notify.threshold());
						budgetsDocument.setContactEmails(notify.contactEmails());
					}
					budgetsDocument.setCreatedOn(new Date());
					budgetsDocument.setUpdatedOn(new Date());
					budgetRepository.save(budgetsDocument);
				} else {
					document.setBudgetId(budget.id());
					document.setSubscriptionId(subscription.getSubscriptionId());
					document.setName(budget.name());
					document.setETag(budget.etag());
					document.setType(budget.type());
					document.setStartDate(budget.timePeriod().startDate().toString());
					document.setEndDate(budget.timePeriod().endDate().toString());
					document.setTimeGrain(budget.timeGrain().toString());
					document.setAmount(budget.amount());
					document.setCurrentSpendAmount(budget.currentSpend().amount());
					document.setUnit(budget.currentSpend().unit());
					document.setCategory(budget.category().toString());
					Map<String, Notification> notification = budget.innerModel().notifications();
					for (Entry<String, Notification> entry : notification.entrySet()) {
						Notification notify = notification.get(entry.getKey());
						document.setThreshold(notify.threshold());
						document.setContactEmails(notify.contactEmails());
					}
					document.setBuID(buID);
					document.setOpID(opID);
					document.setUpdatedOn(new Date());
					budgetRepository.save(document);
				}
			}
		}

		return commonResponse;
	}

	@Override
	public JSONObject getActualCostQuarterly() throws DataValidationException {
		CommonUtils.getCurrentMonthFirstDate();
		CommonUtils.getCurrentMonthLastDate();
		CommonUtils.getFirstDateOfQuarter();
		CommonUtils.getLastDateOfQuarter();
		CommonUtils.getFistDateOfYear();
		CommonUtils.getLastDateOfYear();

		JSONObject jsonObject = new JSONObject();

		CostManagementQueryQuaterlyDocument costManagementQueryDocument = new CostManagementQueryQuaterlyDocument();
		AzureProfile azureProfile = new AzureProfile(AzureEnvironment.AZURE);
		ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
				.clientId(azureRecord().getClientId()).clientSecret(azureRecord().getClientSecret())
				.tenantId(azureRecord().getTenantId()).build();
		CostManagementManager costManagementManager = CostManagementManager.authenticate(clientSecretCredential,
				azureProfile);
		String startDate = CommonUtils.getFirstDateOfQuarter().toString();
		String endDate = CommonUtils.getLastDateOfQuarter().toString();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constant.DATE_FORMAT);
		LocalDate fromLocalDate = LocalDate.parse(startDate, formatter);
		LocalDate toLocalDate = LocalDate.parse(endDate, formatter);
		OffsetDateTime from = OffsetDateTime.of(fromLocalDate, LocalTime.NOON, ZoneOffset.UTC);
		OffsetDateTime to = OffsetDateTime.of(toLocalDate, LocalTime.NOON, ZoneOffset.UTC);
		List<AzureSubscriptions> azureSubscriptions = azureSubscriptionsRepository.findAll();
		for (AzureSubscriptions subscription : azureSubscriptions) {
			String url = Constant.SUBSCRIPTIONS;
			url = url.replace(Constant.SUB_ID, subscription.getSubscriptionId());
			Map<String, QueryAggregation> map = new HashMap<>();
			map.put(Constant.TOTAL_COST, new QueryAggregation().withName("Cost").withFunction(FunctionType.SUM));
			map.put(Constant.TOTAL_COST_USD, new QueryAggregation().withName(Constant.COST_USD).withFunction(FunctionType.SUM));
			Response<QueryResult> result = null;
			try {
				result = costManagementManager.queries().usageWithResponse(url,
						new QueryDefinition().withType(ExportType.ACTUAL_COST).withDataset(
								new QueryDataset().withGranularity(GranularityType.fromString("None")).withAggregation(map)
						/*
						 * .withGrouping(Arrays.asList( new
						 * QueryGrouping().withType(QueryColumnType.DIMENSION).withName("BillingPeriod")
						 * ))
						 */).withTimeframe(TimeframeType.CUSTOM)
								.withTimePeriod(new QueryTimePeriod().withFrom(from).withTo(to)),
						Context.NONE);
			}catch(Exception e) {
				throw new DataValidationException(e.getMessage(), null, HttpStatus.BAD_REQUEST);
			}
			List<QueryColumnsResponse> columnsResponses = new ArrayList<>();
			for (QueryColumn s : result.getValue().columns()) {
				QueryColumnsResponse columnsResponse = new QueryColumnsResponse();
				columnsResponse.setName(s.name());
				columnsResponse.setType(s.type());
				columnsResponses.add(columnsResponse);
			}
			CostManagementQueryQuaterlyDocument document = managementQueryQuaterlyRepository
					.findBySubscriptionId(subscription.getSubscriptionId());
			if (document == null) {
				costManagementQueryDocument.setId(
						sequenceGeneratorService.generateSequence(CostManagementQueryQuaterlyDocument.SEQUENCE_NAME));
				costManagementQueryDocument.setAmigoId(
						sequenceGeneratorService.generateSequence(Constant.SEQUENCE_NAME));
				costManagementQueryDocument.setColumn(columnsResponses);
				costManagementQueryDocument.setRows(result.getValue().rows());
				costManagementQueryDocument.setName(result.getValue().name());
				costManagementQueryDocument.setType(result.getValue().type());
				costManagementQueryDocument.setSubscriptionId(subscription.getSubscriptionId());
				costManagementQueryDocument.setCreatedOn(new Date());
				costManagementQueryDocument.setUpdatedOn(new Date());
				costManagementQueryDocument.setBuID(buID);
				costManagementQueryDocument.setOpID(opID);
				jsonObject.put("Qaurter", costManagementQueryDocument);
				managementQueryQuaterlyRepository.save(costManagementQueryDocument);
			} else {
				document.setColumn(columnsResponses);
				document.setRows(result.getValue().rows());
				document.setName(result.getValue().name());
				document.setType(result.getValue().type());
				document.setUpdatedOn(new Date());
				document.setBuID(buID);
				document.setOpID(opID);
				managementQueryQuaterlyRepository.save(document);
			}
		}
		return jsonObject;
	}

	@Override
	public CommonResponse getActualCostBySerivceName() throws DataValidationException {
		CommonResponse commonResponse = new CommonResponse();
		CostManagementUsageByServiceNameDocument byServiceNameDocument = new CostManagementUsageByServiceNameDocument();
		AzureProfile azureProfile = new AzureProfile(AzureEnvironment.AZURE);
		ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
				.clientId(azureRecord().getClientId()).clientSecret(azureRecord().getClientSecret())
				.tenantId(azureRecord().getTenantId()).build();
		CostManagementManager costManagementManager = CostManagementManager.authenticate(clientSecretCredential,
				azureProfile);
		List<String> values = new ArrayList<>();
		values.add(Constant.AZURE);
		List<AzureSubscriptions> azureSubscriptions = azureSubscriptionsRepository.findAll();
		for (AzureSubscriptions subscription : azureSubscriptions) {
			String url = Constant.SUBSCRIPTIONS;
			url = url.replace(Constant.SUB_ID, subscription.getSubscriptionId());
			Map<String, QueryAggregation> map = new HashMap<>();
			map.put(Constant.TOTAL_COST, new QueryAggregation().withName("Cost").withFunction(FunctionType.SUM));
			map.put(Constant.TOTAL_COST_USD, new QueryAggregation().withName(Constant.COST_USD).withFunction(FunctionType.SUM));
			Response<QueryResult> result = null;
			try {
				result = costManagementManager.queries().usageWithResponse(url,
						new QueryDefinition().withType(ExportType.ACTUAL_COST).withDataset(new QueryDataset()
								.withGranularity(GranularityType.fromString("None")).withAggregation(map)
								.withGrouping(Arrays.asList(new QueryGrouping().withType(QueryColumnType.DIMENSION)
										.withName("ServiceName")))
								.withFilter(new QueryFilter().withDimensions(new QueryComparisonExpression()
										.withName(Constant.PUBLISHER_TYPE).withOperator(OperatorType.IN).withValues(values))))
								.withTimeframe(TimeframeType.fromString("None")),
						null);
			} catch (Exception e) {
				throw new DataValidationException(e.getMessage(), null, HttpStatus.BAD_REQUEST); 
			}
			List<QueryColumnsResponse> columnsResponses = new ArrayList<>();
			for (QueryColumn s : result.getValue().columns()) {
				QueryColumnsResponse columnsResponse = new QueryColumnsResponse();
				columnsResponse.setName(s.name());
				columnsResponse.setType(s.type());
				columnsResponses.add(columnsResponse);
			}
			CostManagementUsageByServiceNameDocument document = costManagementUsageByServiceNameRepository
					.findBySubscriptionId(subscription.getSubscriptionId());
			if (document == null) {
				byServiceNameDocument.setId(sequenceGeneratorService
						.generateSequence(CostManagementUsageByServiceNameDocument.SEQUENCE_NAME));
				byServiceNameDocument.setAmigoId(sequenceGeneratorService
						.generateSequence(Constant.SEQUENCE_NAME));
				byServiceNameDocument.setColumn(columnsResponses);
				byServiceNameDocument.setRows(result.getValue().rows());
				byServiceNameDocument.setName(result.getValue().name());
				byServiceNameDocument.setType(result.getValue().type());
				byServiceNameDocument.setSubscriptionId(subscription.getSubscriptionId());
				byServiceNameDocument.setCreatedOn(new Date());
				byServiceNameDocument.setUpdatedOn(new Date());
				byServiceNameDocument.setOpID(opID);
				byServiceNameDocument.setBuID(buID);
				costManagementUsageByServiceNameRepository.save(byServiceNameDocument);
			} else {
				document.setColumn(columnsResponses);
				document.setRows(result.getValue().rows());
				document.setName(result.getValue().name());
				document.setType(result.getValue().type());
				document.setUpdatedOn(new Date());
				document.setBuID(buID);
				document.setOpID(opID);
				costManagementUsageByServiceNameRepository.save(document);
			}
		}

		return commonResponse;
	}

	@Override
	public CommonResponse getForecastUsage() throws DataValidationException, InterruptedException {
		CommonResponse commonResponse = new CommonResponse();
		List<AzureForecastDocument> list = new ArrayList<>();
		AzureProfile azureProfile = new AzureProfile(AzureEnvironment.AZURE);
		ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
				.clientId(azureRecord().getClientId()).clientSecret(azureRecord().getClientSecret())
				.tenantId(azureRecord().getTenantId()).build();
		CostManagementManager costManagementManager = CostManagementManager.authenticate(clientSecretCredential,
				azureProfile);
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = startDate.plusMonths(12);
		OffsetDateTime from = OffsetDateTime.of(startDate, LocalTime.NOON, ZoneOffset.UTC);
		OffsetDateTime to = OffsetDateTime.of(endDate, LocalTime.NOON, ZoneOffset.UTC);
		List<String> values = new ArrayList<>();
		values.add(Constant.AZURE);
		List<AzureSubscriptions> azureSubscriptions = azureSubscriptionsRepository.findAll();
		for (AzureSubscriptions subscription : azureSubscriptions) {
			List<ResourceGroupDocument> resourceGroupDocuments = resourceGroupRepository
					.getResourceBySubscriptionId(subscription.getSubscriptionId());
			for (ResourceGroupDocument resourceGroupDocument : resourceGroupDocuments) {
				String url = "subscriptions/{{subId}}/resourceGroups/{{resourceName}}";
				url = url.replace(Constant.SUB_ID, subscription.getSubscriptionId());
				url = url.replace("{{resourceName}}", resourceGroupDocument.getName());
				Map<String, QueryAggregation> map = new HashMap<>();
				map.put(Constant.TOTAL_COST, new QueryAggregation().withName("Cost").withFunction(FunctionType.SUM));
				Response<QueryResult> result = null;
				int code = 0;
				try {
					result = costManagementManager.forecasts().usageWithResponse(url, new ForecastDefinition()
							.withType(ForecastType.ACTUAL_COST)
							.withDataset(new QueryDataset().withGranularity(GranularityType.DAILY).withAggregation(map)
									.withFilter(new QueryFilter()
											.withDimensions(new QueryComparisonExpression().withName(Constant.PUBLISHER_TYPE)
													.withOperator(OperatorType.IN).withValues(values))))
							.withTimeframe(ForecastTimeframeType.CUSTOM)
							.withTimePeriod(new QueryTimePeriod().withFrom(from).withTo(to))
							.withIncludeActualCost(false).withIncludeFreshPartialCost(false), null, Context.NONE);
				} catch (ManagementException e) {
					code = e.getResponse().getStatusCode();
				}
				if(code == 429) {
					Thread.sleep(5000);
				}
				if (code == 0) {
					AzureForecastDocument azureForecastDocument = new AzureForecastDocument();
					List<QueryColumnsResponse> columnsResponses = new ArrayList<>();
					for (QueryColumn s : result.getValue().columns()) {
						QueryColumnsResponse columnsResponse = new QueryColumnsResponse();
						columnsResponse.setName(s.name());
						columnsResponse.setType(s.type());
						columnsResponses.add(columnsResponse);
					}
					AzureForecastDocument document = azureForecastRepository.findBySubscriptionIdAndResourceName(
							subscription.getSubscriptionId(), resourceGroupDocument.getName());
					if (document == null) {
						azureForecastDocument
								.setId(sequenceGeneratorService.generateSequence(AzureForecastDocument.SEQUENCE_NAME));
						azureForecastDocument
								.setAmigoId(sequenceGeneratorService.generateSequence(Constant.SEQUENCE_NAME));
						azureForecastDocument.setSubscriptionId(subscription.getSubscriptionId());
						azureForecastDocument.setResourceName(resourceGroupDocument.getName());
						azureForecastDocument.setColumn(columnsResponses);
						azureForecastDocument.setRows(result.getValue().rows());
						azureForecastDocument.setName(result.getValue().name());
						azureForecastDocument.setType(result.getValue().type());
						azureForecastDocument.setSubscriptionId(subscription.getSubscriptionId());
						azureForecastDocument.setCreatedOn(new Date());
						azureForecastDocument.setUpdatedOn(new Date());
						azureForecastDocument.setBuID(buID);
						azureForecastDocument.setOpID(opID);
						azureForecastRepository.save(azureForecastDocument);
					} else {
						document.setSubscriptionId(subscription.getSubscriptionId());
						document.setResourceName(resourceGroupDocument.getName());
						document.setColumn(columnsResponses);
						document.setRows(result.getValue().rows());
						document.setName(result.getValue().name());
						document.setType(result.getValue().type());
						document.setUpdatedOn(new Date());
						document.setOpID(opID);
						document.setBuID(buID);
						azureForecastRepository.save(document);
					}
				}
			}
		}

		return commonResponse;
	}

	@Override
	public CommonResponse getRecommendations(){
		CommonResponse commonResponse = new CommonResponse();
		List<AzureSubscriptions> azureSubscriptions = azureSubscriptionsRepository.findAll();
		for (AzureSubscriptions subscription : azureSubscriptions) {
			AzureProfile azureProfile = new AzureProfile(azureRecord().getTenantId(), subscription.getSubscriptionId(),
					AzureEnvironment.AZURE);
			ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
					.clientId(azureRecord().getClientId()).clientSecret(azureRecord().getClientSecret())
					.tenantId(azureRecord().getTenantId()).build();
			AdvisorManager advisorManager = AdvisorManager.authenticate(clientSecretCredential, azureProfile);
			Recommendations s = advisorManager.recommendations();
			PagedIterable<ResourceRecommendationBase> iterable = s.list();
			for (ResourceRecommendationBase recommendationBase : iterable) {
				AdvisorDocument document = advisorRecommendationsRepository.findByName(recommendationBase.name());
				if (document == null) {
					AdvisorDocument advisorDocument = new AdvisorDocument();
					advisorDocument.setSubscriptionId(subscription.getSubscriptionId());
					advisorDocument.setId(recommendationBase.id());
					advisorDocument.setName(recommendationBase.name());
					advisorDocument.setType(recommendationBase.type());
					advisorDocument.setCategory(recommendationBase.category().toString());
					advisorDocument.setImpact(recommendationBase.impact().toString());
					advisorDocument.setImpactedField(recommendationBase.impactedField());
					advisorDocument.setImpactedValue(recommendationBase.impactedValue());
					advisorDocument.setLastUpdated(recommendationBase.lastUpdated().toString());
					advisorDocument.setRecommendationTypeID(recommendationBase.recommendationTypeId());
					ShortDescription shortDescription = new ShortDescription();
					shortDescription.setProblem(recommendationBase.shortDescription().problem());
					shortDescription.setSolution(recommendationBase.shortDescription().solution());
					advisorDocument.setShortDescription(shortDescription);
					if (recommendationBase.extendedProperties() != null) {
						advisorDocument.setExtendedProperties(recommendationBase.extendedProperties());
					}
					advisorDocument.setResourceId(recommendationBase.resourceMetadata().resourceId());
					advisorDocument.setCreatedOn(new Date());
					advisorDocument.setUpdatedOn(new Date());
					advisorDocument.setOpID(opID);
					advisorDocument.setBuID(buID);
					advisorRecommendationsRepository.save(advisorDocument);
				} else {
					document.setSubscriptionId(subscription.getSubscriptionId());
					document.setId(recommendationBase.id());
					document.setName(recommendationBase.name());
					document.setType(recommendationBase.type());
					document.setCategory(recommendationBase.category().toString());
					document.setImpact(recommendationBase.impact().toString());
					document.setImpactedField(recommendationBase.impactedField());
					document.setImpactedValue(recommendationBase.impactedValue());
					document.setLastUpdated(recommendationBase.lastUpdated().toString());
					document.setRecommendationTypeID(recommendationBase.recommendationTypeId());
					ShortDescription shortDescription = new ShortDescription();
					shortDescription.setProblem(recommendationBase.shortDescription().problem());
					shortDescription.setSolution(recommendationBase.shortDescription().solution());
					document.setShortDescription(shortDescription);
					if (recommendationBase.extendedProperties() != null) {
						document.setExtendedProperties(recommendationBase.extendedProperties());
					}
					document.setResourceId(recommendationBase.resourceMetadata().resourceId());
					document.setUpdatedOn(new Date());
					document.setOpID(opID);
					document.setBuID(buID);
					advisorRecommendationsRepository.save(document);
				}
			}
		}
		return commonResponse;
	}

	@Override
	public JSONObject getActualCostMonthly() throws DataValidationException {
		CommonUtils.getCurrentMonthFirstDate();
		CommonUtils.getCurrentMonthLastDate();
		CommonUtils.getFistDateOfYear();
		CommonUtils.getLastDateOfYear();
		JSONObject jsonObject = new JSONObject();
		CostManagementQueryMonthlyDocument costManagementQueryDocument = new CostManagementQueryMonthlyDocument();
		AzureProfile azureProfile = new AzureProfile(AzureEnvironment.AZURE);
		ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
				.clientId(azureRecord().getClientId()).clientSecret(azureRecord().getClientSecret())
				.tenantId(azureRecord().getTenantId()).build();
		CostManagementManager costManagementManager = CostManagementManager.authenticate(clientSecretCredential,
				azureProfile);
		// LocalDate yearStartDate = LocalDate.now().withDayOfYear(1);
		String startDate = CommonUtils.getCurrentMonthFirstDate().toString();
		// LocalDate date = LocalDate.now().minusDays(1);
		String endDate = CommonUtils.getCurrentMonthLastDate().toString();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constant.DATE_FORMAT);
		LocalDate fromLocalDate = LocalDate.parse(startDate, formatter);
		LocalDate toLocalDate = LocalDate.parse(endDate, formatter);
		OffsetDateTime from = OffsetDateTime.of(fromLocalDate, LocalTime.NOON, ZoneOffset.UTC);
		OffsetDateTime to = OffsetDateTime.of(toLocalDate, LocalTime.NOON, ZoneOffset.UTC);
		List<AzureSubscriptions> azureSubscriptions = azureSubscriptionsRepository.findAll();
		for (AzureSubscriptions subscription : azureSubscriptions) {
			String url = Constant.SUBSCRIPTIONS;
			url = url.replace(Constant.SUB_ID, subscription.getSubscriptionId());
			Map<String, QueryAggregation> map = new HashMap<>();
			map.put(Constant.TOTAL_COST, new QueryAggregation().withName("Cost").withFunction(FunctionType.SUM));
			map.put(Constant.TOTAL_COST_USD, new QueryAggregation().withName(Constant.COST_USD).withFunction(FunctionType.SUM));
			Response<QueryResult> result = null;
			try {
				result = costManagementManager.queries().usageWithResponse(url,
						new QueryDefinition().withType(ExportType.ACTUAL_COST).withDataset(
								new QueryDataset().withGranularity(GranularityType.fromString("None")).withAggregation(map)
						/*
						 * .withGrouping(Arrays.asList( new
						 * QueryGrouping().withType(QueryColumnType.DIMENSION).withName("BillingPeriod")
						 * ))
						 */).withTimeframe(TimeframeType.CUSTOM)
								.withTimePeriod(new QueryTimePeriod().withFrom(from).withTo(to)),
						Context.NONE);
			}catch(Exception e) {
				throw new DataValidationException(e.getMessage(), null, HttpStatus.BAD_REQUEST);
			}
			List<QueryColumnsResponse> columnsResponses = new ArrayList<>();
			for (QueryColumn s : result.getValue().columns()) {
				QueryColumnsResponse columnsResponse = new QueryColumnsResponse();
				columnsResponse.setName(s.name());
				columnsResponse.setType(s.type());
				columnsResponses.add(columnsResponse);
			}
			CostManagementQueryMonthlyDocument document = managementQueryMonthlyRepository
					.findBySubscriptionId(subscription.getSubscriptionId());
			if (document == null) {
				costManagementQueryDocument.setId(
						sequenceGeneratorService.generateSequence(CostManagementQueryMonthlyDocument.SEQUENCE_NAME));
				costManagementQueryDocument.setAmigoId(
						sequenceGeneratorService.generateSequence(Constant.SEQUENCE_NAME));
				costManagementQueryDocument.setColumn(columnsResponses);
				costManagementQueryDocument.setRows(result.getValue().rows());
				costManagementQueryDocument.setName(result.getValue().name());
				costManagementQueryDocument.setType(result.getValue().type());
				costManagementQueryDocument.setSubscriptionId(subscription.getSubscriptionId());
				costManagementQueryDocument.setCreatedOn(new Date());
				costManagementQueryDocument.setUpdatedOn(new Date());
				costManagementQueryDocument.setOpID(opID);
				costManagementQueryDocument.setBuID(buID);
				jsonObject.put("Monthly", costManagementQueryDocument);
				managementQueryMonthlyRepository.save(costManagementQueryDocument);
			} else {
				document.setColumn(columnsResponses);
				document.setRows(result.getValue().rows());
				document.setName(result.getValue().name());
				document.setType(result.getValue().type());
				document.setUpdatedOn(new Date());
				document.setOpID(opID);
				document.setBuID(buID);
				managementQueryMonthlyRepository.save(document);
			}
		}
		return jsonObject;
	}

	@Override
	public JSONObject getActualCostYearly() throws DataValidationException {
		CommonUtils.getFistDateOfYear();
		CommonUtils.getLastDateOfYear();
		JSONObject jsonObject = new JSONObject();
		CostManagementQueryYearlyDocument costManagementQueryDocument = new CostManagementQueryYearlyDocument();
		AzureProfile azureProfile = new AzureProfile(AzureEnvironment.AZURE);
		ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
				.clientId(azureRecord().getClientId()).clientSecret(azureRecord().getClientSecret())
				.tenantId(azureRecord().getTenantId()).build();
		CostManagementManager costManagementManager = CostManagementManager.authenticate(clientSecretCredential,
				azureProfile);
		// LocalDate yearStartDate = LocalDate.now().withDayOfYear(1);
		String startDate = CommonUtils.getFistDateOfYear().toString();
		// LocalDate date = LocalDate.now().minusDays(1);
		String endDate = CommonUtils.getLastDateOfYear().toString();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constant.DATE_FORMAT);
		LocalDate fromLocalDate = LocalDate.parse(startDate, formatter);
		LocalDate toLocalDate = LocalDate.parse(endDate, formatter);
		OffsetDateTime from = OffsetDateTime.of(fromLocalDate, LocalTime.NOON, ZoneOffset.UTC);
		OffsetDateTime to = OffsetDateTime.of(toLocalDate, LocalTime.NOON, ZoneOffset.UTC);
		List<AzureSubscriptions> azureSubscriptions = azureSubscriptionsRepository.findAll();
		for (AzureSubscriptions subscription : azureSubscriptions) {
			String url = Constant.SUBSCRIPTIONS;
			url = url.replace(Constant.SUB_ID, subscription.getSubscriptionId());
			Map<String, QueryAggregation> map = new HashMap<>();
			map.put(Constant.TOTAL_COST, new QueryAggregation().withName("Cost").withFunction(FunctionType.SUM));
			map.put(Constant.TOTAL_COST_USD, new QueryAggregation().withName(Constant.COST_USD).withFunction(FunctionType.SUM));
			Response<QueryResult> result = null;
			try {
				result = costManagementManager.queries().usageWithResponse(url,
						new QueryDefinition().withType(ExportType.ACTUAL_COST).withDataset(
								new QueryDataset().withGranularity(GranularityType.fromString("None")).withAggregation(map)
						/*
						 * .withGrouping(Arrays.asList( new
						 * QueryGrouping().withType(QueryColumnType.DIMENSION).withName("BillingPeriod")
						 * ))
						 */).withTimeframe(TimeframeType.CUSTOM)
								.withTimePeriod(new QueryTimePeriod().withFrom(from).withTo(to)),
						Context.NONE);
			}catch(Exception e) {
				throw new DataValidationException(e.getMessage(), null, HttpStatus.BAD_REQUEST);
			}
			List<QueryColumnsResponse> columnsResponses = new ArrayList<>();
			for (QueryColumn s : result.getValue().columns()) {
				QueryColumnsResponse columnsResponse = new QueryColumnsResponse();
				columnsResponse.setName(s.name());
				columnsResponse.setType(s.type());
				columnsResponses.add(columnsResponse);
			}
			CostManagementQueryYearlyDocument document = managementQueryYearlyRepository
					.findBySubscriptionId(subscription.getSubscriptionId());
			if (document == null) {
				costManagementQueryDocument.setId(
						sequenceGeneratorService.generateSequence(CostManagementQueryYearlyDocument.SEQUENCE_NAME));
				costManagementQueryDocument.setAmigoId(
						sequenceGeneratorService.generateSequence(Constant.SEQUENCE_NAME));
				costManagementQueryDocument.setColumn(columnsResponses);
				costManagementQueryDocument.setRows(result.getValue().rows());
				costManagementQueryDocument.setName(result.getValue().name());
				costManagementQueryDocument.setType(result.getValue().type());
				costManagementQueryDocument.setSubscriptionId(subscription.getSubscriptionId());
				costManagementQueryDocument.setCreatedOn(new Date());
				costManagementQueryDocument.setUpdatedOn(new Date());
				jsonObject.put("Yearly", costManagementQueryDocument);
				costManagementQueryDocument.setBuID(buID);
				costManagementQueryDocument.setOpID(opID);
				managementQueryYearlyRepository.save(costManagementQueryDocument);
			} else {
				document.setColumn(columnsResponses);
				document.setRows(result.getValue().rows());
				document.setName(result.getValue().name());
				document.setType(result.getValue().type());
				document.setUpdatedOn(new Date());
				document.setOpID(opID);
				document.setBuID(buID);
				managementQueryYearlyRepository.save(document);
			}
		}
		return jsonObject;
	}

	@Override
	public CommonResponse createBudget(BudgetRequest budgetRequest) {
		ConsumptionManager consumptionManager = ConsumptionManager.authenticate(
				new ClientSecretCredentialBuilder().clientId(azureRecord().getClientId())
						.clientSecret(azureRecord().getClientSecret()).tenantId(azureRecord().getTenantId()).build(),
				new AzureProfile(AzureEnvironment.AZURE));
		List<AzureSubscriptions> azureSubscriptions = azureSubscriptionsRepository.findAll().stream()
				.filter(p -> p.getSubscriptionId().equalsIgnoreCase(budgetRequest.getBudgetScope()))
				.collect(Collectors.toList());
		for (AzureSubscriptions subscription : azureSubscriptions) {
			String scope = Constant.SUBSCRIPTIONS.replace(Constant.SUB_ID, subscription.getSubscriptionId());
			LocalDate start = LocalDate.parse(budgetRequest.getCreationDate(),
					DateTimeFormatter.ofPattern(Constant.DATE_FORMAT));
			LocalDate end = LocalDate.parse(budgetRequest.getExpiryDate(), DateTimeFormatter.ofPattern(Constant.DATE_FORMAT));
			OffsetDateTime startoffsetDateTime = start.atStartOfDay().atOffset(ZoneOffset.UTC);
			OffsetDateTime endoffsetDateTime = end.atStartOfDay().atOffset(ZoneOffset.UTC);
			Map<String, Notification> notification = new HashMap<String, Notification>();
			for (BudgetAlertDetailRequest alertDetails : budgetRequest.getAlertDetails()) {
				String random = UUID.randomUUID().toString();
				notification.put(random,
						new Notification().withEnabled(true)
								.withOperator(com.azure.resourcemanager.consumption.models.OperatorType.GREATER_THAN)
								.withThreshold(new BigDecimal(alertDetails.getThreshold()))
								.withContactEmails(Arrays.asList(budgetRequest.getRecipientEmail()))
								.withThresholdType(ThresholdType.fromString(alertDetails.getType()))
								.withLocale(CultureCode.EN_US));
			}
			consumptionManager.budgets().define(budgetRequest.getBudgetName()).withExistingScope(scope)
					.withCategory(CategoryType.COST)
					.withAmount(new BigDecimal(budgetRequest.getBudgetAmount().getValue()))
					.withTimeGrain(TimeGrainType.fromString(budgetRequest.getResetPeriod()))
					.withTimePeriod(
							new BudgetTimePeriod().withStartDate(startoffsetDateTime).withEndDate(endoffsetDateTime))
					.withNotifications(notification).create();
		}
		if (azureSubscriptions.isEmpty()) {
			return new CommonResponse(HttpStatus.NOT_FOUND,
					new saaspe.azure.model.Response("CreateAzureBudget", Arrays.asList("Subscription Not Found")),
					"Budget Creation Failed");
		}
		return new CommonResponse(HttpStatus.CREATED,
				new saaspe.azure.model.Response("CreateAzureBudget", new ArrayList<>()), "Budget Created Successfully");
	}
	
	@Override
	public void updateRecommendations() {
		List<AdvisorDocument> advisorDocuments = advisorRecommendationsRepository.findAll();
		for(AdvisorDocument advisorDocument :advisorDocuments) {
			if(advisorDocument.getUpdatedOn()==null) {
				advisorDocument.setUpdatedOn(new Date());
				advisorDocument.setBuID(buID);
				advisorDocument.setOpID(opID);
				advisorRecommendationsRepository.save(advisorDocument);
			}
		}
	}
	
	@Override
	public CommonResponse getMonthlySpendingHistory() throws DataValidationException, InterruptedException, IOException {
	    AzureProfile azureProfile = new AzureProfile(AzureEnvironment.AZURE);
	    ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
	            .clientId(azureRecord().getClientId())
	            .clientSecret(azureRecord().getClientSecret())
	            .tenantId(azureRecord().getTenantId())
	            .build();
	    CostManagementManager costManagementManager = CostManagementManager.authenticate(clientSecretCredential, azureProfile);

	    LocalDate currentDate = LocalDate.now();
	    int currentYear = currentDate.getYear();
	    int currentMonth = currentDate.getMonthValue();

	    MonthlySpendingResponse monthlySpendingResponse = new MonthlySpendingResponse();
	    List<Map<String, Object>> dataList = new ArrayList<>();

	    for (int i = 0; i < 12; i++) {
	        Thread.sleep(18000);

	        YearMonth yearMonth = YearMonth.of(currentYear, currentMonth).minusMonths(i);
	        String monthName = yearMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
	        int year = yearMonth.getYear();

	        Map<String, Object> monthlyData = new LinkedHashMap<>();
	        monthlyData.put("month", monthName + " " + year);

	        AzureSpendingHistoryDocument document = azureSpendHistoryRepository.findByMonthAndYear(monthName, year);

	        CloudProvider cloudProvider = new CloudProvider();
	        cloudProvider.setName("Azure");
	        List<CostInfo> costInfos = new ArrayList<>();

	        if (document != null && !(i == 0 && currentDate.getDayOfMonth() < yearMonth.lengthOfMonth())) {
	            CostInfo costInfoINR = new CostInfo();
	            costInfoINR.setValueInINR(document.getTotalCostINR());
	            costInfoINR.setCurrency("INR");

	            CostInfo costInfoUSD = new CostInfo();
	            costInfoUSD.setValueInUSD(document.getTotalCostUSD());
	            costInfoUSD.setCurrency("USD");

	            costInfos.add(costInfoINR);
	            costInfos.add(costInfoUSD);
	        } else {
	            OffsetDateTime startDate = OffsetDateTime.of(year, yearMonth.getMonthValue(), 1, 0, 0, 0, 0, ZoneOffset.UTC);
	            OffsetDateTime endDate;

	            if (i == 0) {
	                endDate = OffsetDateTime.of(year, yearMonth.getMonthValue(), currentDate.getDayOfMonth(), 23, 59, 59, 999, ZoneOffset.UTC);
	            } else {
	                int lastDayOfMonth = yearMonth.lengthOfMonth();
	                endDate = OffsetDateTime.of(year, yearMonth.getMonthValue(), lastDayOfMonth, 23, 59, 59, 999, ZoneOffset.UTC);
	            }

	            String url = Constant.SUBSCRIPTIONS.replace(Constant.SUB_ID, subscriptionID);
	            Map<String, QueryAggregation> map = new HashMap<>();
	            map.put(Constant.TOTAL_COST, new QueryAggregation().withName("Cost").withFunction(FunctionType.SUM));

	            Response<QueryResult> result;
	            Thread.sleep(15000);

	            try {
	                result = costManagementManager.queries().usageWithResponse(url,
	                        new QueryDefinition().withType(ExportType.ACTUAL_COST)
	                                .withDataset(new QueryDataset().withGranularity(GranularityType.fromString("Monthly")).withAggregation(map))
	                                .withTimeframe(TimeframeType.CUSTOM)
	                                .withTimePeriod(new QueryTimePeriod().withFrom(startDate).withTo(endDate)),
	                        Context.NONE);
	            } catch (Exception e) {
	                throw new DataValidationException(e.getMessage(), null, HttpStatus.BAD_REQUEST);
	            }

	            QueryResult jsonValue = result.getValue();
	            if (jsonValue != null && jsonValue.rows() != null) {
	                BigDecimal totalCostInINR = BigDecimal.ZERO;
	                BigDecimal totalCostInUSD = BigDecimal.ZERO;

	                for (List<Object> row : jsonValue.rows()) {
	                    BigDecimal totalMonthlyCostForSubscription = new BigDecimal(row.get(0).toString());
	                    BigDecimal gstRate = BigDecimal.valueOf(0.18);
	                    BigDecimal gstAmount = totalMonthlyCostForSubscription.multiply(gstRate);
	                    BigDecimal totalCostWithGst = totalMonthlyCostForSubscription.add(gstAmount);

	                    totalCostInINR = totalCostInINR.add(totalCostWithGst);
	                    totalCostInUSD = totalCostInUSD.add(getCurrentUSDValue().multiply(totalCostWithGst));
	                }

	                CostInfo costInfoINR = new CostInfo();
	                costInfoINR.setValueInINR(totalCostInINR);
	                costInfoINR.setCurrency("INR");

	                CostInfo costInfoUSD = new CostInfo();
	                costInfoUSD.setValueInUSD(totalCostInUSD);
	                costInfoUSD.setCurrency("USD");

	                costInfos.add(costInfoINR);
	                costInfos.add(costInfoUSD);

	                if (document == null) {
	                    document = new AzureSpendingHistoryDocument();
	                    document.setId(sequenceGeneratorService.generateSequence(AzureSpendingHistoryDocument.SEQUENCE_NAME));
	                }

	                document.setCloudProvider("Azure");
	                document.setMonth(monthName);
	                document.setYear(year);
	                document.setTotalCostINR(totalCostInINR);
	                document.setTotalCostUSD(totalCostInUSD);
	                document.setCreatedOn(new Date());
	                document.setUpdatedOn(new Date());
	                azureSpendHistoryRepository.save(document);
	            }
	        }

	        Map<String, String> costMap = new LinkedHashMap<>();
	        for (CostInfo costInfo : costInfos) {
	            costMap.put(costInfo.getCurrency(), costInfo.getValue().toString());
	        }

	        Map<String, Object> serviceMap = new LinkedHashMap<>();
	        serviceMap.put("name", "Azure");
	        serviceMap.put("cost", costMap);

	        monthlyData.put("CloudProvider", Collections.singletonList(serviceMap));
	        dataList.add(monthlyData);
	    }

	    monthlySpendingResponse.setMonthlySpendingHistory(dataList);
	    return new CommonResponse(HttpStatus.OK,
	            new saaspe.azure.model.Response("MonthlySpendingHistory", monthlySpendingResponse),
	            "Monthly spending history retrieved successfully.");
	}

	private BigDecimal getCurrentUSDValue() throws IOException, InterruptedException {
	    HttpClient client = HttpClient.newHttpClient();
	    HttpRequest request = HttpRequest.newBuilder()
	            .uri(URI.create("https://open.er-api.com/v6/latest/USD"))
	            .build();
	    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
	    JSONObject json = new JSONObject(response.body());
	    BigDecimal usdToInrRate = json.getJSONObject("rates").getBigDecimal("INR");
	    BigDecimal inrToUsdRate = BigDecimal.ONE.divide(usdToInrRate, 4, RoundingMode.HALF_UP);
	    return inrToUsdRate;
	}

}
