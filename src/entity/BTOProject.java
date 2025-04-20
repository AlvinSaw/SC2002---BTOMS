package entity;

import enums.FlatType;
import java.util.*;
import java.time.LocalDate;

public class BTOProject {
    private String projectName;
    private String neighborhood;
    private Map<FlatType, Integer> flatUnits;
    private Map<FlatType, Integer> remainingUnits;
    private LocalDate applicationOpenDate;
    private LocalDate applicationCloseDate;
    private HDBManager manager;
    private List<HDBOfficer> officers;
    private List<BTOApplication> applications;
    private List<Enquiry> enquiries;
    private boolean visible;
    private int maxOfficerSlots;
    
    public BTOProject(String projectName, String neighborhood, Map<FlatType, Integer> flatUnits,
                     LocalDate openDate, LocalDate closeDate, HDBManager manager) {
        this.projectName = projectName;
        this.neighborhood = neighborhood;
        this.flatUnits = new HashMap<>(flatUnits);
        this.remainingUnits = new HashMap<>(flatUnits);
        this.applicationOpenDate = openDate;
        this.applicationCloseDate = closeDate;
        this.manager = manager;
        this.officers = new ArrayList<>();
        this.applications = new ArrayList<>();
        this.enquiries = new ArrayList<>();
        this.visible = false;
        this.maxOfficerSlots = 10;
    }

    public String getProjectName() { return projectName; }
    public String getNeighborhood() { return neighborhood; }
    public Map<FlatType, Integer> getFlatUnits() { return new HashMap<>(flatUnits); }
    public Map<FlatType, Integer> getRemainingUnits() { return new HashMap<>(remainingUnits); }
    public LocalDate getApplicationOpenDate() { return applicationOpenDate; }
    public LocalDate getApplicationCloseDate() { return applicationCloseDate; }
    public HDBManager getManager() { return manager; }
    public List<HDBOfficer> getOfficers() { return new ArrayList<>(officers); }
    public List<BTOApplication> getApplications() { return new ArrayList<>(applications); }
    public List<Enquiry> getEnquiries() { return new ArrayList<>(enquiries); }
    public boolean isVisible() { return visible; }
    public int getMaxOfficerSlots() { return maxOfficerSlots; }
    public int getRemainingOfficerSlots() { return maxOfficerSlots - officers.size(); }

    public void setVisible(boolean visible) { this.visible = visible; }
    public void setMaxOfficerSlots(int maxOfficerSlots) { this.maxOfficerSlots = maxOfficerSlots; }
    public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }
    public void setFlatUnits(Map<FlatType, Integer> flatUnits) { 
        this.flatUnits = new HashMap<>(flatUnits);
        this.remainingUnits = new HashMap<>(flatUnits);
    }
    public void setApplicationOpenDate(LocalDate openDate) { this.applicationOpenDate = openDate; }
    public void setApplicationCloseDate(LocalDate closeDate) { this.applicationCloseDate = closeDate; }
    
    public boolean addOfficer(HDBOfficer officer) {
        if (officers.size() >= maxOfficerSlots) return false;
        if (officers.contains(officer)) return false;
        return officers.add(officer);
    }
    
    public void addApplication(BTOApplication application) {
        applications.add(application);
    }
    
    public void addEnquiry(Enquiry enquiry) {
        enquiries.add(enquiry);
    }
    
    public boolean hasApplicant(String nric) {
        for (BTOApplication app : applications) {
            if (app.getApplicant().getNric().equals(nric)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isApplicationPeriodOverlapping(BTOProject other) {
        return !(this.applicationCloseDate.isBefore(other.applicationOpenDate) ||
                this.applicationOpenDate.isAfter(other.applicationCloseDate));
    }
    
    public boolean updateRemainingUnits(FlatType flatType, int booked) {
        int current = remainingUnits.get(flatType);
        if (current >= booked) {
            remainingUnits.put(flatType, current - booked);
            return true;
        }
        return false;
    }
    
    public boolean isApplicationOpen() {
        LocalDate now = LocalDate.now();
        return !now.isBefore(applicationOpenDate) && !now.isAfter(applicationCloseDate);
    }
} 