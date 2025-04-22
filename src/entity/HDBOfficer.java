package entity;

import enums.MaritalStatus;
import enums.UserType;
import enums.ApplicationStatus;
import entity.interfaces.IApplicationManageable;
import java.util.List;
import java.util.ArrayList;
import control.ProjectManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HDBOfficer extends Applicant implements IApplicationManageable {
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
    
    public String generateReceipt(BTOApplication application) {
        if (!canGenerateReceipt(application)) {
            return "Cannot generate receipt for this application.";
        }
        
        Applicant applicant = application.getApplicant();
        BTOProject project = application.getProject();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        StringBuilder receipt = new StringBuilder();
        receipt.append("==========================================\n");
        receipt.append("               FLAT BOOKING RECEIPT               \n");
        receipt.append("==========================================\n\n");
        
        receipt.append("Receipt Date: ").append(LocalDateTime.now().format(formatter)).append("\n\n");
        
        receipt.append("APPLICANT DETAILS:\n");
        receipt.append("-----------------\n");
        receipt.append("Name: ").append(applicant.getName()).append("\n");
        receipt.append("NRIC: ").append(applicant.getNric()).append("\n");
        receipt.append("Age: ").append(applicant.getAge()).append("\n");
        receipt.append("Marital Status: ").append(applicant.getMaritalStatus()).append("\n\n");
        
        receipt.append("FLAT DETAILS:\n");
        receipt.append("------------\n");
        receipt.append("Project Name: ").append(project.getProjectName()).append("\n");
        receipt.append("Neighborhood: ").append(project.getNeighborhood()).append("\n");
        receipt.append("Flat Type: ").append(application.getSelectedFlatType().getDisplayName()).append("\n\n");
        
        receipt.append("BOOKING DETAILS:\n");
        receipt.append("----------------\n");
        receipt.append("Application Date: ").append(application.getApplicationDate().format(formatter)).append("\n");
        receipt.append("Status: BOOKED\n\n");
        
        receipt.append("==========================================\n");
        receipt.append("This is an official receipt for your flat booking.\n");
        receipt.append("Please keep this receipt for your records.\n");
        receipt.append("==========================================\n");
        
        return receipt.toString();
    }

    @Override
    public UserType getUserType() {
        return UserType.HDB_OFFICER;
    }
} 