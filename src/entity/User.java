package entity;

import enums.MaritalStatus;
import enums.UserType;
import control.PasswordHasher;

public abstract class User {
    private String nric;
    private String password;
    private int age;
    private MaritalStatus maritalStatus;
    private UserType userType;
    private String name;

    public User(String nric, String password, int age, MaritalStatus maritalStatus, UserType userType, String name) {
        this.nric = nric;
        this.password = password.isEmpty() ? "" : PasswordHasher.hashPassword(password);
        this.age = age;
        this.maritalStatus = maritalStatus;
        this.userType = userType;
        this.name = name;
    }

    public String getNric() {
        return nric;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPassword(String password, boolean needHash) {
        this.password = needHash ? PasswordHasher.hashPassword(password) : password;
    }

    public String getPassword() {
        return password;
    }

    public int getAge() {
        return age;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public UserType getUserType() {
        return userType;
    }

    public boolean validatePassword(String inputPassword) {
        return PasswordHasher.verifyPassword(inputPassword, this.password);
    }

    public boolean validateNRIC() {
        if (nric == null || nric.length() != 9) return false;
        char first = nric.charAt(0);
        char last = nric.charAt(8);
        return (first == 'S' || first == 'T') && 
               Character.isLetter(last) &&
               nric.substring(1, 8).matches("\\d{7}");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
} 