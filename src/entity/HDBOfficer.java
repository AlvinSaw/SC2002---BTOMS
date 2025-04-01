package entity;

import enums.MaritalStatus;
import enums.UserType;

public class HDBOfficer extends User {
    private BTOProject assignedProject;
    private boolean registrationApproved;

    public HDBOfficer(String nric, String password, int age, MaritalStatus maritalStatus) {
        super(nric, password, age, maritalStatus, UserType.HDB_OFFICER);
        this.assignedProject = null;
        this.registrationApproved = false;
    }

    public BTOProject getAssignedProject() {
        return assignedProject;
    }

    public void setAssignedProject(BTOProject project) {
        this.assignedProject = project;
    }

    public boolean isRegistrationApproved() {
        return registrationApproved;
    }

    public void setRegistrationApproved(boolean approved) {
        this.registrationApproved = approved;
    }

    public boolean canRegisterForProject(BTOProject project) {
        if (assignedProject != null) {
            return false;
        }
        
        return !project.hasApplicant(getNric());
    }
} 