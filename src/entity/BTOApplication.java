/**
 * This package contains the entity classes representing the core data models of the BTO Management System.
 */
package entity;

import enums.ApplicationStatus;
import enums.FlatType;
import java.time.LocalDateTime;

/**
 * BTOApplication class represents an application for a BTO project in the system.
 */
public class BTOApplication {
    private Applicant applicant;
    private BTOProject project;
    private ApplicationStatus status;
    private LocalDateTime applicationDate;
    private FlatType selectedFlatType;
    private boolean withdrawalRequested;

    /**
     * Constructor for BTOApplication.
     * @param applicant The applicant submitting the application
     * @param project The BTO project being applied for
     * @param selectedFlatType The flat type selected in the application
     */
    public BTOApplication(Applicant applicant, BTOProject project, FlatType selectedFlatType) {
        this.applicant = applicant;
        this.project = project;
        this.status = ApplicationStatus.PENDING;
        this.applicationDate = LocalDateTime.now();
        this.selectedFlatType = selectedFlatType;
        this.withdrawalRequested = false;
    }

    /**
     * Retrieves the applicant associated with the application.
     * @return The Applicant
     */
    public Applicant getApplicant() { return applicant; }

    /**
     * Retrieves the project associated with the application.
     * @return The BTOProject
     */
    public BTOProject getProject() { return project; }

    /**
     * Retrieves the current status of the application.
     * @return The ApplicationStatus
     */
    public ApplicationStatus getStatus() { return status; }

    /**
     * Retrieves the date and time the application was submitted.
     * @return The application date and time
     */
    public LocalDateTime getApplicationDate() { return applicationDate; }

    /**
     * Retrieves the flat type selected in the application.
     * @return The selected FlatType
     */
    public FlatType getSelectedFlatType() { return selectedFlatType; }

    /**
     * Checks if a withdrawal has been requested for the application.
     * @return True if withdrawal is requested, otherwise false
     */
    public boolean isWithdrawalRequested() { return withdrawalRequested; }

    /**
     * Sets the status of the application.
     * @param status The new ApplicationStatus
     */
    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    /**
     * Requests withdrawal for the application.
     */
    public void requestWithdrawal() {
        this.withdrawalRequested = true;
    }

    /**
     * Checks if the application is eligible for flat booking.
     * @return True if the application can book a flat, otherwise false
     */
    public boolean canBook() {
        return status == ApplicationStatus.SUCCESSFUL && !withdrawalRequested;
    }

    /**
     * Books a flat for the application.
     * @return True if the flat is booked successfully, otherwise false
     */
    public boolean book() {
        if (!canBook()) return false;
        
        if (project.updateRemainingUnits(selectedFlatType, 1)) {
            status = ApplicationStatus.BOOKED;
            return true;
        }
        return false;
    }
}
