package entity;

import enums.MaritalStatus;
import enums.UserType;
import java.util.List;
import control.ProjectManager;

public class HDBOfficer extends Applicant {
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
} 