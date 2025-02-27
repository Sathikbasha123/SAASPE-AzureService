package saaspe.azure.aspect;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

public class CustomMaskingFilter extends AbstractMatcherFilter<ILoggingEvent> {

	@Override
	public FilterReply decide(ILoggingEvent event) {
		if (!isStarted()) {
			return FilterReply.NEUTRAL;
		}

		String message = event.getFormattedMessage();
		String maskedMessage = maskSensitiveInformation(message);

		if (!message.equals(maskedMessage)) {
			event.getMessage();
			return onMatch;
		}

		return onMismatch;
	}

	private String maskSensitiveInformation(String message) {
		// Perform your custom logic to mask sensitive information here
		// For example, let's replace all occurrences of "password" with asterisks
		return message.replaceAll("password", "********");
	}
}
