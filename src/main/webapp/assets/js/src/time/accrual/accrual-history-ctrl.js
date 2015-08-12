var essTime = angular.module('essTime');

essTime.controller('AccrualHistoryCtrl',
    ['$scope', '$http', 'appProps', 'AccrualHistoryApi', 'EmpActiveYearsApi',
    function($scope, $http, appProps, AccrualHistoryApi, EmpActiveYearsApi) {

    $scope.state = {
        empId: appProps.user.employeeId,
        today: moment(),
        accSummaries: null,
        activeYears: [],
        selectedYear: null
    };

    $scope.getAccSummaries = function(year) {
        var fromDate = moment([year, 0, 1]);
        var toDate = moment([year + 1, 0, 1]).subtract(1, 'days');
        if (year == moment().year()) {
            //toDate = moment();
        }
        var accSummariesResp = AccrualHistoryApi.get({
            empId: $scope.state.empId,
            fromDate: fromDate.format('YYYY-MM-DD'),
            toDate: toDate.format('YYYY-MM-DD')
        }, function(resp) {
            if (resp.success) {
                $scope.state.accSummaries = resp.result;
                if (year == moment().year()) {
                    var currSummary = $scope.state.accSummaries.filter(function(accSum) {
                        return $scope.state.today.isBetween(accSum.payPeriod.startDate, accSum.payPeriod.endDate);
                    });
                    if (currSummary) {
                        currSummary[0].current = true;
                        console.log(currSummary);
                    }
                }

            }
            else {
                alert("Error while fetching accruals.");
            }
        });
    };

    $scope.getEmpActiveYears = function(callBack) {
        EmpActiveYearsApi.get({empId: $scope.state.empId}, function(resp) {
            $scope.state.activeYears = resp.activeYears.reverse();
            $scope.state.selectedYear = resp.activeYears[0];
            if (callBack) callBack();
        });
    };

    $scope.floatTheadOpts = {
        scrollingTop: 47,
        autoReflow: true
    };

    $scope.init = function() {
        $scope.getEmpActiveYears(function() {
            $scope.getAccSummaries($scope.state.selectedYear);
        });
    }();
}]);