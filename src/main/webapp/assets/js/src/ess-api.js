var essApi = angular.module('essApi');

/** --- Pay Period API --- */

essApi.factory('PayPeriodApi', ['$resource', 'appProps', function($resource, appProps) {
    return $resource(appProps.apiPath + '/periods/:periodType');
}]);

/** --- Holiday API --- */

essApi.factory('HolidayApi', ['$resource', 'appProps', function($resource, appProps) {
    return $resource(appProps.apiPath + '/holidays');
}]);

/** --- Time Record API --- */

essApi.factory('TimeRecordsApi', ['$resource', 'appProps', function ($resource, appProps) {
    return $resource(appProps.apiPath + '/timerecords');
}]);

essApi.factory('ActiveTimeRecordsApi', ['$resource', 'appProps', function ($resource, appProps) {
    return $resource(appProps.apiPath + '/timerecords/active');
}]);

essApi.factory('ActiveYearsTimeRecordsApi', ['$resource', 'appProps', function ($resource, appProps) {
    return $resource(appProps.apiPath + '/timerecords/activeYears');
}]);

essApi.factory('SupervisorTimeRecordsApi', ['$resource', 'appProps', function ($resource, appProps) {
    return $resource(appProps.apiPath + '/timerecords/supervisor');
}]);

essApi.factory('SupervisorTimeRecordCountsApi', ['$resource', 'appProps', function ($resource, appProps) {
    return $resource(appProps.apiPath + '/timerecords/supervisor/count');
}]);

/** --- Supervisor API --- */

essApi.factory('SupervisorEmployeesApi', ['$resource', 'appProps', function ($resource, appProps) {
    return $resource(appProps.apiPath + '/supervisor/employees');
}]);

essApi.factory('SupervisorChainApi', ['$resource', 'appProps', function ($resource, appProps) {
    return $resource(appProps.apiPath + '/supervisor/chain');
}]);

essApi.factory('SupervisorOverridesApi', ['$resource', 'appProps', function ($resource, appProps) {
    return $resource(appProps.apiPath + '/supervisor/overrides');
}]);

essApi.factory('SupervisorGrantsApi', ['$resource', 'appProps', function ($resource, appProps) {
    return $resource(appProps.apiPath + '/supervisor/grants');
}]);


/** --- Accrual API --- */

essApi.factory('AccrualPeriodApi', ['$resource', 'appProps', function($resource, appProps) {
    return $resource(appProps.apiPath + '/accruals');
}]);

essApi.factory('AccrualHistoryApi', ['$resource', 'appProps', function($resource, appProps) {
    return $resource(appProps.apiPath + '/accruals/history');
}]);

/** --- Employee API --- */

essApi.factory('EmpInfoApi', ['$resource', 'appProps', function ($resource, appProps) {
    return $resource(appProps.apiPath + '/employees.json');
}]);

essApi.factory('EmpActiveYearsApi', ['$resource', 'appProps', function($resource, appProps) {
    return $resource(appProps.apiPath + '/employees/activeYears');
}]);

/** --- Transaction API --- */

essApi.factory('EmpTransactionsApi', ['$resource', 'appProps', function($resource, appProps) {
    return $resource(appProps.apiPath + '/empTransactions/')
}]);

essApi.factory('EmpTransactionSnapshotApi', ['$resource', 'appProps', function($resource, appProps) {
    return $resource(appProps.apiPath + '/empTransactions/snapshot')
}]);