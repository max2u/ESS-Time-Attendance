package gov.nysenate.seta.client.view;

import gov.nysenate.seta.client.view.base.ViewObject;
import gov.nysenate.seta.model.personnel.Employee;
import gov.nysenate.seta.model.personnel.SupervisorOverride;

import java.time.LocalDate;

public class SupervisorOverrideView implements ViewObject
{
    protected int supervisorId;
    protected boolean active;
    protected int overrideSupervisorId;
    protected EmployeeView overrideSupervisor;
    protected LocalDate startDate;
    protected LocalDate endDate;

    /** --- Constructors --- */

    public SupervisorOverrideView(SupervisorOverride ovr, Employee overrideSupervisor) {
        if (ovr != null) {
            this.supervisorId = ovr.getGranteeSupervisorId();
            this.overrideSupervisorId = ovr.getGranterSupervisorId();
            this.overrideSupervisor = new EmployeeView(overrideSupervisor);
            this.active = ovr.isActive();
            this.startDate = ovr.getStartDate().orElse(null);
            this.endDate = ovr.getEndDate().orElse(null);
        }
    }

    @Override
    public String getViewType() {
        return "supervisor override";
    }

    /** --- Basic Getters --- */

    public int getSupervisorId() {
        return supervisorId;
    }

    public boolean isActive() {
        return active;
    }

    public int getOverrideSupervisorId() {
        return overrideSupervisorId;
    }

    public EmployeeView getOverrideSupervisor() {
        return overrideSupervisor;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
