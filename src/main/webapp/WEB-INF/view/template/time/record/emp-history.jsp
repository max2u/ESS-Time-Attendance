<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<section ng-controller="EmpRecordHistoryCtrl">

    <div class="content-container content-controls">
        <p class="content-info">View Attendance Records for Employee &nbsp;
            <select ng-model="state.selectedEmp" ng-if="state.allEmps.length > 0"
                    ng-init="state.selectedEmp = state.selectedEmp || state.allEmps[0]"
                    ng-change="getTimeRecordsForEmp(state.selectedEmp)"
                    ng-options="emp.dropDownLabel group by emp.group for emp in state.allEmps">
            </select>
        </p>
    </div>

    <section class="content-container">
        <h1>{{state.selectedEmp.empLastName}}'s Attendance Records</h1>
        <div class="content-controls">
            <p class="content-info" style="margin-bottom:0;">
                View attendance records for year &nbsp;
                <select ng-model="state.selectedRecYear"
                        ng-change="getTimeRecordForEmpByYear(state.selectedEmp, state.selectedRecYear)"
                        ng-options="year for year in state.recordYears">
                </select>
            </p>
        </div>
        <p class="content-info" style="">Time records that have been submitted for pay periods during {{state.selectedRecYear}}
            are listed in the table below.<br/>You can view details about each pay period by clicking the 'View Details' link to the right.</p>
        <div class="padding-10">
            <div loader-indicator ng-show="state.searching"></div>
            <table id="attendance-history-table" ng-show="!state.searching" class="ess-table attendance-listing-table">
                <thead>
                <tr>
                    <th>Date Range</th>
                    <th>Pay Period</th>
                    <th>Status</th>
                    <th>Work</th>
                    <th>Holiday</th>
                    <th>Vacation</th>
                    <th>Personal</th>
                    <th>Sick Emp</th>
                    <th>Sick Fam</th>
                    <th>Misc</th>
                    <th>Total</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                <tr ng-repeat="record in state.records">
                    <td>{{record.beginDate | moment:'l'}} - {{record.endDate | moment:'l'}}</td>
                    <td>{{record.payPeriod.payPeriodNum}}</td>
                    <td>{{record.recordStatus | timeRecordStatus}}</td>
                    <td>{{record.totals.workHours}}</td>
                    <td>{{record.totals.holidayHours}}</td>
                    <td>{{record.totals.vacationHours}}</td>
                    <td>{{record.totals.personalHours}}</td>
                    <td>{{record.totals.sickEmpHours}}</td>
                    <td>{{record.totals.sickFamHours}}</td>
                    <td>{{record.totals.miscHours}}</td>
                    <td>{{record.totals.total}}</td>
                    <td><a class="action-link" ng-click="showDetails(record)">View Details</a></td>
                </tr>
                </tbody>
            </table>
        </div>

        <div modal-container ng-show="top" ng-switch="top">
            <div record-detail-modal ng-if="isOpen('details')"></div>
        </div>
    </section>
</section>

