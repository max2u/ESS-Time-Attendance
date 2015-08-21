<%@tag description="Top navigation menu" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ attribute name="activeTopic" required="true" description="A key indicating which nav topic should be active." %>

<header class="ess-top-header">
    <div class="fixed-width-header">
        <div class="left-header-area">
            <div class="nysslogo"></div>
            <div class="nysslogo-text-container">
                <div class="nysslogo-text">
                    <span class="highlight">NYSS </span>
                    <span>ESS</span>
                </div>
            </div>
            <ul class="top-nav-list">
                <li id="dashboardLink" class="main-topic orange <c:if test='${activeTopic == "myinfo"}'>active</c:if>">
                    <a target="_self" href="${ctxPath}/myinfo"><img class="nav-icon" src="${ctxPath}/assets/img/user.png"/>My Info</a>
                </li>
                <li id="timeAttendanceLink" class="main-topic teal <c:if test='${activeTopic == "time"}'>active</c:if>">
                    <a target="_self" href="${ctxPath}/time"><img class="nav-icon" src="${ctxPath}/assets/img/20px-ffffff/clock.png"/>Time</a>
                </li>
                <li id="helpLink" class="main-topic">
                    <a target="_self"><img class="nav-icon" src="${ctxPath}/assets/img/20px-ffffff/question.png"/>Help</a>
                </li>
            </ul>
        </div>
        <div class="right-header-area">
            <c:if test="${runtimeLevel != 'prod'}">
                <div class="header-label-segment dark-red">
                    <span class="dark-red">Running in ${runtimeLevel} mode.</span>
                </div>
            </c:if>
            <div class="header-label-segment">Hi, ${principal.getFullName()}</div>
            <div id="logoutSection">
                <a target="_self" href="${ctxPath}/logout">Sign Out</a>
            </div>
        </div>
    </div>
</header>