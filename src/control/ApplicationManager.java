package control;

import entity.*;
import enums.*;
import java.util.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ApplicationManager {
    private static ApplicationManager instance;
    private List<BTOApplication> applications;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ApplicationManager() {
        applications = new ArrayList<>();
        loadApplications();
    }

    public static ApplicationManager getInstance() {
        if (instance == null) {
            instance = new ApplicationManager();
        }
        return instance;
    }

    private void loadApplications() {
        try (BufferedReader reader = new BufferedReader(new FileReader("database/applications.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                Applicant applicant = (Applicant) UserManager.getInstance().getUser(parts[0]);
                BTOProject project = ProjectManager.getInstance().getProject(parts[1]);
                FlatType flatType = FlatType.valueOf(parts[2]);
                
                BTOApplication application = new BTOApplication(applicant, project, flatType);
                application.setStatus(ApplicationStatus.valueOf(parts[3]));
                
                if (parts.length > 4 && Boolean.parseBoolean(parts[4])) {
                    application.requestWithdrawal();
                }
                
                applications.add(application);
                applicant.setCurrentApplication(application);
                project.addApplication(application);
            }
        } catch (IOException e) {
            System.err.println("Error loading applications: " + e.getMessage());
        }
    }

    public void saveApplications() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("database/applications.txt"))) {
            for (BTOApplication app : applications) {
                writer.println(String.format("%s|%s|%s|%s|%s",
                    app.getApplicant().getNric(),
                    app.getProject().getProjectName(),
                    app.getSelectedFlatType(),
                    app.getStatus(),
                    app.isWithdrawalRequested()));
            }
        } catch (IOException e) {
            System.err.println("Error saving applications: " + e.getMessage());
        }
    }

    public BTOApplication getApplication(String nric) {
        for (BTOApplication app : applications) {
            if (app.getApplicant().getNric().equals(nric)) {
                return app;
            }
        }
        return null;
    }

    public List<BTOApplication> getApplicationsForProject(String projectName) {
        List<BTOApplication> projectApplications = new ArrayList<>();
        for (BTOApplication app : applications) {
            if (app.getProject().getProjectName().equals(projectName)) {
                projectApplications.add(app);
            }
        }
        return projectApplications;
    }

    public boolean createApplication(Applicant applicant, BTOProject project, FlatType flatType) {
        if (applicant.getCurrentApplication() != null) {
            return false;
        }

        BTOApplication application = new BTOApplication(applicant, project, flatType);
        applications.add(application);
        applicant.setCurrentApplication(application);
        project.addApplication(application);
        saveApplications();
        return true;
    }

    public boolean updateApplicationStatus(BTOApplication application, ApplicationStatus newStatus) {
        application.setStatus(newStatus);
        saveApplications();
        return true;
    }

    public boolean requestWithdrawal(BTOApplication application) {
        application.requestWithdrawal();
        saveApplications();
        return true;
    }

    public boolean approveWithdrawal(BTOApplication application) {
        if (application.isWithdrawalRequested()) {
            if (application.getStatus() == ApplicationStatus.BOOKED) {
                application.getProject().updateRemainingUnits(application.getSelectedFlatType(), -1);
            }
            applications.remove(application);
            application.getApplicant().setCurrentApplication(null);
            saveApplications();
            return true;
        }
        return false;
    }

    public boolean bookFlat(BTOApplication application) {
        if (application.canBook() && application.book()) {
            saveApplications();
            return true;
        }
        return false;
    }

    public String generateReceipt(BTOApplication application, HDBOfficer officer) {
        if (application.getStatus() != ApplicationStatus.BOOKED) {
            return "Can only generate receipts for booked applications.";
        }

        if (officer == null) {
            // If the applicant requests to generate a receipt
            StringBuilder receipt = new StringBuilder();
            receipt.append("=== HDB BTO Application Receipt ===\n");
            receipt.append("Date: ").append(LocalDateTime.now().format(DATE_FORMAT)).append("\n\n");
            
            receipt.append("Applicant Details:\n");
            receipt.append("Name: ").append(application.getApplicant().getName()).append("\n");
            receipt.append("NRIC: ").append(application.getApplicant().getNric()).append("\n");
            receipt.append("Age: ").append(application.getApplicant().getAge()).append("\n");
            receipt.append("Marital Status: ").append(application.getApplicant().getMaritalStatus()).append("\n\n");
            
            receipt.append("Flat Details:\n");
            receipt.append("Project: ").append(application.getProject().getProjectName()).append("\n");
            receipt.append("Neighborhood: ").append(application.getProject().getNeighborhood()).append("\n");
            receipt.append("Flat Type: ").append(application.getSelectedFlatType().getDisplayName()).append("\n\n");
            
            receipt.append("Booking Details:\n");
            receipt.append("Application Date: ").append(application.getApplicationDate().format(DATE_FORMAT)).append("\n");
            receipt.append("Status: ").append(application.getStatus()).append("\n");
            
            return receipt.toString();
        } else if (officer.canGenerateReceipt(application)) {
            return officer.generateReceipt(application);
        }
        return "Cannot generate receipt for this application.";
    }
}