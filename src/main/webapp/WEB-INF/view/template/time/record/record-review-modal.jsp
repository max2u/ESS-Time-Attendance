<section id="record-review-content" class="content-container content-controls" title="Review and Approve Records">
  <div>
    <p class="content-info no-bottom-margin">
      Click a record from the Employee Record List on the left hand side to review the time record. You can then either
      Approve
      or Reject the record.
    </p>
    <hr/>
    <div id="record-selection-pane">
      <div class="pane-title">
        <span>Employee Record List</span>
      </div>
      <table id="record-selection-table" class="ess-table approve-attendance-rec-table"
             ng-model="records">
        <thead>
        <tr>
          <th>Employee</th>
          <th style="width:130px;">Pay Period</th>
          <th>Action</th>
        </tr>
        </thead>
        <tbody>
        <tr ng-repeat='record in records' ng-class="{'active': iSelectedRecord === $index,
                                                     'approved': getApprovalStatus(record) === 'approved',
                                                     'disapproved': getApprovalStatus(record) === 'disapproved'}"
            ng-click="selectRecord($index)" id="{{record.timeRecordId}}">
          <td class="name-column">
            {{record.employee.lastName}}
          </td>
          <td>{{record.beginDate | moment:'MM/DD'}} - {{record.endDate | moment:'MM/DD'}}</td>
          <td ng-switch="getApprovalStatus(record)" style="width: 10em">
            <span ng-switch-when="approved">Approve</span>
            <span ng-switch-when="disapproved">Disapprove</span>
            <span ng-switch-default>--</span>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
    <div id="record-details-view">
      <div record-details record="records[iSelectedRecord]"></div>
      <hr/>
      <div id="action-container" ng-if="allowApproval === true">
        <div ng-switch="getApprovalStatus(records[iSelectedRecord])" class="record-approval-buttons">
          <input type="button" value="Undo Approval" class="reject-button"
                 ng-switch-when="approved" ng-click="cancelRecord()"/>
          <input type="button" value="Undo Disapproval" class="neutral-button"
                 ng-switch-when="disapproved" ng-click="cancelRecord()"/>
          <input type="button" value="Approve Record" class="submit-button"
                 ng-switch-default ng-click="approveRecord()"/>
          <input type="button" value="Disapprove Record" class="reject-button"
                 ng-switch-default ng-click="rejectRecord()"/>

        </div>
        <div>
          <input type="button" class="submit-button" value="Submit Changes"
                 ng-click="submitChanges()" ng-disabled="submissionEmpty()"/>
          <input type="button" class="neutral-button" value="Cancel" ng-click="close()"/>
        </div>
      </div>
    </div>
  </div>
</section>
