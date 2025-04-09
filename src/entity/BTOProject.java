/**
 * This package contains the entity classes representing the core data models of the BTO Management System.
 */
package entity;

import enums.FlatType;
import java.util.*;
import java.time.LocalDate;

/**
 * BTOProject class represents a Build-To-Order project in the system.
 */
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
    
    /**
     * Constructor for BTOProject.
     * @param projectName The name of the project
     * @param neighborhood The neighborhood of the project
     * @param flatUnits The map of flat types and their respective unit counts
     * @param openDate The application opening date
     * @param closeDate The application closing date
     * @param manager The HDBManager managing the project
     */
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

    /**
     * Retrieves the name of the project.
     * @return The project name
     */
    public String getProjectName() { return projectName; }

    /**
     * Retrieves the neighborhood of the project.
     * @return The neighborhood
     */
    public String getNeighborhood() { return neighborhood; }

    /**
     * Retrieves the map of flat types and their respective unit counts.
     * @return A map of FlatType to unit counts
     */
    public Map<FlatType, Integer> getFlatUnits() { return new HashMap<>(flatUnits); }

    /**
     * Retrieves the map of remaining flat units.
     * @return A map of FlatType to remaining unit counts
     */
    public Map<FlatType, Integer> getRemainingUnits() { return new HashMap<>(remainingUnits); }

    /**
     * Retrieves the application opening date.
     * @return The application opening date
     */
    public LocalDate getApplicationOpenDate() { return applicationOpenDate; }

    /**
     * Retrieves the application closing date.
     * @return The application closing date
     */
    public LocalDate getApplicationCloseDate() { return applicationCloseDate; }

    /**
     * Retrieves the manager of the project.
     * @return The HDBManager
     */
    public HDBManager getManager() { return manager; }

    /**
     * Retrieves the list of officers assigned to the project.
     * @return A list of HDBOfficers
     */
    public List<HDBOfficer> getOfficers() { return new ArrayList<>(officers); }

    /**
     * Retrieves the list of applications for the project.
     * @return A list of BTOApplications
     */
    public List<BTOApplication> getApplications() { return new ArrayList<>(applications); }

    /**
     * Retrieves the list of enquiries for the project.
     * @return A list of Enquiries
     */
    public List<Enquiry> getEnquiries() { return new ArrayList<>(enquiries); }

    /**
     * Checks if the project is visible.
     * @return True if the project is visible, otherwise false
     */
    public boolean isVisible() { return visible; }

    /**
     * Retrieves the maximum number of officer slots for the project.
     * @return The maximum officer slots
     */
    public int getMaxOfficerSlots() { return maxOfficerSlots; }

    /**
     * Retrieves the remaining officer slots for the project.
     * @return The remaining officer slots
     */
    public int getRemainingOfficerSlots() { return maxOfficerSlots - officers.size(); }

    /**
     * Sets the visibility of the project.
     * @param visible True to make the project visible, otherwise false
     */
    public void setVisible(boolean visible) { this.visible = visible; }

    /**
     * Adds an officer to the project.
     * @param officer The HDBOfficer to add
     * @return True if the officer is added successfully, otherwise false
     */
    public boolean addOfficer(HDBOfficer officer) {
        if (officers.size() >= maxOfficerSlots) return false;
        return officers.add(officer);
    }

    /**
     * Adds an application to the project.
     * @param application The BTOApplication to add
     */
    public void addApplication(BTOApplication application) {
        applications.add(application);
    }

    /**
     * Adds an enquiry to the project.
     * @param enquiry The Enquiry to add
     */
    public void addEnquiry(Enquiry enquiry) {
        enquiries.add(enquiry);
    }

    /**
     * Checks if the project has an applicant with the given NRIC.
     * @param nric The NRIC of the applicant
     * @return True if the applicant exists, otherwise false
     */
    public boolean hasApplicant(String nric) {
        for (BTOApplication app : applications) {
            if (app.getApplicant().getNric().equals(nric)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the application period overlaps with another project.
     * @param other The other BTOProject to check against
     * @return True if the periods overlap, otherwise false
     */
    public boolean isApplicationPeriodOverlapping(BTOProject other) {
        return !(this.applicationCloseDate.isBefore(other.applicationOpenDate) ||
                this.applicationOpenDate.isAfter(other.applicationCloseDate));
    }

    /**
     * Updates the remaining units for a specific flat type.
     * @param flatType The flat type to update
     * @param booked The number of units booked
     * @return True if the update is successful, otherwise false
     */
    public boolean updateRemainingUnits(FlatType flatType, int booked) {
        int current = remainingUnits.get(flatType);
        if (current >= booked) {
            remainingUnits.put(flatType, current - booked);
            return true;
        }
        return false;
    }

    /**
     * Checks if the application period is currently open.
     * @return True if the application period is open, otherwise false
     */
    public boolean isApplicationOpen() {
        LocalDate now = LocalDate.now();
        return !now.isBefore(applicationOpenDate) && !now.isAfter(applicationCloseDate);
    }
}
