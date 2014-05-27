<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="ess" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="ess-component" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="ess-component-nav" tagdir="/WEB-INF/tags/component/nav" %>
<%@ taglib prefix="ess-layout" tagdir="/WEB-INF/tags/layout" %>

<ess-layout:head>
    <jsp:attribute name="pageTitle">ESS - Time and Attendance</jsp:attribute>
    <jsp:body>
        <ess:ts-assets/>
        <script type="text/javascript" src="${ctxPath}/assets/js/src/nav/ess-nav.js"></script>
        <script type="text/javascript" src="${ctxPath}/assets/js/src/nav/ess-routes.js"></script>
        <script type="text/javascript" src="${ctxPath}/assets/js/src/time/record/record-entry-ctrl.js"></script>
        <script type="text/javascript" src="${ctxPath}/assets/js/src/time/record/record-directives.js"></script>
        <script type="text/javascript" src="${ctxPath}/assets/js/src/nav/home.js"></script>

        <!-- Common Directives -->
        <script type="text/javascript" src="${ctxPath}/assets/js/src/common/highcharts-directive.js"></script>
        <script type="text/javascript" src="${ctxPath}/assets/js/src/common/ess-notifications.js"></script>
        <script type="text/javascript" src="${ctxPath}/assets/js/src/common/odometer-directive.js"></script>
        <script type="text/javascript" src="${ctxPath}/assets/js/src/common/datepicker-directive.js"></script>

        <!-- Time Off Requests -->
        <script type="text/javascript" src="${ctxPath}/assets/js/src/time/timeoff/new-request-ctrl.js"></script>

        <!-- Pay Period Viewer -->
        <script type="text/javascript" src="${ctxPath}/assets/js/src/time/period/pay-period-view-ctrl.js"></script>

    </jsp:body>
</ess-layout:head>

<ess-layout:body>
    <jsp:body>
        <base href="/" />
        <ess-component:top-nav/>

        <section class="content-wrapper" ng-controller="MainCtrl as main">

            <ess-component-nav:nav-header topicTitle="Time And Attendance" pageTitle="Current page name"/>
            <ess-component-nav:time-nav/>

            <div class="view-animate-container">
                <div ng-view class="view-animate"></div>
            </div>

           <!-- <pre>$location.path() = {{$location.path()}}</pre>
            <pre>$route.current.templateUrl = {{$route.current.templateUrl}}</pre>
            <pre>$route.current.params = {{$route.current.params}}</pre>
            <pre>$route.current.scope.name = {{$route.current.scope.name}}</pre>
            <pre>$routeParams = {{$routeParams}}</pre>     -->

        </section>
    </jsp:body>
</ess-layout:body>