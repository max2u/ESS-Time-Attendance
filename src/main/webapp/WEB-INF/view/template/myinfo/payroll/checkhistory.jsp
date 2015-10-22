<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div ng-controller="EmpCheckHistoryCtrl">
  <div class="my-info-hero">
    <h2>Pay Check History</h2>
  </div>

  <div class="content-container content-controls">
    <p class="content-info">
      Filter By Year&nbsp;
      <select ng-model="checkHistory.year" ng-options="year for year in checkHistory.recordYears" ng-change="getRecords()"></select>
    </p>
  </div>

  <div loader-indicator ng-show="checkHistory.searching === true"></div>

  <div class="content-container" ng-show="paychecks.length > 0">
    <h1>{{checkHistory.year}} Paycheck Records</h1>
    <div class="padding-10 scroll-x">
      <table id="paycheck-history-table" class="ess-table" ng-model="paychecks">
        <thead>
        <tr>
          <th>Check Date</th>
          <th>Pay Period</th>
          <th>Gross</th>
          <th ng-repeat="(desc, value) in deductionSet">{{desc | formatDeductionHeader}}</th>
          <th>Direct Deposit</th>
          <th>Check</th>
        </tr>
        </thead>
        <tbody>
        <tr ng-repeat="paycheck in paychecks">
          <td>{{paycheck.checkDate | moment:'l'}}</td>
          <td>{{paycheck.payPeriod}}</td>
          <td>{{paycheck.grossIncome | currency}}</td>
          <td ng-repeat="(desc, value) in deductionSet">{{paycheck.deductions[desc].amount || 0 | currency}}</td>
          <td>{{paycheck.directDepositAmount | currency}}</td>
          <td>{{paycheck.checkAmount | currency}}</td>
        </tr>
        <tr class="yearly-totals">
          <td>Annual Totals</td>
          <td colspan="1"></td>
          <td>{{ytd.gross | currency}}</td>
          <td ng-repeat="(desc, value) in deductionSet">{{ytd[desc] || 0 | currency}}</td>
          <td>{{ytd.directDeposit | currency}}</td>
          <td>{{ytd.check | currency}}</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>

  <%-- No results notification --%>
  <div class="content-container" ng-show="checkHistory.searching === false && paychecks.length === 0">
    <h1>No pay checks found for {{checkHistory.year}}</h1>
  </div>
  <div modal-container></div>
</div>
