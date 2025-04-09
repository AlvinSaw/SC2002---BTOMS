/**
 * This package contains the control classes for managing the business logic of the BTO Management System.
 */
package control;

import entity.*;
import enums.*;
import java.util.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * ProjectManager class handles the management of BTO projects in the system.
 */
public class ProjectManager {
    private static ProjectManager instance;
    private List<BTOProject> projects;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ProjectManager() {
        projects = new ArrayList<>();
        loadProjects();
    }

    /**
     * Returns the singleton instance of ProjectManager.
     * @return The ProjectManager instance
     */
    public static ProjectManager getInstance() {
        if (instance == null) {
            instance = new ProjectManager();
        }
        return instance;
    }

    /**
     * Loads projects from the database file.
     */
    private void loadProjects() {
        try (BufferedReader reader = new BufferedReader(new FileReader("database/projects.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                String projectName = parts[0];
                String neighborhood = parts[1];
                Map<FlatType, Integer> flatUnits = new HashMap<>();
                String[] units = parts[2].split(",");
                for (String unit : units) {
                    String[] unitParts = unit.split(":");
                    flatUnits.put(FlatType.valueOf(unitParts[0]), Integer.parseInt(unitParts[1]));
                }
                LocalDate openDate = LocalDate.parse(parts[3], DATE_FORMAT);
                LocalDate closeDate = LocalDate.parse(parts[4], DATE_FORMAT);
                HDBManager manager = (HDBManager) UserManager.getInstance().getUser(parts[5]);
                
                BTOProject project = new BTOProject(projectName, neighborhood, flatUnits, openDate, closeDate, manager);
                project.setVisible(Boolean.parseBoolean(parts[6]));
                
                if (parts.length > 7 && !parts[7].isEmpty()) {
                    String[] officerIds = parts[7].split(",");
                    for (String officerId : officerIds) {
                        HDBOfficer officer = (HDBOfficer) UserManager.getInstance().getUser(officerId);
                        if (officer != null) {
                            project.addOfficer(officer);
                            officer.setAssignedProject(project);
                        }
                    }
                }
                
                projects.add(project);
                manager.addCreatedProject(project);
            }
        } catch (IOException e) {
            System.err.println("Error loading projects: " + e.getMessage());
        }
    }

    /**
     * Saves all projects to the database file.
     */
    public void saveProjects() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("database/projects.txt"))) {
            for (BTOProject project : projects) {
                StringBuilder sb = new StringBuilder();
                sb.append(project.getProjectName()).append("|")
                  .append(project.getNeighborhood()).append("|");
                
                StringJoiner unitJoiner = new StringJoiner(",");
                for (Map.Entry<FlatType, Integer> entry : project.getFlatUnits().entrySet()) {
                    unitJoiner.add(entry.getKey() + ":" + entry.getValue());
                }
                sb.append(unitJoiner.toString()).append("|")
                  .append(project.getApplicationOpenDate().format(DATE_FORMAT)).append("|")
                  .append(project.getApplicationCloseDate().format(DATE_FORMAT)).append("|")
                  .append(project.getManager().getNric()).append("|")
                  .append(project.isVisible()).append("|");
                
                StringJoiner officerJoiner = new StringJoiner(",");
                for (HDBOfficer officer : project.getOfficers()) {
                    officerJoiner.add(officer.getNric());
                }
                sb.append(officerJoiner.toString());
                
                writer.println(sb.toString());
            }
        } catch (IOException e) {
            System.err.println("Error saving projects: " + e.getMessage());
        }
    }

    /**
     * Retrieves all projects in the system.
     * @return A list of all BTOProjects
     */
    public List<BTOProject> getAllProjects() {
        return new ArrayList<>(projects);
    }

    /**
     * Retrieves all visible projects in the system.
     * @return A list of visible BTOProjects
     */
    public List<BTOProject> getVisibleProjects() {
        List<BTOProject> visibleProjects = new ArrayList<>();
        for (BTOProject project : projects) {
            if (project.isVisible()) {
                visibleProjects.add(project);
            }
        }
        return visibleProjects;
    }

    /**
     * Retrieves visible projects eligible for a specific user.
     * @param user The user to filter projects for
     * @return A list of visible BTOProjects eligible for the user
     */
    public List<BTOProject> getVisibleProjectsForUser(User user) {
        if (user instanceof Applicant) {
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
            return eligibleProjects;
        }
        return getVisibleProjects();
    }

    /**
     * Retrieves a project by its name.
     * @param projectName The name of the project
     * @return The BTOProject if found, otherwise null
     */
    public BTOProject getProject(String projectName) {
        for (BTOProject project : projects) {
            if (project.getProjectName().equals(projectName)) {
                return project;
            }
        }
        return null;
    }

    /**
     * Adds a new project to the system.
     * @param project The BTOProject to add
     */
    public void addProject(BTOProject project) {
        projects.add(project);
        saveProjects();
    }

    /**
     * Deletes a project from the system.
     * @param projectName The name of the project to delete
     * @return True if the project is deleted successfully
     */
    public boolean deleteProject(String projectName) {
        BTOProject project = getProject(projectName);
        if (project != null && project.getApplications().isEmpty()) {
            projects.remove(project);
            saveProjects();
            return true;
        }
        return false;
    }
}
