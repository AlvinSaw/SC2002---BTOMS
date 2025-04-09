/**
 * This package contains the entity classes representing the core data models of the BTO Management System.
 */
package entity;

import enums.MaritalStatus;
import enums.UserType;

/**
 * HDBOfficer class represents an HDB Officer in the BTO Management System.
 */
public class HDBOfficer extends User {
    private BTOProject assignedProject;
    private boolean registrationApproved;

    /**
     * Constructor for HDBOfficer.
     * @param nric The NRIC of the officer
     * @param password The password of the officer
     * @param age The age of the officer
     * @param maritalStatus The marital status of the officer
     */
    public HDBOfficer(String nric, String password, int age, MaritalStatus maritalStatus) {
        super(nric, password, age, maritalStatus, UserType.HDB_OFFICER);
        this.assignedProject = null;
        this.registrationApproved = false;
    }

    /**
     * Retrieves the project assigned to the officer.
     * @return The assigned BTOProject
     */
    public BTOProject getAssignedProject() {
        return assignedProject;
    }

    /**
     * Sets the project assigned to the officer.
     * @param project The BTOProject to assign
     */
    public void setAssignedProject(BTOProject project) {
        this.assignedProject = project;
    }

    /**
     * Checks if the officer's registration is approved.
     * @return True if registration is approved, otherwise false
     */
    public boolean isRegistrationApproved() {
        return registrationApproved;
    }

    /**
     * Sets the registration approval status for the officer.
     * @param approved True to approve registration, otherwise false
     */
    public void setRegistrationApproved(boolean approved) {
        this.registrationApproved = approved;
    }

    /**
     * Checks if the officer can register for a specific project.
     * @param project The BTOProject to check
     * @return True if the officer can register, otherwise false
     */
    public boolean canRegisterForProject(BTOProject project) {
        if (assignedProject != null) {
            return false;
        }
        
        return !project.hasApplicant(getNric());
    }
}
