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
                StringBuilder flatUnitsStr = new StringBuilder();
                for (Map.Entry<FlatType, Integer> entry : project.getFlatUnits().entrySet()) {
                    if (flatUnitsStr.length() > 0) flatUnitsStr.append(",");
                    flatUnitsStr.append(entry.getKey()).append(":").append(entry.getValue());
                }
                
                StringBuilder officersStr = new StringBuilder();
                for (HDBOfficer officer : project.getOfficers()) {
                    if (officersStr.length() > 0) officersStr.append(",");
                    officersStr.append(officer.getNric()).append(":").append(officer.isRegistrationApproved());
                }
                
                writer.println(String.format("%s|%s|%s|%s|%s|%s|%b|%d|%s",
                    project.getProjectName(),
                    project.getNeighborhood(),
                    flatUnitsStr.toString(),
                    project.getApplicationOpenDate().format(DATE_FORMAT),
                    project.getApplicationCloseDate().format(DATE_FORMAT),
                    project.getManager().getNric(),
                    project.isVisible(),
                    project.getMaxOfficerSlots(),
                    officersStr.toString()));
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