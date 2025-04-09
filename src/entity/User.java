/**
 * This package contains the entity classes representing the core data models of the BTO Management System.
 */
package entity;

import enums.MaritalStatus;
import enums.UserType;

/**
 * User class represents a generic user in the BTO Management System.
 * It serves as a base class for specific user types like Applicant, HDBOfficer, and HDBManager.
 */
public abstract class User {
    private String nric;
    private String password;
    private int age;
    private MaritalStatus maritalStatus;
    private UserType userType;

    /**
     * Constructor for User.
     * @param nric The NRIC of the user
     * @param password The password of the user
     * @param age The age of the user
     * @param maritalStatus The marital status of the user
     * @param userType The type of the user
     */
    public User(String nric, String password, int age, MaritalStatus maritalStatus, UserType userType) {
        this.nric = nric;
        this.password = password;
        this.age = age;
        this.maritalStatus = maritalStatus;
        this.userType = userType;
    }

    /**
     * Retrieves the NRIC of the user.
     * @return The NRIC
     */
    public String getNric() {
        return nric;
    }

    /**
     * Sets the password for the user.
     * @param password The new password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Retrieves the password of the user.
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Retrieves the age of the user.
     * @return The age
     */
    public int getAge() {
        return age;
    }

    /**
     * Retrieves the marital status of the user.
     * @return The MaritalStatus
     */
    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    /**
     * Retrieves the type of the user.
     * @return The UserType
     */
    public UserType getUserType() {
        return userType;
    }

    /**
     * Validates the user's password.
     * @param inputPassword The password to validate
     * @return True if the password matches, otherwise false
     */
    public boolean validatePassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    /**
     * Validates the user's NRIC.
     * @return True if the NRIC is valid, otherwise false
     */
    public boolean validateNRIC() {
        if (nric == null || nric.length() != 9) return false;
        char first = nric.charAt(0);
        char last = nric.charAt(8);
        return (first == 'S' || first == 'T') && 
               Character.isLetter(last) &&
               nric.substring(1, 8).matches("\\d{7}");
    }
}
