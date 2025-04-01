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
    private boolean withdrawalRequested;

    public BTOApplication(Applicant applicant, BTOProject project, FlatType selectedFlatType) {
        this.applicant = applicant;
        this.project = project;
        this.status = ApplicationStatus.PENDING;
        this.applicationDate = LocalDateTime.now();
        this.selectedFlatType = selectedFlatType;
        this.withdrawalRequested = false;
    }

    public Applicant getApplicant() { return applicant; }
    public BTOProject getProject() { return project; }
    public ApplicationStatus getStatus() { return status; }
    public LocalDateTime getApplicationDate() { return applicationDate; }
    public FlatType getSelectedFlatType() { return selectedFlatType; }
    public boolean isWithdrawalRequested() { return withdrawalRequested; }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public void requestWithdrawal() {
        this.withdrawalRequested = true;
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
} 