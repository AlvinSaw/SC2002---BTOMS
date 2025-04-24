package entity;

import enums.MaritalStatus;
import enums.UserType;
import enums.FlatType;
import enums.ApplicationStatus;
import control.*;
import java.util.*;
import java.time.LocalDate;

public class Applicant extends User{
    private BTOApplication currentApplication;
    private List<Enquiry> enquiries;
    private String name;

    public Applicant(String nric, String password, int age, MaritalStatus maritalStatus, String name) {
        super(nric, password, age, maritalStatus, UserType.APPLICANT, name);
        this.currentApplication = null;
        this.enquiries = new ArrayList<>();
        this.name = name;
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

    public List<Enquiry> getEnquiries() {
        return new ArrayList<>(enquiries);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}