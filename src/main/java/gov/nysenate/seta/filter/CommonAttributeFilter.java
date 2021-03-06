package gov.nysenate.seta.filter;

import gov.nysenate.seta.model.payroll.MiscLeaveType;
import gov.nysenate.seta.security.xsrf.XsrfValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This filter is responsible for setting attributes on the servlet request that are commonly used
 * by jsp templates.
 */
@Component("commonAttributeFilter")
public class CommonAttributeFilter implements Filter
{
    @Autowired
    private Environment env;

    private static final Logger logger = LoggerFactory.getLogger(CommonAttributeFilter.class);
    private static Set<String> runtimeLevels = new HashSet<>(Arrays.asList("dev", "test", "prod"));

    /** Attribute keys */
    public static String CONTEXT_PATH_ATTRIBUTE = "ctxPath";
    public static String RUNTIME_LEVEL_ATTRIBUTE = "runtimeLevel";
    public static String LOGIN_URL_ATTRIBUTE = "loginUrl";
    public static String MISC_LEAVE_ATTRIBUTE = "miscLeaves";

    @Value("${runtime.level}") private String runtimeLevel;
    @Value("${login.url}") private String loginUrl;

    @Autowired
    private XsrfValidator xsrfValidator;

    public CommonAttributeFilter() {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        logger.trace("CommonAttributeFilter processing url {}", httpServletRequest.getRequestURI());

        setContextPathAttribute(httpServletRequest);
        setRuntimeLevelAttribute(request);
        setLoginUrlAttribute(request);
        setXsrfTokenAttribute(httpServletRequest);
        setMiscLeaveAttribute(httpServletRequest);

        chain.doFilter(request, response);
    }

    private static void setContextPathAttribute(HttpServletRequest httpServletRequest) {
        httpServletRequest.setAttribute(CONTEXT_PATH_ATTRIBUTE, httpServletRequest.getContextPath());
    }

    private void setRuntimeLevelAttribute(ServletRequest request) {
        String level = "prod";
        if (runtimeLevel != null && runtimeLevels.contains(runtimeLevel.toLowerCase())) {
            level = runtimeLevel.toLowerCase();
        }
        else {
            logger.error("The runtime level string is empty! Assuming prod. Please ensure it is set in app.properties.");
        }
        request.setAttribute(RUNTIME_LEVEL_ATTRIBUTE, level);
    }

    private void setLoginUrlAttribute(ServletRequest request) {
        request.setAttribute(LOGIN_URL_ATTRIBUTE, loginUrl);
    }

    private void setXsrfTokenAttribute(HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession();
        xsrfValidator.saveXsrfToken(httpServletRequest, session);
    }

    private static void setMiscLeaveAttribute(HttpServletRequest request) {
        request.setAttribute(MISC_LEAVE_ATTRIBUTE, MiscLeaveType.getJsonLabels());
    }

    /** Life-cycle is maintained by Spring. The init method is not used. */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    /** Life-cycle is maintained by Spring. The destroy method is not used. */
    @Override
    public void destroy() {}
}
