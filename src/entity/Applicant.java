package entity;

import enums.MaritalStatus;
import enums.UserType;
import enums.FlatType;
import enums.ApplicationStatus;
import entity.interfaces.IProjectViewable;
import entity.interfaces.IEnquiryManageable;
import control.*;
import java.util.*;
import java.time.LocalDate;

public class Applicant extends User implements IProjectViewable, IEnquiryManageable {
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

    @Override
    public List<BTOProject> getViewableProjects() {
        List<BTOProject> viewableProjects = new ArrayList<>();
        for (BTOProject project : ProjectManager.getInstance().getAllProjects()) {
            if (canViewProject(project)) {
                viewableProjects.add(project);
            }
        }
        return viewableProjects;
    }

    @Override
    public boolean canViewProject(BTOProject project) {
        // If already applied for this project, can view even if expired
        if (currentApplication != null && currentApplication.getProject().equals(project)) {
            return true;
        }
        
        // Check if project is within application period
        LocalDate now = LocalDate.now();
        return !now.isBefore(project.getApplicationOpenDate()) && 
               !now.isAfter(project.getApplicationCloseDate()) &&
               project.isVisible();
    }

    @Override
    public List<Enquiry> getEnquiries() {
        return new ArrayList<>(enquiries);
    }

    @Override
    public boolean canEditEnquiry(Enquiry enquiry) {
        return enquiry.getCreator().equals(this) && !enquiry.hasReply();
    }

    @Override
    public boolean canDeleteEnquiry(Enquiry enquiry) {
        return enquiry.getCreator().equals(this) && !enquiry.hasReply();
    }

    public void addEnquiry(Enquiry enquiry) {
        enquiries.add(enquiry);
    }

    public boolean canWithdrawApplication() {
        return currentApplication != null && 
               currentApplication.getStatus() != ApplicationStatus.UNSUCCESSFUL &&
               !currentApplication.isWithdrawalRequested();
    }

    public void requestWithdrawal() {
        if (canWithdrawApplication()) {
            currentApplication.requestWithdrawal();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Update the applicant's profile with the selected flat type
     * This method is called when an HDB Officer books a flat for the applicant
     * 
     * @param flatType The flat type selected for the applicant
     */
    public void updateSelectedFlatType(FlatType flatType) {
        // This method can be expanded to store the flat type in the applicant's profile
        // Currently just ensures the currentApplication has the correct flat type
        if (currentApplication != null) {
            currentApplication.setSelectedFlatType(flatType);
        }
    }
}