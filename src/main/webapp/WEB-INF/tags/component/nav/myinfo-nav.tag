<%@tag description="Left navigation menu for Time & Attendance screens" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="ess-component-nav" tagdir="/WEB-INF/tags/component/nav" %>

<section class="left-nav" ess-navigation>
    <ess-component-nav:nav-header topicTitle="My Info Menu" colorClass="orange"/>
    <h3 class="main-topic">Personnel</h3>
    <ul class="sub-topic-list">
        <li class="sub-topic"><a href="${ctxPath}/myinfo/personnel/info">Current Info</a></li>
        <li class="sub-topic"><a href="${ctxPath}/myinfo/personnel/transactions">Employee Updates</a></li>
    </ul>
    <h3 class="main-topic">Payroll</h3>
    <ul class="sub-topic-list">
        <li class="sub-topic"><a href="${ctxPath}/myinfo/payroll/checkhistory">Pay Check History</a></li>
    </ul>
</section>