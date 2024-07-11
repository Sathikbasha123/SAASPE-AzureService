package saaspe.azure.serviceImpl;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.consumption.models.Budget;

import saaspe.azure.document.BudgetsDocument;
import saaspe.azure.repository.BudgetRepository;

@Service
public class AsyncServiceImpl {
	
	@Autowired
	private BudgetRepository budgetRepository;
	
	public void deleteBudgets(PagedIterable<Budget> budgets,String subId) {
		 ExecutorService executor = Executors.newSingleThreadExecutor();
	        executor.submit(() -> {
	        	List<BudgetsDocument> existingBudgets = budgetRepository.findBySubscriptionsId(subId);
	    		for(BudgetsDocument doc : existingBudgets) {
	    			if(budgets.stream().map(p->p.id()).noneMatch(doc.getBudgetId()::equals)) {
	    				budgetRepository.delete(doc);
	    			}
	    		}
	            executor.shutdown();
	        });
	}

}
