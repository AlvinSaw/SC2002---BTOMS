package entity;

import enums.MaritalStatus;
import enums.UserType;
import enums.ApplicationStatus;
import entity.interfaces.IApplicationManageable;
import java.util.List;
import java.util.ArrayList;
import control.ProjectManager;

public class HDBOfficer extends Applicant implements IApplicationManageable {
    private BTOProject assignedProject;
    private boolean registrationApproved;

    public HDBOfficer(String nric, String password, int age, MaritalStatus maritalStatus) {
        super(nric, password, age, maritalStatus);
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
        
        if (project.hasApplicant(getNric())) {
            return false;
        }

        List<BTOProject> allProjects = ProjectManager.getInstance().getAllProjects();
        for (BTOProject p : allProjects) {
            if (p.getOfficers().contains(this) && p.isApplicationPeriodOverlapping(project)) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public List<BTOApplication> getApplications() {
        return assignedProject != null ? assignedProject.getApplications() : new ArrayList<>();
    }

    @Override
    public boolean canUpdateApplicationStatus(BTOApplication application, ApplicationStatus newStatus) {
        if (assignedProject == null || !application.getProject().equals(assignedProject)) {
            return false;
        }

        ApplicationStatus currentStatus = application.getStatus();
        
        // Validate status transition legality
        switch (currentStatus) {
            case PENDING:
                return newStatus == ApplicationStatus.SUCCESSFUL || 
                       newStatus == ApplicationStatus.UNSUCCESSFUL;
            case SUCCESSFUL:
                if (application.isWithdrawalRequested()) {
                    return newStatus == ApplicationStatus.UNSUCCESSFUL;
                }
                return newStatus == ApplicationStatus.BOOKED;
            case BOOKED:
                if (application.isWithdrawalRequested()) {
                    return newStatus == ApplicationStatus.UNSUCCESSFUL;
                }
                return false;
            default:
                return false;
        }
    }

    @Override
    public boolean canGenerateReceipt(BTOApplication application) {
        return application.getStatus() == ApplicationStatus.BOOKED && 
               !application.isWithdrawalRequested();
    }
} 