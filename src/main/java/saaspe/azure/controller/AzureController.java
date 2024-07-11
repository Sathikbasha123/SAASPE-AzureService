package saaspe.azure.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.azure.core.exception.HttpResponseException;

import saaspe.azure.aspect.ControllerLogging;
import saaspe.azure.model.BudgetRequest;
import saaspe.azure.model.CommonResponse;
import saaspe.azure.model.MultiCloudCountResponse;
import saaspe.azure.model.Response;
import saaspe.azure.service.AzureService;

@RestController
@RequestMapping("/api/mutlicloud")
@ControllerLogging
public class AzureController {

	private static final Logger log = LoggerFactory.getLogger(AzureController.class);

	@Autowired
	private AzureService azureService;

	@Scheduled(cron = "0 */10 * ? * *")
	public ResponseEntity<CommonResponse> getAllSubscriptions() {
		String traceId = UUID.randomUUID().toString();
		try {
			log.info("[TRACE_ID: {}] Start of getAllSubscriptions {}", traceId, LocalDate.now(ZoneId.systemDefault()));
			CommonResponse response = azureService.getAllSubscriptions();
			log.info("[TRACE_ID: {}] Success In getAllSubscriptions {}", traceId, response);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("[TRACE_ID: {}] Error in getAllSubscriptions: {}", traceId, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@Scheduled(cron = "0 */15 * ? * *")
	public ResponseEntity<CommonResponse> getAllResourceGroups() {
		String traceId = UUID.randomUUID().toString();
		try {
			log.info("[TRACE_ID: {}] Start of getAllResourceGroups: {}", traceId, LocalDate.now(ZoneId.systemDefault()));
			CommonResponse response = azureService.getAllResourceGroups();
			log.info("[TRACE_ID: {}] Success In getAllResourceGroups: {}", traceId, response);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("[TRACE_ID: {}] Error in getAllResourceGroups: {}", traceId, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@Scheduled(cron = "0 */20 * ? * *")
	public ResponseEntity<CommonResponse> getAllResourceGroupsActualCost() {
		String traceId = UUID.randomUUID().toString();
		try {
			log.info("[TRACE_ID: {}] Start of getAllResourceGroupsActualCost: {}", traceId, LocalDate.now(ZoneId.systemDefault()));
			CommonResponse response = azureService.getAllResourceGroupsActualCost();
			log.info("[TRACE_ID: {}] Success In getAllResourceGroupsActualCost: {}", traceId, response);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("[TRACE_ID: {}] Error in getAllResourceGroupsActualCost: {}", traceId, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@Scheduled(cron = "0 */25 * ? * *")
	public ResponseEntity<CommonResponse> getAllResources() {
		String traceId = UUID.randomUUID().toString();
		try {
			log.info("[TRACE_ID: {}] Start of getAllResources: {}", traceId, LocalDate.now(ZoneId.systemDefault()));
			CommonResponse response = azureService.getAllResources();
			log.info("[TRACE_ID: {}] Success In getAllResources: {}", traceId, response);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("[TRACE_ID: {}] Error in getAllResources: {}", traceId, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@Scheduled(cron = "0 */30 * ? * *")
	public ResponseEntity<CommonResponse> getBudgets() {
		String traceId = UUID.randomUUID().toString();
		try {
			log.info("[TRACE_ID: {}] Start of getBudgets: {}", traceId, LocalDate.now(ZoneId.systemDefault()));
			CommonResponse response = azureService.getBudgets();
			log.info("[TRACE_ID: {}] Success In getBudgets: {}", traceId, response);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("[TRACE_ID: {}] Error in getBudgets: {}", traceId, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@Scheduled(cron = "0 */10 * ? * *")
	public ResponseEntity<List<JSONObject>> getActualCostMonthQuarterYear() {
		String traceId = UUID.randomUUID().toString();
		try {
			List<JSONObject> obsjects = new ArrayList<>();
			log.info("Azure getActualCostMonthQuarterYear start");
			log.info("Azure getActualCostMonthly start");
			JSONObject month = azureService.getActualCostMonthly();
			obsjects.add(month);
			log.info("Azure getActualCostMonthly success");
			Thread.sleep(10000);
			log.info("Azure getActualCostQuarterly start");
			JSONObject quarter = azureService.getActualCostQuarterly();
			obsjects.add(quarter);
			log.info("Azure getActualCostQuarterly success");
			Thread.sleep(10000);
			log.info("Azure getActualCostYearly start");
			JSONObject year = azureService.getActualCostYearly();
			obsjects.add(year);
			log.info("Azure getActualCostYearly success");
			log.info("Azure getActualCostMonthQuarterYear success");
			log.info("[TRACE_ID: {}] Success In getActualCostMonthQuarterYear: {}", traceId, obsjects);
			return ResponseEntity.ok(obsjects);
		} catch (Exception e) {
			log.error("[TRACE_ID: {}] Error in getActualCostMonthQuarterYear: {}", traceId, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@Scheduled(cron = "0 */45 * ? * *")
	public ResponseEntity<CommonResponse> getActualCostBySerivceName() {
		String traceId = UUID.randomUUID().toString();
		try {
			log.info("[TRACE_ID: {}] Start of getActualCostBySerivceName: {}", traceId, LocalDate.now(ZoneId.systemDefault()));
			CommonResponse obsjects = azureService.getActualCostBySerivceName();
			log.info("[TRACE_ID: {}] Success In getActualCostBySerivceName: {}", traceId, obsjects);
			return ResponseEntity.ok(obsjects);
		} catch (Exception e) {
			log.error("[TRACE_ID: {}] Error in getActualCostBySerivceName: {}", traceId, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	// @GetMapping("/overview")
	public ResponseEntity<MultiCloudCountResponse> getCountBasedOnSubscriptionType() {
		String traceId = UUID.randomUUID().toString();
		try {
			MultiCloudCountResponse response = azureService.getCountBasedOnSubscriptionType();
			log.info("Azure getCountBasedOnSubscriptionType success");
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("[TRACE_ID: {}] Error In getAllSubscriptions {}", traceId, e);
			return ResponseEntity.badRequest().build();
		}
	}

	@Scheduled(cron = "0 */50 * ? * *")
	public ResponseEntity<CommonResponse> getForecastUsage() {
		String traceId = UUID.randomUUID().toString();
		try {
			log.info("[TRACE_ID: {}] Start of getForecastUsage: {}", traceId, LocalDate.now(ZoneId.systemDefault()));
			CommonResponse response = azureService.getForecastUsage();
			log.info("[TRACE_ID: {}] Success In getForecastUsage: {}", traceId, response);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("[TRACE_ID: {}] Error in getForecastUsage: {}", traceId, e.getMessage(), e);
			Thread.currentThread().interrupt();
			return ResponseEntity.badRequest().build();
		}
	}

	@Scheduled(cron = "0 */55 * ? * *")
	public ResponseEntity<CommonResponse> getRecommendations() {
		String traceId = UUID.randomUUID().toString();
		try {
			log.info("[TRACE_ID: {}] Start of getRecommendations: {}", traceId, LocalDate.now(ZoneId.systemDefault()));
			CommonResponse response = azureService.getRecommendations();
			log.info("[TRACE_ID: {}] Success In getRecommendations: {}", traceId, response);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("[TRACE_ID: {}] Error in getRecommendations: {}", traceId, e.getMessage(), e);
			return ResponseEntity.badRequest().build();
		}
	}

	@Scheduled(cron = "0 */15 * ? * *")
	public void initialHit() {
		String traceId = UUID.randomUUID().toString();
		try {
			log.info("initialHit start");
			azureService.initialHit();
			log.info("initialHit End");
			log.info("[TRACE_ID: {}] Success In initialHit: {}", traceId,"initialHit() method");
		} catch (Exception e) {
			log.error("[TRACE_ID: {}] Error in initialHit: {}", traceId, e.getMessage(), e);
			Thread.currentThread().interrupt();
		}
	}

	@PostMapping(value = "/budget/create")
	public ResponseEntity<CommonResponse> createBudget(@RequestBody BudgetRequest budgetRequest) {
		String traceId = UUID.randomUUID().toString();

		try {
			CommonResponse response = azureService.createBudget(budgetRequest);
			log.info("[TRACE_ID: {}] Success In createBudget: {}", traceId, response);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (HttpResponseException ex) {
			String name = ex.getValue().toString();
			if (ex.getValue().toString().indexOf("(") != -1) {
				name = ex.getValue().toString().substring(0, ex.getValue().toString().indexOf("("));
			}
			return ResponseEntity.ok().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("CreateAzureBudget", new ArrayList<>()), name));
		} catch (Exception e) {
			log.error("[TRACE_ID: {}] Error in createBudget: {}", traceId, e.getMessage(), e);
			return ResponseEntity.ok().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("CreateAzureBudget", new ArrayList<>()), e.getMessage()));
		}
	}
	
	@GetMapping("/recommendation/update")
	public void updateRecommendations() {
		String traceId = UUID.randomUUID().toString();
		try {
			log.info("updatedRecommendations start");
			azureService.updateRecommendations();
			log.info("updatedRecommendations End");
			log.info("[TRACE_ID: {}] Success In updatedRecommendations: {}", traceId,"updatedRecommendations() method");
		} catch (Exception e) {
			log.error("[TRACE_ID: {}] Error in updatedRecommendations: {}", traceId, e.getMessage(), e);
		}
	}
	
	@Scheduled(cron = "0 */30 * ? * *")
	public ResponseEntity<CommonResponse> getMonthlySpendHistory() {
		String traceId = UUID.randomUUID().toString();
		try {
			log.info("[TRACE_ID: {}] Start of getMonthlySpendHistory: {}", traceId, LocalDate.now(ZoneId.systemDefault()));
			CommonResponse response = azureService. getMonthlySpendingHistory();
			log.info("[TRACE_ID: {}] Success In getMonthlySpendHistory: {}", traceId, response);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("[TRACE_ID: {}] Error in getMonthlySpendHistory: {}", traceId, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}
}
