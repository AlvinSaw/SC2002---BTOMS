package boundary;

import control.*;
import entity.*;
import enums.*;
import java.util.*;
import java.time.LocalDateTime;

public class HDBOfficerMenu extends ApplicantMenu {
    private HDBOfficer officer;
    private ProjectManager projectManager;
    private ApplicationManager applicationManager;
    private EnquiryManager enquiryManager;

    public HDBOfficerMenu(HDBOfficer officer) {
        super(officer);
        this.officer = officer;
        this.projectManager = ProjectManager.getInstance();
        this.applicationManager = ApplicationManager.getInstance();
        this.enquiryManager = EnquiryManager.getInstance();
    }

    @Override
    public void show() {
        while (true) {
            System.out.println("\n=== HDB Officer Menu ===");
            System.out.println("1. Switch to Applicant Mode");
            System.out.println("2. Switch to Officer Mode");
            System.out.println("3. Logout");
            System.out.print("Choose an option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    showApplicantMenu();
                    break;
                case 2:
                    showOfficerMenu();
                    break;
                case 3:
                    UserManager.getInstance().logout();
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void showApplicantMenu() {
        while (true) {
            System.out.println("\n=== Applicant Mode ===");
            System.out.println("1. View Available Projects");
            System.out.println("2. View My Application");
            System.out.println("3. View My Enquiries");
            System.out.println("4. Create New Enquiry");
            System.out.println("5. Change Password");
            System.out.println("6. Back to Main Menu");
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
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void showOfficerMenu() {
        while (true) {
            System.out.println("\n=== Officer Mode ===");
            System.out.println("1. View Available Projects to Register");
            System.out.println("2. View My Project");
            System.out.println("3. View Project Enquiries");
            System.out.println("4. View and Process Applications");
            System.out.println("5. Change Password");
            System.out.println("6. Back to Main Menu");
            System.out.print("Choose an option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    viewProjectsRegister();
                    break;
                case 2:
                    viewMyProject();
                    break;
                case 3:
                    viewProjectEnquiries();
                    break;
                case 4:
                    viewAndProcessApplications();
                    break;
                case 5:
                    changePassword();
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void viewProjectsRegister() {
        List<BTOProject> projects = projectManager.getAllProjects();
        List<BTOProject> availableProjects = new ArrayList<>();
        
        for (BTOProject project : projects) {
            if (project.getRemainingOfficerSlots() > 0 && officer.canRegisterForProject(project)) {
                availableProjects.add(project);
            }
        }

        if (availableProjects.isEmpty()) {
            System.out.println("No available projects to register for.");
            return;
        }

        System.out.println("\nAvailable Projects:");
        for (int i = 0; i < availableProjects.size(); i++) {
            BTOProject project = availableProjects.get(i);
            System.out.printf("%d. %s (%s) - %d slots remaining%n",
                i + 1,
                project.getProjectName(),
                project.getNeighborhood(),
                project.getRemainingOfficerSlots());
        }

        System.out.print("Enter project number to register (0 to go back): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); 

        if (choice > 0 && choice <= availableProjects.size()) {
            BTOProject selected = availableProjects.get(choice - 1);
            if (selected.addOfficer(officer)) {
                officer.setAssignedProject(selected);
                projectManager.saveProjects();
                System.out.println("Registration submitted successfully! Awaiting manager approval.");
            } else {
                System.out.println("Failed to register for project.");
            }
        }
    }

    private void viewMyProject() {
        BTOProject project = officer.getAssignedProject();
        if (project == null) {
            System.out.println("You are not assigned to any project.");
            return;
        }

        System.out.println("\nMy Project Details:");
        System.out.println("Name: " + project.getProjectName());
        System.out.println("Neighborhood: " + project.getNeighborhood());
        System.out.println("Application Period: " + project.getApplicationOpenDate() + " to " + project.getApplicationCloseDate());
        System.out.println("Registration Status: " + (officer.isRegistrationApproved() ? "Approved" : "Pending"));
        System.out.println("\nRemaining Units:");
        
        for (Map.Entry<FlatType, Integer> entry : project.getRemainingUnits().entrySet()) {
            System.out.printf("%s: %d units%n", entry.getKey().getDisplayName(), entry.getValue());
        }
    }

    private void viewProjectEnquiries() {
        BTOProject project = officer.getAssignedProject();
        if (project == null || !officer.isRegistrationApproved()) {
            System.out.println("You are not approved to handle any project yet.");
            return;
        }

        List<Enquiry> enquiries = enquiryManager.getEnquiriesForProject(project.getProjectName());
        if (enquiries.isEmpty()) {
            System.out.println("No enquiries for this project.");
            return;
        }

        System.out.println("\nProject Enquiries:");
        for (int i = 0; i < enquiries.size(); i++) {
            Enquiry enquiry = enquiries.get(i);
            System.out.printf("%d. From: %s%n   Content: %s%n   Reply: %s%n",
                i + 1,
                enquiry.getCreator().getNric(),
                enquiry.getContent(),
                enquiry.getReply() != null ? enquiry.getReply() : "No reply yet");
        }

        System.out.print("Enter enquiry number to reply (0 to go back): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); 

        if (choice > 0 && choice <= enquiries.size()) {
            Enquiry selected = enquiries.get(choice - 1);
            if (!selected.hasReply()) {
                System.out.print("Enter your reply: ");
                String reply = scanner.nextLine();
                if (enquiryManager.addReply(selected.getId(), reply, officer)) {
                    System.out.println("Reply added successfully!");
                } else {
                    System.out.println("Failed to add reply.");
                }
            } else {
                System.out.println("This enquiry has already been replied to.");
            }
        }
    }

    private void viewAndProcessApplications() {
        BTOProject project = officer.getAssignedProject();
        if (project == null || !officer.isRegistrationApproved()) {
            System.out.println("You are not approved to handle any project yet.");
            return;
        }

        List<BTOApplication> applications = applicationManager.getApplicationsForProject(project.getProjectName());
        if (applications.isEmpty()) {
            System.out.println("No applications for this project.");
            return;
        }

        while (true) {
            System.out.println("\nApplications:");
            for (int i = 0; i < applications.size(); i++) {
                BTOApplication app = applications.get(i);
                System.out.printf("%d. NRIC: %s, Type: %s, Status: %s%s%n",
                    i + 1,
                    app.getApplicant().getNric(),
                    app.getSelectedFlatType().getDisplayName(),
                    app.getStatus(),
                    app.isWithdrawalRequested() ? " (Withdrawal Requested)" : "");
            }

            System.out.println("\n1. Process Application Status");
            System.out.println("2. Process Withdrawal Request");
            System.out.println("3. Process Flat Booking");
            System.out.println("4. Go Back");
            System.out.print("Choose an option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    processApplicationStatus(applications);
                    break;
                case 2:
                    processWithdrawalRequest(applications);
                    break;
                case 3:
                    processFlatBooking(applications);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void processApplicationStatus(List<BTOApplication> applications) {
        System.out.print("Enter application number: ");
        int appNum = scanner.nextInt();
        scanner.nextLine(); 

        if (appNum < 1 || appNum > applications.size()) {
            System.out.println("Invalid application number.");
            return;
        }

        BTOApplication application = applications.get(appNum - 1);
        if (application.getStatus() != ApplicationStatus.PENDING) {
            System.out.println("Can only process pending applications.");
            return;
        }

        System.out.println("1. Mark as Successful");
        System.out.println("2. Mark as Unsuccessful");
        System.out.print("Choose an option: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine(); 

        ApplicationStatus newStatus = null;
        switch (choice) {
            case 1:
                newStatus = ApplicationStatus.SUCCESSFUL;
                break;
            case 2:
                newStatus = ApplicationStatus.UNSUCCESSFUL;
                break;
            default:
                System.out.println("Invalid option.");
                return;
        }

        if (applicationManager.updateApplicationStatus(application, newStatus)) {
            System.out.println("Application status updated successfully!");
        } else {
            System.out.println("Failed to update application status.");
        }
    }

    private void processWithdrawalRequest(List<BTOApplication> applications) {
        System.out.print("Enter application number: ");
        int appNum = scanner.nextInt();
        scanner.nextLine(); 

        if (appNum < 1 || appNum > applications.size()) {
            System.out.println("Invalid application number.");
            return;
        }

        BTOApplication application = applications.get(appNum - 1);
        if (!application.isWithdrawalRequested()) {
            System.out.println("No withdrawal request for this application.");
            return;
        }

        System.out.println("1. Approve Withdrawal");
        System.out.println("2. Reject Withdrawal");
        System.out.print("Choose an option: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine(); 

        if (choice == 1) {
            if (applicationManager.approveWithdrawal(application)) {
                System.out.println("Withdrawal request approved successfully!");
            } else {
                System.out.println("Failed to approve withdrawal request.");
            }
        }
    }

    private void processFlatBooking(List<BTOApplication> applications) {
        System.out.print("Enter application number: ");
        int appNum = scanner.nextInt();
        scanner.nextLine(); 

        if (appNum < 1 || appNum > applications.size()) {
            System.out.println("Invalid application number.");
            return;
        }

        BTOApplication application = applications.get(appNum - 1);
        if (!application.canBook()) {
            System.out.println("This application is not eligible for flat booking.");
            return;
        }

        if (applicationManager.bookFlat(application)) {
            System.out.println("Flat booked successfully!");
            System.out.println("\nBooking Receipt:");
            System.out.println("Applicant NRIC: " + application.getApplicant().getNric());
            System.out.println("Project: " + application.getProject().getProjectName());
            System.out.println("Flat Type: " + application.getSelectedFlatType().getDisplayName());
            System.out.println("Booking Date: " + LocalDateTime.now());
        } else {
            System.out.println("Failed to book flat.");
        }
    }
} 