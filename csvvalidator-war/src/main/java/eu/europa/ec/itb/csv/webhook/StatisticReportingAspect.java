package eu.europa.ec.itb.csv.webhook;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.handler.MessageContext;

import com.gitb.tr.TAR;
import com.gitb.tr.TestResultType;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import eu.europa.ec.itb.commons.war.webhook.StatisticReporting;
import eu.europa.ec.itb.commons.war.webhook.UsageData;
import eu.europa.ec.itb.csv.ApplicationConfig;
import eu.europa.ec.itb.csv.gitb.ValidationServiceImpl;
import eu.europa.ec.itb.csv.validation.CSVValidator;

@Aspect
@Component
@ConditionalOnProperty(name = "validator.webhook.statistics")
public class StatisticReportingAspect extends StatisticReporting {

    private static final Logger logger = LoggerFactory.getLogger(StatisticReportingAspect.class);

    private static ThreadLocal<Map<String, String>> adviceContext = new ThreadLocal<>();

    @Autowired
    private ApplicationConfig config;

    /**
     * Pointcut for minimal WEB validation.
     */
    @Pointcut("execution(public * eu.europa.ec.itb.csv.web.UploadController.handleUploadM(..))")
    private void minimalUploadValidation() {
    }

    /**
     * Pointcut for regular WEB validation.
     */
    @Pointcut("execution(public * eu.europa.ec.itb.csv.web.UploadController.handleUpload(..))")
    private void uploadValidation() {
    }

    /**
     * Advice to obtain the arguments passed to the web upload API call.
     */
    @Before("minimalUploadValidation() || uploadValidation()")
    public void getUploadContext(JoinPoint joinPoint) throws Throwable {
        Map<String, String> contextParams = new HashMap<String, String>();
        contextParams.put("api", StatisticReportingConstants.WEB_API);
        if (config.getWebhook().isStatisticsEnableCountryDetection()) {
            HttpServletRequest request = getHttpRequest(joinPoint);
            if (request != null) {
                String ip = extractIpAddress(request);
                contextParams.put("ip", ip);
            }
        }
        adviceContext.set(contextParams);
    }

    /**
     * Advice to obtain the arguments passed to the SOAP API call.
     */
    @Before(value = "execution(public * eu.europa.ec.itb.csv.gitb.ValidationServiceImpl.validate(..))")
    public void getSoapCallContext(JoinPoint joinPoint) throws Throwable {
        Map<String, String> contextParams = new HashMap<String, String>();
        contextParams.put("api", StatisticReportingConstants.SOAP_API);
        if (config.getWebhook().isStatisticsEnableCountryDetection()) {
            ValidationServiceImpl validationService = (ValidationServiceImpl) joinPoint.getTarget();
            HttpServletRequest request = (HttpServletRequest) validationService.getWebServiceContext()
                    .getMessageContext().get(MessageContext.SERVLET_REQUEST);
            String ip = extractIpAddress(request);
            contextParams.put("ip", ip);
        }
        adviceContext.set(contextParams);
    }

    /**
     * Advice to send the usage report.
     */
    @Around("execution(public * eu.europa.ec.itb.csv.validation.CSVValidator.validate(..))")
    public Object reportValidatorDataUsage(ProceedingJoinPoint joinPoint) throws Throwable {
        CSVValidator validator = (CSVValidator) joinPoint.getTarget();
        Object report = joinPoint.proceed();
        try {
            Map<String, String> usageParams = adviceContext.get();
            String validatorId = config.getIdentifier();
            String domain = validator.getDomain();
            String validationType = validator.getValidationType();
            String api = usageParams.get("api");
            // obtain the result of the model
            String ip = usageParams.get("ip");
            TAR reportTAR = (TAR) report;
            UsageData.Result result = extractResult(reportTAR);
            // Send the usage data
            sendUsageData(validatorId, domain, api, validationType, result, ip);
        } catch (Exception ex) {
            // Ensure unexpected errors never block validation processing
            logger.warn("Unexpected error during statistics reporting", ex);
        } finally {
            return report;
        }
    }

    /**
     * Method that obtains a TAR object and obtains the result of the validation to
     * be reported.
     */
    private UsageData.Result extractResult(TAR report) {
        TestResultType tarResult = report.getResult();
        if (tarResult == TestResultType.SUCCESS) {
            return UsageData.Result.SUCCESS;
        } else if (tarResult == TestResultType.WARNING) {
            return UsageData.Result.WARNING;
        } else {
            return UsageData.Result.FAILURE;
        }
    }

}