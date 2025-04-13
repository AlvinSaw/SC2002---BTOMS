package boundary;

import control.*;
import entity.*;
import enums.*;
import java.util.*;
import java.io.*;
import java.time.format.DateTimeFormatter;

public class ApplicantMenu {
    protected Scanner scanner = new Scanner(System.in);
    protected Applicant applicant;
    protected ProjectManager projectManager;
    protected ApplicationManager applicationManager;
    protected EnquiryManager enquiryManager;

    public ApplicantMenu(Applicant applicant) {
        this.applicant = applicant;
        this.projectManager = ProjectManager.getInstance();
        this.applicationManager = ApplicationManager.getInstance();
        this.enquiryManager = EnquiryManager.getInstance();
    }

    public void show() {
        while (true) {
            System.out.println("\n=== Applicant Menu ===");
            System.out.println("1. View Available Projects");
            System.out.println("2. View My Application");
            System.out.println("3. View My Enquiries");
            System.out.println("4. Create New Enquiry");
            System.out.println("5. Change Password");
            System.out.println("6. Logout");
            System.out.print("Choose an option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    viewAvailableProjects();
                    break;
                case 2:
                    viewMyApplication();
                    break;
                case 3:
                    viewMyEnquiries();
                    break;
                case 4:
                    createNewEnquiry();
                    break;
                case 5:
                    changePassword();
                    break;
                case 6:
                    UserManager.getInstance().logout();
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    protected void viewAvailableProjects() {
        List<BTOProject> projects = projectManager.getVisibleProjectsForUser(applicant);
        if (projects.isEmpty()) {
            System.out.println("No available projects found.");
            return;
        }

        System.out.println("\nAvailable Projects:");
        for (int i = 0; i < projects.size(); i++) {
            BTOProject project = projects.get(i);
            System.out.printf("%d. %s (%s)%n", i + 1, project.getProjectName(), project.getNeighborhood());
        }

        System.out.print("Enter project number to view details (0 to go back): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); 

        if (choice > 0 && choice <= projects.size()) {
            BTOProject selected = projects.get(choice - 1);
            viewProjectDetails(selected);
        }
    }

    protected void viewProjectDetails(BTOProject project) {
        System.out.println("\nProject Details:");
        System.out.println("Name: " + project.getProjectName());
        System.out.println("Neighborhood: " + project.getNeighborhood());
        System.out.println("Application Period: " + project.getApplicationOpenDate() + " to " + project.getApplicationCloseDate());
        System.out.println("\nAvailable Units:");
        
        Map<FlatType, Integer> remainingUnits = project.getRemainingUnits();
        for (Map.Entry<FlatType, Integer> entry : remainingUnits.entrySet()) {
            if (applicant.canApplyForFlatType(entry.getKey())) {
                System.out.printf("%s: %d units%n", entry.getKey().getDisplayName(), entry.getValue());
            }
        }

        if (applicant.getCurrentApplication() == null) {
            System.out.print("\nWould you like to apply for this project? (Y/N): ");
            String choice = scanner.nextLine();
            
            if (choice.equalsIgnoreCase("Y")) {
                applyForProject(project);
            }
        }
    }

    protected void applyForProject(BTOProject project) {
        System.out.println("\nSelect Flat Type:");
        List<FlatType> availableTypes = new ArrayList<>();
        
        for (FlatType type : project.getFlatUnits().keySet()) {
            if (applicant.canApplyForFlatType(type)) {
                availableTypes.add(type);
                System.out.printf("%d. %s%n", availableTypes.size(), type.getDisplayName());
            }
        }

        if (availableTypes.isEmpty()) {
            System.out.println("No flat types available for your eligibility.");
            return;
        }

        System.out.print("Enter your choice (0 to cancel): ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice > 0 && choice <= availableTypes.size()) {
            FlatType selectedType = availableTypes.get(choice - 1);
            if (applicationManager.createApplication(applicant, project, selectedType)) {
                System.out.println("Application submitted successfully!");
            } else {
                System.out.println("Failed to submit application.");
            }
        }
    }

    protected void viewMyApplication() {
        BTOApplication application = applicant.getCurrentApplication();
        if (application == null) {
            System.out.println("You have no active application.");
            System.out.println("You can apply for a project by selecting 'View Available Projects' from the main menu.");
            return;
        }

        // Define date formatter for better readability
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        String formattedDate = application.getApplicationDate().format(formatter);

        System.out.println("\nMy Application:");
        System.out.println("Project: " + application.getProject().getProjectName());
        System.out.println("Flat Type: " + application.getSelectedFlatType().getDisplayName());
        System.out.println("Status: " + application.getStatus());
        System.out.println("Application Date: " + formattedDate);
        
        if (application.isWithdrawalRequested()) {
            System.out.println("\nWithdrawal Request Status: PENDING");
            System.out.println("Your withdrawal request has been submitted and is awaiting approval.");
            System.out.println("Once approved, you will be able to apply for another project.");
            return;
        }

        if (!application.isWithdrawalRequested() && application.getStatus() != ApplicationStatus.BOOKED) {
            System.out.print("\nWould you like to request withdrawal? (Y/N): ");
            String choice = scanner.nextLine();
            
            if (choice.equalsIgnoreCase("Y")) {
                if (applicationManager.requestWithdrawal(application)) {
                    System.out.println("Withdrawal request submitted successfully!");
                    System.out.println("Once approved, you will be able to apply for another project.");
                } else {
                    System.out.println("Failed to submit withdrawal request.");
                }
            }
        }
    }

    protected void viewMyEnquiries() {
        List<Enquiry> enquiries = enquiryManager.getEnquiriesForUser(applicant.getNric());
        if (enquiries.isEmpty()) {
            System.out.println("You have no enquiries.");
            return;
        }

        System.out.println("\nMy Enquiries:");
        for (int i = 0; i < enquiries.size(); i++) {
            Enquiry enquiry = enquiries.get(i);
            System.out.printf("%d. Project: %s%n   Content: %s%n   Reply: %s%n",
                i + 1,
                enquiry.getProject().getProjectName(),
                enquiry.getContent(),
                enquiry.getReply() != null ? enquiry.getReply() : "No reply yet");
        }

        System.out.print("\nEnter enquiry number to edit/delete (0 to go back): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); 

        if (choice > 0 && choice <= enquiries.size()) {
            manageEnquiry(enquiries.get(choice - 1));
        }
    }

    private void manageEnquiry(Enquiry enquiry) {
        if (enquiry.hasReply()) {
            System.out.println("Cannot modify enquiry after it has been replied to.");
            return;
        }

        System.out.println("1. Edit");
        System.out.println("2. Delete");
        System.out.println("3. Go back");
        System.out.print("Choose an option: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine(); 

        switch (choice) {
            case 1:
                System.out.print("Enter new content: ");
                String newContent = scanner.nextLine();
                if (enquiryManager.updateEnquiry(enquiry.getId(), newContent, applicant)) {
                    System.out.println("Enquiry updated successfully!");
                } else {
                    System.out.println("Failed to update enquiry.");
                }
                break;
            case 2:
                if (enquiryManager.deleteEnquiry(enquiry.getId(), applicant)) {
                    System.out.println("Enquiry deleted successfully!");
                } else {
                    System.out.println("Failed to delete enquiry.");
                }
                break;
        }
    }

    protected void createNewEnquiry() {
        List<BTOProject> projects = projectManager.getVisibleProjects();
        if (projects.isEmpty()) {
            System.out.println("No projects available.");
            return;
        }

        System.out.println("\nSelect Project:");
        for (int i = 0; i < projects.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, projects.get(i).getProjectName());
        }

        System.out.print("Enter project number (0 to cancel): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); 

        if (choice > 0 && choice <= projects.size()) {
            System.out.print("Enter your enquiry: ");
            String content = scanner.nextLine();
            
            Enquiry enquiry = enquiryManager.createEnquiry(applicant, projects.get(choice - 1), content);
            if (enquiry != null) {
                System.out.println("Enquiry created successfully!");
            } else {
                System.out.println("Failed to create enquiry.");
            }
        }
    }

    protected void changePassword() {
        System.out.print("Enter current password: ");
        String oldPassword = scanner.nextLine();
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();
        
        if (UserManager.getInstance().changePassword(oldPassword, newPassword)) {
            System.out.println("Password changed successfully!");
        } else {
            System.out.println("Failed to change password.");
        }
    }
}