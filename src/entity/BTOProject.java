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
    private boolean autoPublish; // Added property for auto-publishing
    
    public BTOProject(String projectName, String neighborhood, Map<FlatType, Integer> flatUnits,
                     LocalDate openDate, LocalDate closeDate, HDBManager manager, int maxOfficerSlots, boolean autoPublish) {
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
        this.maxOfficerSlots = maxOfficerSlots;
        this.autoPublish = autoPublish;
    }
    
    // Maintain backward compatibility with existing code
    public BTOProject(String projectName, String neighborhood, Map<FlatType, Integer> flatUnits,
                     LocalDate openDate, LocalDate closeDate, HDBManager manager, int maxOfficerSlots) {
        this(projectName, neighborhood, flatUnits, openDate, closeDate, manager, maxOfficerSlots, false);
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
    public boolean isAutoPublish() { return autoPublish; }

    public void setVisible(boolean visible) { this.visible = visible; }
    public void setMaxOfficerSlots(int maxOfficerSlots) { this.maxOfficerSlots = maxOfficerSlots; }
    public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }
    public void setFlatUnits(Map<FlatType, Integer> flatUnits) { 
        this.flatUnits = new HashMap<>(flatUnits);
        this.remainingUnits = new HashMap<>(flatUnits);
    }
    public void setApplicationOpenDate(LocalDate openDate) { this.applicationOpenDate = openDate; }
    public void setApplicationCloseDate(LocalDate closeDate) { this.applicationCloseDate = closeDate; }
    public void setAutoPublish(boolean autoPublish) { this.autoPublish = autoPublish; }
    
    public boolean addOfficer(HDBOfficer officer) {
        if (officers.size() >= maxOfficerSlots) return false;
        if (officers.contains(officer)) return false;
        return officers.add(officer);
    }
    
    public void addApplication(BTOApplication application) {
        applications.add(application);
    }
    
    /**
     * Removes an application from this project's application list
     * @param application The application to remove
     * @return True if the application was successfully removed, false otherwise
     */
    public boolean removeApplication(BTOApplication application) {
        return applications.remove(application);
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
    
    /**
     * Sets the remaining units value for a specific flat type.
     * This is a helper method used by ProjectManager.
     * @param flatType The flat type to update
     * @param value The new remaining units value
     * @return True if the update was successful, false otherwise
     */
    public boolean setRemainingUnitValue(FlatType flatType, int value) {
        if (remainingUnits.containsKey(flatType) && flatUnits.containsKey(flatType)) {
            // Ensure value is not negative and doesn't exceed total units
            if (value >= 0 && value <= flatUnits.get(flatType)) {
                remainingUnits.put(flatType, value);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Sets the remaining units for all flat types.
     * This method allows for explicit control over all remaining unit values.
     * @param remainingUnits A map containing flat types and their remaining unit counts
     * @return True if the update was successful, false otherwise
     */
    public boolean setRemainingUnits(Map<FlatType, Integer> remainingUnits) {
        // Validate that the provided map contains all flat types from flatUnits
        if (remainingUnits == null || !this.flatUnits.keySet().equals(remainingUnits.keySet())) {
            return false;
        }
        
        // Validate that remaining units don't exceed total units for any flat type
        for (Map.Entry<FlatType, Integer> entry : remainingUnits.entrySet()) {
            FlatType type = entry.getKey();
            int remaining = entry.getValue();
            
            // Remaining units cannot be negative or exceed total units
            if (remaining < 0 || remaining > this.flatUnits.get(type)) {
                return false;
            }
        }
        
        // All validation passed, update the remainingUnits map
        this.remainingUnits = new HashMap<>(remainingUnits);
        
        // Removed call to ProjectManager.getInstance().saveProjects() to break circular dependency
        
        return true;
    }
    
    /**
     * @deprecated Use ProjectManager.updateRemainingUnits instead
     */
    @Deprecated
    public boolean updateRemainingUnits(FlatType flatType, int booked) {
        return control.ProjectManager.getInstance().updateRemainingUnits(this, flatType, booked);
    }
    
    public boolean isApplicationOpen() {
        LocalDate now = LocalDate.now();
        return !now.isBefore(applicationOpenDate) && !now.isAfter(applicationCloseDate);
    }
}