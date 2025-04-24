package entity;

import enums.MaritalStatus;
import enums.UserType;
import enums.ApplicationStatus;
import java.util.List;
import java.util.ArrayList;
import control.ProjectManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HDBOfficer extends Applicant{
    private BTOProject assignedProject;
    private boolean registrationApproved;

    public HDBOfficer(String nric, String password, int age, MaritalStatus maritalStatus, String name) {
        super(nric, password, age, maritalStatus, name);
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

    public List<BTOApplication> getApplications() {
        return assignedProject != null ? assignedProject.getApplications() : new ArrayList<>();
    }

    public boolean canGenerateReceipt(BTOApplication application) {
        return application.getStatus() == ApplicationStatus.BOOKED && 
               !application.isWithdrawalRequested();
    }

    @Override
    public UserType getUserType() {
        return UserType.HDB_OFFICER;
    }
}