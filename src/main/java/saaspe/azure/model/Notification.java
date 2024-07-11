package saaspe.azure.model;

import java.math.BigDecimal;
import java.util.List;

import com.azure.resourcemanager.consumption.models.CultureCode;
import com.azure.resourcemanager.consumption.models.OperatorType;
import com.azure.resourcemanager.consumption.models.ThresholdType;

import lombok.Data;

@Data
public class Notification {

    private boolean enabled;

    private OperatorType operator;

    private BigDecimal threshold;

    private List<String> contactEmails;

    private List<String> contactRoles;

    private List<String> contactGroups;

    private ThresholdType thresholdType;

    private CultureCode locale;

}
