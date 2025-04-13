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
        if (application.getStatus() != ApplicationStatus.BOOKED) {
            application.requestWithdrawal();
            saveApplications();
            return true;
        }
        return false;
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
}