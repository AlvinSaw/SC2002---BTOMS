package entity;

import enums.MaritalStatus;
import enums.UserType;
import enums.FlatType;
import java.io.*;

public class Applicant extends User {
    private BTOApplication currentApplication;

    public Applicant(String nric, String password, int age, MaritalStatus maritalStatus) {
        super(nric, password, age, maritalStatus, UserType.APPLICANT);
        this.currentApplication = null;
    }

    public BTOApplication getCurrentApplication() {
        return currentApplication;
    }

    public void setCurrentApplication(BTOApplication application) {
        this.currentApplication = application;
    }

    public boolean canApplyForFlatType(FlatType flatType) {
        if (getAge() < 21) return false;
        
        if (getMaritalStatus() == MaritalStatus.SINGLE) {
            return getAge() >= 35 && flatType == FlatType.TWO_ROOM;
        }
        
        return true;
    }
} 