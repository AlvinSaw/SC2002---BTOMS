package entity;

import enums.ApplicationStatus;
import enums.FlatType;
import java.time.LocalDateTime;

public class BTOApplication {
    private Applicant applicant;
    private BTOProject project;
    private ApplicationStatus status;
    private LocalDateTime applicationDate;
    private FlatType selectedFlatType;
    private FlatType assignedFlatType; // New property for the assigned flat type
    private boolean withdrawalRequested;

    public BTOApplication(Applicant applicant, BTOProject project, FlatType selectedFlatType) {
        this.applicant = applicant;
        this.project = project;
        this.status = ApplicationStatus.PENDING;
        this.applicationDate = LocalDateTime.now();
        this.selectedFlatType = selectedFlatType;
        this.assignedFlatType = null; // Initially null until assigned by officer
        this.withdrawalRequested = false;
    }

    public Applicant getApplicant() { return applicant; }
    public BTOProject getProject() { return project; }
    public ApplicationStatus getStatus() { return status; }
    public LocalDateTime getApplicationDate() { return applicationDate; }
    public FlatType getSelectedFlatType() { return selectedFlatType; }
    public FlatType getAssignedFlatType() { return assignedFlatType; } // Getter for assigned flat type
    public boolean isWithdrawalRequested() { return withdrawalRequested; }
    
    // Returns the actual flat type (assigned if available, otherwise selected)
    public FlatType getActualFlatType() {
        return assignedFlatType != null ? assignedFlatType : selectedFlatType;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public void requestWithdrawal() {
        this.withdrawalRequested = true;
    }

    public void resetWithdrawalRequest() {
        this.withdrawalRequested = false;
    }

    public boolean canBook() {
        return status == ApplicationStatus.SUCCESSFUL && !withdrawalRequested;
    }

    public boolean book() {
        if (!canBook()) return false;
        
        if (project.updateRemainingUnits(selectedFlatType, 1)) {
            status = ApplicationStatus.BOOKED;
            return true;
        }
        return false;
    }

    public void setSelectedFlatType(FlatType flatType) {
        this.selectedFlatType = flatType;
    }
    
    // New method to set the assigned flat type by officer
    public void setAssignedFlatType(FlatType flatType) {
        this.assignedFlatType = flatType;
    }

    public String getProjectName() {
        return project.getProjectName();
    }
}