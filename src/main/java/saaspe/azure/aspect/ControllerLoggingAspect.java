package saaspe.azure.aspect;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import saaspe.azure.model.API;

@Aspect
@Component
@EnableAspectJAutoProxy
@Configuration
public class ControllerLoggingAspect {

	@Autowired
	private ObjectMapper mapper;

	private static final int MAX_LENGTH = 1000;

	private final Logger logger = LoggerFactory.getLogger(ControllerLoggingAspect.class);

	@Around("@within(saaspe.azure.aspect.ControllerLogging) || @annotation(saaspe.azure.aspect.ControllerLogging)")
	public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
		long startTime = System.currentTimeMillis();
		String methodName = joinPoint.getSignature().getName();
		String className = joinPoint.getTarget().getClass().getSimpleName();
		Object[] args = joinPoint.getArgs();
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = (requestAttributes instanceof ServletRequestAttributes)
				? ((ServletRequestAttributes) requestAttributes).getRequest()
				: null;
		HttpServletResponse response = (requestAttributes instanceof ServletRequestAttributes)
				? ((ServletRequestAttributes) requestAttributes).getResponse()
				: null;
		String traceId = (response != null) ? response.getHeader("X-Trace-Id") : null;

		// Set MDC trace id to the value in response header
		if (traceId != null) {
			org.slf4j.MDC.put("traceId", traceId);
		}

		StringBuilder logMessage = new StringBuilder();
		logMessage.append("[START] {}.{} [TRACE ID: {}]").append(System.lineSeparator());
		if (request != null) {
			logMessage.append("Request URL: {} {}, From: {}").append(System.lineSeparator());
			logMessage.append("Headers: {} [TRACE ID: {}]").append(System.lineSeparator());
			logMessage.append("Parameters: {}").append(System.lineSeparator());
		}
		logMessage.append("Method Arguments: {}").append(System.lineSeparator());

		logger.info(logMessage.toString(), className, methodName, traceId,
				(request != null) ? request.getMethod() : null, (request != null) ? request.getRequestURL() : null,
				(request != null) ? request.getHeader("X-From") : null,
				(request != null) ? getRequestHeaders(request) : null, traceId,
				(request != null) ? getRequestParameters(request) : null, Arrays.toString(args));

		Object result;
		API api = getAPIFromJoinPoint(joinPoint);
		try {
			result = joinPoint.proceed();
			api.setRsp(StringUtils.abbreviate(mapper.writeValueAsString(result), MAX_LENGTH));
			logger.info("Method Returned: {}", api);
		} catch (Throwable t) {
			String errorMessage = String.format("Exception in method %s: %s: %s", methodName, t.getMessage(), traceId);
			logger.error(errorMessage, t);
			throw t;
		} finally {
			// Remove MDC trace id
			org.slf4j.MDC.remove("traceId");
			logger.info("[END] {}.{} [TIME ELAPSED: {}ms] [TRACE ID: {}]", className, methodName,
					System.currentTimeMillis() - startTime, traceId);
		}

		return result;
	}

	private Map<String, String> getRequestHeaders(HttpServletRequest request) {
		return Collections.list(request.getHeaderNames()).stream()
				.collect(Collectors.toMap(header -> header, request::getHeader));
	}

	private Map<Object, Object> getRequestParameters(HttpServletRequest request) {
		return request.getParameterMap().entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> Arrays.toString(entry.getValue())));
	}

	private API getAPIFromJoinPoint(JoinPoint joinPoint) {
		Map<String, Object> ipl = new HashMap<>();
		int i = 1;
		for (Object obj : joinPoint.getArgs()) {
			if (obj != null && !obj.getClass().getName().startsWith("org.spring")) {
				ipl.put(i + ":" + obj.getClass().getName(), obj);
				i++;
			}
		}

		Signature signature = joinPoint.getSignature();
		API api = new API();
		api.setCls(signature.getDeclaringTypeName());
		api.setOpp(signature.getName());
		api.setIpl(ipl);
		return api;
	}
}
