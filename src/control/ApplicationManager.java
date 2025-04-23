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
                
                // Load assigned flat type if available
                if (parts.length > 5 && !parts[5].isEmpty()) {
                    application.setAssignedFlatType(FlatType.valueOf(parts[5]));
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
                writer.println(String.format("%s|%s|%s|%s|%s|%s",
                    app.getApplicant().getNric(),
                    app.getProject().getProjectName(),
                    app.getSelectedFlatType(),
                    app.getStatus(),
                    app.isWithdrawalRequested(),
                    app.getAssignedFlatType() != null ? app.getAssignedFlatType() : ""));
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
        BTOApplication currentApplication = applicant.getCurrentApplication();
        
        // If applicant has a current application...
        if (currentApplication != null) {
            // Allow creating a new application if the current one is UNSUCCESSFUL or WITHDRAWN
            if (currentApplication.getStatus() == ApplicationStatus.UNSUCCESSFUL || 
                currentApplication.getStatus() == ApplicationStatus.WITHDRAWN) {
                // Remove the old application before creating a new one
                applications.remove(currentApplication);
                currentApplication.getProject().removeApplication(currentApplication);
                // Continue with creating a new application
            } else {
                // For other statuses (PENDING, SUCCESSFUL, BOOKED), don't allow new application
                return false;
            }
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
                // Use the correct flat type (assigned if available, otherwise selected)
                FlatType flatTypeToReturn = application.getAssignedFlatType() != null ? 
                    application.getAssignedFlatType() : application.getSelectedFlatType();
                
                // Print information about the unit being returned for debugging
                System.out.println("\nReturning a unit to the available pool:");
                System.out.println("Originally selected flat type: " + application.getSelectedFlatType().getDisplayName());
                System.out.println("Actually assigned flat type: " + 
                    (application.getAssignedFlatType() != null ? 
                    application.getAssignedFlatType().getDisplayName() : "None (using original)"));
                System.out.println("Returning flat type: " + flatTypeToReturn.getDisplayName());
                
                // Show remaining units before the update
                Map<FlatType, Integer> remainingBefore = application.getProject().getRemainingUnits();
                System.out.println("Remaining units before return: " + remainingBefore.get(flatTypeToReturn));
                
                // When a booked unit is returned, we need to decrease the booked count
                // Since updateRemainingUnits subtracts the booked parameter from remaining units,
                // we need to use a negative value to add units back
                application.getProject().updateRemainingUnits(flatTypeToReturn, -1);
                
                // Show remaining units after the update
                Map<FlatType, Integer> remainingAfter = application.getProject().getRemainingUnits();
                System.out.println("Remaining units after return: " + remainingAfter.get(flatTypeToReturn));
                System.out.println("Unit successfully returned to the pool.\n");
            }
            
            // Mark application as withdrawn instead of removing it
            application.setStatus(ApplicationStatus.WITHDRAWN);
            application.resetWithdrawalRequest(); // Clear the withdrawal request flag
            
            // The applicant's currentApplication remains set, but is now WITHDRAWN status
            // This allows them to see it as withdrawn, but they can still apply for a new project
            
            saveApplications();
            ProjectManager.getInstance().saveProjects(); // Ensure projects are saved with updated unit counts
            return true;
        }
        return false;
    }

    public boolean rejectWithdrawal(BTOApplication application) {
        if (application.isWithdrawalRequested()) {
            // Just reset the withdrawal request flag without removing the application
            application.resetWithdrawalRequest();
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

    public boolean bookFlatWithType(BTOApplication application, FlatType selectedFlatType) {
        if (application.getStatus() != ApplicationStatus.SUCCESSFUL) {
            return false;
        }
        
        BTOProject project = application.getProject();
        
        // Verify flat type is eligible for applicant
        Applicant applicant = application.getApplicant();
        if (!applicant.canApplyForFlatType(selectedFlatType)) {
            return false;
        }
        
        // Check if there are enough units of this flat type
        Map<FlatType, Integer> remainingUnits = project.getRemainingUnits();
        if (!remainingUnits.containsKey(selectedFlatType) || remainingUnits.get(selectedFlatType) <= 0) {
            return false;
        }
        
        // Set the assigned flat type instead of changing the original selected type
        application.setAssignedFlatType(selectedFlatType);
        
        // Debug information to verify assigned flat type is set
        System.out.println("\nFlat booking details:");
        System.out.println("Originally selected flat type: " + application.getSelectedFlatType());
        System.out.println("Assigned flat type: " + application.getAssignedFlatType());
        
        // Update remaining units count for the selected flat type
        project.updateRemainingUnits(selectedFlatType, 1);
        
        // Update application status to BOOKED
        application.setStatus(ApplicationStatus.BOOKED);
        
        // Save changes
        saveApplications();
        ProjectManager.getInstance().saveProjects();
        
        return true;
    }

    public String generateReceipt(BTOApplication application, HDBOfficer officer) {
        if (application.getStatus() != ApplicationStatus.BOOKED) {
            return "Can only generate receipts for booked applications.";
        }

        if (officer == null) {
            // If the applicant requests to generate a receipt
            StringBuilder receipt = new StringBuilder();
            receipt.append("=== HDB BTO APPLICATION RECEIPT ===\n");
            receipt.append("Date: ").append(LocalDateTime.now().format(DATE_FORMAT)).append("\n\n");
            
            receipt.append("APPLICANT DETAILS:\n");
            receipt.append("Name: ").append(application.getApplicant().getName()).append("\n");
            receipt.append("NRIC: ").append(application.getApplicant().getNric()).append("\n");
            receipt.append("Age: ").append(application.getApplicant().getAge()).append("\n");
            receipt.append("Marital Status: ").append(application.getApplicant().getMaritalStatus()).append("\n\n");
            
            receipt.append("PROJECT DETAILS:\n");
            receipt.append("Project: ").append(application.getProject().getProjectName()).append("\n");
            receipt.append("Neighborhood: ").append(application.getProject().getNeighborhood()).append("\n\n");
            
            receipt.append("FLAT DETAILS:\n");
            receipt.append("Originally Selected Flat Type: ").append(application.getSelectedFlatType().getDisplayName()).append("\n");
            
            // Use the assigned flat type for booked applications if available
            if (application.getAssignedFlatType() != null) {
                receipt.append("ASSIGNED FLAT TYPE: ").append(application.getAssignedFlatType().getDisplayName()).append("\n\n");
            } else {
                receipt.append("Assigned Flat Type: Same as selected\n\n");
            }
            
            receipt.append("BOOKING DETAILS:\n");
            receipt.append("Application Date: ").append(application.getApplicationDate().format(DATE_FORMAT)).append("\n");
            receipt.append("Status: ").append(application.getStatus()).append("\n");
            
            return receipt.toString();
        } else if (officer.canGenerateReceipt(application)) {
            // When an officer generates the receipt
            StringBuilder receipt = new StringBuilder();
            receipt.append("=== OFFICIAL HDB BTO BOOKING RECEIPT ===\n");
            receipt.append("Date: ").append(LocalDateTime.now().format(DATE_FORMAT)).append("\n");
            receipt.append("Officer: ").append(officer.getName()).append(" (").append(officer.getNric()).append(")\n\n");
            
            receipt.append("APPLICANT DETAILS:\n");
            receipt.append("Name: ").append(application.getApplicant().getName()).append("\n");
            receipt.append("NRIC: ").append(application.getApplicant().getNric()).append("\n");
            receipt.append("Age: ").append(application.getApplicant().getAge()).append("\n");
            receipt.append("Marital Status: ").append(application.getApplicant().getMaritalStatus()).append("\n\n");
            
            receipt.append("PROJECT DETAILS:\n");
            receipt.append("Project: ").append(application.getProject().getProjectName()).append("\n");
            receipt.append("Neighborhood: ").append(application.getProject().getNeighborhood()).append("\n\n");
            
            receipt.append("FLAT DETAILS:\n");
            receipt.append("Originally Selected Flat Type: ").append(application.getSelectedFlatType().getDisplayName()).append("\n");
            
            // Prominently display the assigned flat type with formatting
            if (application.getAssignedFlatType() != null) {
                receipt.append("ASSIGNED FLAT TYPE: ").append(application.getAssignedFlatType().getDisplayName()).append("\n\n");
            } else {
                receipt.append("Assigned Flat Type: Same as selected\n\n");
            }
            
            receipt.append("BOOKING DETAILS:\n");
            receipt.append("Application Date: ").append(application.getApplicationDate().format(DATE_FORMAT)).append("\n");
            receipt.append("Status: ").append(application.getStatus()).append("\n\n");
            
            receipt.append("This is an official receipt for flat booking in the BTO Management System.\n");
            receipt.append("=================================================\n");
            
            return receipt.toString();
        }
        return "Cannot generate receipt for this application.";
    }
}