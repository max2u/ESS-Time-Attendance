var essApp = angular.module('ess');

essApp.directive('datepicker', [function(){
    return {
        restrict: 'AE',
        scope: {
            stepMonths: "@",    // Set to zero to disable month toggle.
            inline: "@",        // Set true if datepicker should be inline
            defaultDate: "@",   // Default Date to display
            beforeShowDay: "&"  // See http://api.jqueryui.com/datepicker/#option-beforeShowDay
        },
        link: function($scope, element, attrs) {

            var defaultDate = ($scope.defaultDate) ? $scope.defaultDate : new Date();

            element.datepicker({
                inline: $scope.inline || false,
                stepMonths: $scope.stepMonths || 1,
                defaultDate: $scope.defaultDate || 0,
                beforeShowDay: $scope.beforeShowDay()
            });

            if ($scope.stepMonths === 0) {
                element.find(".ui-datepicker-prev, .ui-datepicker-next").remove();
            }
        }
    }
}]);