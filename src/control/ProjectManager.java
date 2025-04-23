package control;

import entity.*;
import enums.*;
import java.util.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ProjectManager {
    private static ProjectManager instance;
    private List<BTOProject> projects;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ProjectManager() {
        projects = new ArrayList<>();
        loadProjects();
    }

    public static ProjectManager getInstance() {
        if (instance == null) {
            instance = new ProjectManager();
        }
        return instance;
    }

    private void loadProjects() {
        try (BufferedReader reader = new BufferedReader(new FileReader("database/projects.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                String projectName = parts[0];
                String neighborhood = parts[1];
                
                // Parse flat units
                Map<FlatType, Integer> flatUnits = new HashMap<>();
                String[] units = parts[2].split(",");
                for (String unit : units) {
                    String[] unitParts = unit.split(":");
                    flatUnits.put(FlatType.valueOf(unitParts[0]), Integer.parseInt(unitParts[1]));
                }
                
                LocalDate openDate = LocalDate.parse(parts[3], DATE_FORMAT);
                LocalDate closeDate = LocalDate.parse(parts[4], DATE_FORMAT);
                HDBManager manager = (HDBManager) UserManager.getInstance().getUser(parts[5]);
                
                BTOProject project = new BTOProject(projectName, neighborhood, flatUnits, openDate, closeDate, manager, Integer.parseInt(parts[7]));
                project.setVisible(Boolean.parseBoolean(parts[6]));
                
                // Set autoPublish property if it exists in the file
                if (parts.length > 9) {
                    project.setAutoPublish(Boolean.parseBoolean(parts[9]));
                }
                
                // Load officers
                if (parts.length > 8 && !parts[8].isEmpty()) {
                    String[] officerIds = parts[8].split(",");
                    for (String officerId : officerIds) {
                        String[] officerParts = officerId.split(":");
                        HDBOfficer officer = (HDBOfficer) UserManager.getInstance().getUser(officerParts[0]);
                        if (officer != null) {
                            project.addOfficer(officer);
                            officer.setAssignedProject(project);
                            if (officerParts.length > 1) {
                                officer.setRegistrationApproved(Boolean.parseBoolean(officerParts[1]));
                            }
                        }
                    }
                }
                
                // Load remaining units if present in the file
                if (parts.length > 10 && !parts[10].isEmpty()) {
                    Map<FlatType, Integer> remainingUnits = new HashMap<>();
                    String[] remainingUnitStrings = parts[10].split(",");
                    for (String unit : remainingUnitStrings) {
                        String[] unitParts = unit.split(":");
                        remainingUnits.put(FlatType.valueOf(unitParts[0]), Integer.parseInt(unitParts[1]));
                    }
                    // Set the remaining units in the project
                    project.setRemainingUnits(remainingUnits);
                }
                
                projects.add(project);
                manager.addCreatedProject(project);
            }
        } catch (IOException e) {
            System.err.println("Error loading projects: " + e.getMessage());
        }
    }

    public void saveProjects() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("database/projects.txt"))) {
            for (BTOProject project : projects) {
                // Build flat units string (total units)
                StringBuilder flatUnitsStr = new StringBuilder();
                for (Map.Entry<FlatType, Integer> entry : project.getFlatUnits().entrySet()) {
                    if (flatUnitsStr.length() > 0) flatUnitsStr.append(",");
                    flatUnitsStr.append(entry.getKey()).append(":").append(entry.getValue());
                }
                
                // Build remaining units string
                StringBuilder remainingUnitsStr = new StringBuilder();
                for (Map.Entry<FlatType, Integer> entry : project.getRemainingUnits().entrySet()) {
                    if (remainingUnitsStr.length() > 0) remainingUnitsStr.append(",");
                    remainingUnitsStr.append(entry.getKey()).append(":").append(entry.getValue());
                }
                
                // Build officers string
                StringBuilder officersStr = new StringBuilder();
                for (HDBOfficer officer : project.getOfficers()) {
                    if (officersStr.length() > 0) officersStr.append(",");
                    officersStr.append(officer.getNric()).append(":").append(officer.isRegistrationApproved());
                }
                
                writer.println(String.format("%s|%s|%s|%s|%s|%s|%b|%d|%s|%b|%s",
                    project.getProjectName(),
                    project.getNeighborhood(),
                    flatUnitsStr.toString(),
                    project.getApplicationOpenDate().format(DATE_FORMAT),
                    project.getApplicationCloseDate().format(DATE_FORMAT),
                    project.getManager().getNric(),
                    project.isVisible(),
                    project.getMaxOfficerSlots(),
                    officersStr.toString(),
                    project.isAutoPublish(),
                    remainingUnitsStr.toString())); // Added remaining units to the saved data
            }
        } catch (IOException e) {
            System.err.println("Error saving projects: " + e.getMessage());
        }
    }

    public List<BTOProject> getAllProjects() {
        return new ArrayList<>(projects);
    }

    public List<BTOProject> getVisibleProjects() {
        List<BTOProject> visibleProjects = new ArrayList<>();
        for (BTOProject project : projects) {
            if (project.isVisible()) {
                visibleProjects.add(project);
            }
        }
        return visibleProjects;
    }

    public List<BTOProject> getVisibleProjectsForUser(User user) {
        if (user instanceof Applicant || user instanceof HDBOfficer) {
            List<BTOProject> eligibleProjects = new ArrayList<>();
            List<BTOProject> visibleProjects = getVisibleProjects();
            Applicant applicant = (Applicant) user;
            
            for (BTOProject project : visibleProjects) {
                if (!project.isApplicationOpen()) {
                    continue;
                }
                
                boolean canApply = false;
                for (FlatType type : project.getFlatUnits().keySet()) {
                    if (applicant.canApplyForFlatType(type)) {
                        canApply = true;
                        break;
                    }
                }
                
                if (canApply) {
                    eligibleProjects.add(project);
                }
            }
            
            if (user instanceof HDBOfficer) {
                if (((HDBOfficer) user).getAssignedProject() != null) {
                    eligibleProjects.remove(((HDBOfficer) user).getAssignedProject());
                }
            }

            return eligibleProjects;
        }
        return getVisibleProjects();
    }

    public BTOProject getProject(String projectName) {
        for (BTOProject project : projects) {
            if (project.getProjectName().equals(projectName)) {
                return project;
            }
        }
        return null;
    }

    public void addProject(BTOProject project) {
        projects.add(project);
        saveProjects();
    }

    /**
     * Removes a project from the system by name
     * @param projectName The name of the project to remove
     * @return true if successful, false otherwise
     */
    public boolean deleteProject(String projectName) {
        BTOProject project = getProject(projectName);
        if (project != null) {
            return removeProject(project);
        }
        return false;
    }

    /**
     * Removes a project from the system using the project object
     * @param project The project to remove
     * @return true if successful, false otherwise
     */
    public boolean removeProject(BTOProject project) {
        if (project != null) {
            // Release all assigned officers
            for (HDBOfficer officer : project.getOfficers()) {
                officer.setAssignedProject(null);
                officer.setRegistrationApproved(false);
            }
            
            // Remove the project from the manager's list
            HDBManager manager = project.getManager();
            if (manager != null) {
                manager.removeCreatedProject(project);
            }
            
            // Remove from the project list
            projects.remove(project);
            saveProjects();
            return true;
        }
        return false;
    }

    /**
     * Automatically updates project visibility based on the current date.
     * Only sets projects to visible when their opening date is reached AND autoPublish is enabled.
     */
    public void autoPublishProjects() {
        LocalDate currentDate = LocalDate.now();
        boolean changesDetected = false;
        
        for (BTOProject project : projects) {
            // Check if project has auto-publish enabled AND is not visible AND today is on or after the opening date
            if (project.isAutoPublish() && !project.isVisible() && 
                (currentDate.isEqual(project.getApplicationOpenDate()) || 
                 currentDate.isAfter(project.getApplicationOpenDate()))) {
                
                // Set project to visible
                project.setVisible(true);
                System.out.println("Auto-publishing project: " + project.getProjectName());
                changesDetected = true;
            }
        }
        
        // Only save if changes were made
        if (changesDetected) {
            saveProjects();
        }
    }

    /**
     * Updates the remaining units for a specific flat type in a project.
     * @param project The project to update
     * @param flatType The flat type to update
     * @param booked The number of units to book (positive) or return (negative)
     * @return True if the update is successful, otherwise false
     */
    public boolean updateRemainingUnits(BTOProject project, FlatType flatType, int booked) {
        if (project == null || flatType == null) {
            return false;
        }
        
        Map<FlatType, Integer> remainingUnits = project.getRemainingUnits();
        if (remainingUnits.containsKey(flatType)) {
            int current = remainingUnits.get(flatType);
            // For booking: check if enough units
            // For returning (negative booked): always allow
            if (current >= booked || booked < 0) {
                // Calculate new value
                int newValue = current - booked;
                
                // Update the Map in the project - setRemainingUnitValue now returns success/failure
                boolean updateSuccess = project.setRemainingUnitValue(flatType, newValue);
                
                // Only save if update was successful
                if (updateSuccess) {
                    // Save changes to database
                    saveProjects();
                    return true;
                }
            }
        }
        return false;
    }
}