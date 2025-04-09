/**
 * This package contains the entity classes representing the core data models of the BTO Management System.
 */
package entity;

import enums.MaritalStatus;
import enums.UserType;
import enums.FlatType;
import java.io.*;

/**
 * Applicant class represents an applicant in the BTO Management System.
 */
public class Applicant extends User {
    private BTOApplication currentApplication;

    /**
     * Constructor for Applicant.
     * @param nric The NRIC of the applicant
     * @param password The password of the applicant
     * @param age The age of the applicant
     * @param maritalStatus The marital status of the applicant
     */
    public Applicant(String nric, String password, int age, MaritalStatus maritalStatus) {
        super(nric, password, age, maritalStatus, UserType.APPLICANT);
        this.currentApplication = null;
    }

    /**
     * Retrieves the current application of the applicant.
     * @return The current BTOApplication
     */
    public BTOApplication getCurrentApplication() {
        return currentApplication;
    }

    /**
     * Sets the current application for the applicant.
     * @param application The BTOApplication to set
     */
    public void setCurrentApplication(BTOApplication application) {
        this.currentApplication = application;
    }

    /**
     * Checks if the applicant is eligible to apply for a specific flat type.
     * @param flatType The flat type to check eligibility for
     * @return True if the applicant is eligible, otherwise false
     */
    public boolean canApplyForFlatType(FlatType flatType) {
        if (getAge() < 21) return false;
        
        if (getMaritalStatus() == MaritalStatus.SINGLE) {
            return getAge() >= 35 && flatType == FlatType.TWO_ROOM;
        }
        
        return true;
    }
}
