package boundary;

import control.*;
import entity.*;
import enums.*;
import util.TablePrinter;
import java.util.*;
import java.time.LocalDateTime;
import java.util.InputMismatchException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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
            
            try {
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
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number (1-3).");
                scanner.nextLine(); // Clear invalid input
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                scanner.nextLine(); // Clear invalid input
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
            System.out.println("5. Generate Receipt");
            System.out.println("6. Change Password");
            System.out.println("7. Back to Main Menu");
            System.out.print("Choose an option: ");
            
            try {
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
                        super.generateReceipt();
                        break;
                    case 6:
                        changePassword();
                        break;
                    case 7:
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number (1-7).");
                scanner.nextLine(); // Clear invalid input
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                scanner.nextLine(); // Clear invalid input
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
            System.out.println("5. Generate Receipt");
            System.out.println("6. Change Password");
            System.out.println("7. Back to Main Menu");
            System.out.print("Choose an option: ");
            
            try {
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
                        generateReceipt();
                        break;
                    case 6:
                        changePassword();
                        break;
                    case 7:
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number (1-7).");
                scanner.nextLine(); // Clear invalid input
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                scanner.nextLine(); // Clear invalid input
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
        String[] headers = {"No.", "Project Name", "Neighborhood", "Available Slots"};
        String[][] data = new String[availableProjects.size()][4];
        
        for (int i = 0; i < availableProjects.size(); i++) {
            BTOProject project = availableProjects.get(i);
            data[i][0] = String.valueOf(i + 1);
            data[i][1] = project.getProjectName();
            data[i][2] = project.getNeighborhood();
            data[i][3] = String.valueOf(project.getRemainingOfficerSlots());
        }
        
        TablePrinter.printTable(headers, data);

        System.out.print("Enter project number to register (0 to go back): ");
        try {
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
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid project number.");
            scanner.nextLine(); // Clear invalid input
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            scanner.nextLine(); // Clear invalid input
        }
    }

    private void viewMyProject() {
        BTOProject project = officer.getAssignedProject();
        if (project == null) {
            System.out.println("You are not assigned to any project.");
            return;
        }

        System.out.println("\nMy Project Details:");
        
        // Project basic information table
        String[] basicHeaders = {"Property", "Value"};
        String[][] basicData = {
            {"Name", project.getProjectName()},
            {"Neighborhood", project.getNeighborhood()},
            {"Application Period", project.getApplicationOpenDate() + " to " + project.getApplicationCloseDate()},
            {"Registration Status", officer.isRegistrationApproved() ? "Approved" : "Pending"}
        };
        
        TablePrinter.printTable(basicHeaders, basicData);
        
        // Remaining units table
        System.out.println("\nRemaining Units:");
        String[] flatHeaders = {"Flat Type", "Remaining Units"};
        String[][] flatData = new String[project.getRemainingUnits().size()][2];
        
        int i = 0;
        for (Map.Entry<FlatType, Integer> entry : project.getRemainingUnits().entrySet()) {
            flatData[i][0] = entry.getKey().getDisplayName();
            flatData[i][1] = String.valueOf(entry.getValue());
            i++;
        }
        
        TablePrinter.printTable(flatHeaders, flatData);
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
        String[] headers = {"No.", "From", "Content", "Reply Status"};
        String[][] data = new String[enquiries.size()][4];
        
        for (int i = 0; i < enquiries.size(); i++) {
            Enquiry enquiry = enquiries.get(i);
            data[i][0] = String.valueOf(i + 1);
            data[i][1] = enquiry.getCreator().getName() + " (" + enquiry.getCreator().getNric() + ")";
            data[i][2] = enquiry.getContent();
            data[i][3] = enquiry.hasReply() ? "Replied: " + enquiry.getReply() : "No reply yet";
        }
        
        TablePrinter.printTable(headers, data);

        System.out.print("Enter enquiry number to reply (0 to go back): ");
        try {
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
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid enquiry number.");
            scanner.nextLine(); // Clear invalid input
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            scanner.nextLine(); // Clear invalid input
        }
    }

    private void viewAndProcessApplications() {
        BTOProject project = officer.getAssignedProject();
        if (project == null || !officer.isRegistrationApproved()) {
            System.out.println("You are not approved to handle any project yet.");
            return;
        }

        boolean continueProcessing = true;
        while (continueProcessing) {
            // Refresh the list of applications each time through the loop
            List<BTOApplication> applications = applicationManager.getApplicationsForProject(project.getProjectName());
            if (applications.isEmpty()) {
                System.out.println("No applications for this project.");
                return;
            }

            System.out.println("\nApplications:");
            String[] headers = {"No.", "Applicant", "NRIC", "Age", "Marital Status", "Flat Type", "Status", "Withdrawal"};
            String[][] data = new String[applications.size()][8];
            
            for (int i = 0; i < applications.size(); i++) {
                BTOApplication app = applications.get(i);
                Applicant applicant = app.getApplicant();
                data[i][0] = String.valueOf(i + 1);
                data[i][1] = applicant.getName();
                data[i][2] = applicant.getNric();
                data[i][3] = String.valueOf(applicant.getAge());
                data[i][4] = applicant.getMaritalStatus().toString();
                data[i][5] = app.getSelectedFlatType().getDisplayName();
                data[i][6] = app.getStatus().toString();
                data[i][7] = app.isWithdrawalRequested() ? "Yes" : "No";
            }
            
            TablePrinter.printTable(headers, data);

            System.out.println("\n1. Process Application Status");
            System.out.println("2. Process Withdrawal Request");
            System.out.println("3. Process Flat Booking");
            System.out.println("4. Go Back");
            System.out.print("Choose an option: ");
            
            try {
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
                        continueProcessing = false;
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number (1-4).");
                scanner.nextLine(); // Clear invalid input
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                scanner.nextLine(); // Clear invalid input
            }
        }
    }

    private void processApplicationStatus(List<BTOApplication> applications) {
        System.out.print("Enter application number: ");
        try {
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

            System.out.printf("Processing application for %s (NRIC: %s, Age: %d, Status: %s)%n", 
                application.getApplicant().getName(),
                application.getApplicant().getNric(),
                application.getApplicant().getAge(),
                application.getApplicant().getMaritalStatus());

            System.out.println("1. Mark as Successful");
            System.out.println("2. Mark as Unsuccessful");
            System.out.print("Choose an option: ");
            
            try {
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
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.nextLine(); // Clear invalid input
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid application number.");
            scanner.nextLine(); // Clear invalid input
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            scanner.nextLine(); // Clear invalid input
        }
    }

    private void processWithdrawalRequest(List<BTOApplication> applications) {
        System.out.print("Enter application number: ");
        try {
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

            System.out.printf("Processing withdrawal request for %s (NRIC: %s, Age: %d, Status: %s)%n", 
                application.getApplicant().getName(),
                application.getApplicant().getNric(),
                application.getApplicant().getAge(),
                application.getApplicant().getMaritalStatus());

            System.out.println("1. Approve Withdrawal");
            System.out.println("2. Reject Withdrawal");
            System.out.print("Choose an option: ");
            
            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); 

                if (choice == 1) {
                    if (applicationManager.approveWithdrawal(application)) {
                        System.out.println("Withdrawal request approved successfully!");
                        System.out.println("The applicant can now apply for another project.");
                    } else {
                        System.out.println("Failed to approve withdrawal request.");
                    }
                } else if (choice == 2) {
                    if (applicationManager.updateApplicationStatus(application, application.getStatus())) {
                        application.requestWithdrawal(); // Reset withdrawal request
                        System.out.println("Withdrawal request rejected.");
                    } else {
                        System.out.println("Failed to reject withdrawal request.");
                    }
                } else {
                    System.out.println("Invalid option.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.nextLine(); // Clear invalid input
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid application number.");
            scanner.nextLine(); // Clear invalid input
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            scanner.nextLine(); // Clear invalid input
        }
    }

    private void processFlatBooking(List<BTOApplication> applications) {
        System.out.print("Enter application number: ");
        try {
            int appNum = scanner.nextInt();
            scanner.nextLine(); 

            if (appNum < 1 || appNum > applications.size()) {
                System.out.println("Invalid application number.");
                return;
            }

            BTOApplication application = applications.get(appNum - 1);
            if (application.getStatus() != ApplicationStatus.SUCCESSFUL) {
                System.out.println("Only applications with 'SUCCESSFUL' status can proceed to flat booking.");
                return;
            }

            if (application.isWithdrawalRequested()) {
                System.out.println("This application has a pending withdrawal request. Cannot proceed with booking.");
                return;
            }

            Applicant applicant = application.getApplicant();
            BTOProject project = application.getProject();
            
            System.out.printf("Processing flat booking for %s (NRIC: %s)%n", 
                applicant.getName(), applicant.getNric());
            
            // Display available flat types with remaining units
            System.out.println("\nAvailable Flat Types:");
            String[] headers = {"No.", "Flat Type", "Remaining Units", "Eligible"};
            
            Map<FlatType, Integer> remainingUnits = project.getRemainingUnits();
            List<FlatType> availableFlatTypes = new ArrayList<>();
            
            // Filter flat types with remaining units > 0 that the applicant is eligible for
            for (Map.Entry<FlatType, Integer> entry : remainingUnits.entrySet()) {
                FlatType flatType = entry.getKey();
                int units = entry.getValue();
                
                if (units > 0 && applicant.canApplyForFlatType(flatType)) {
                    availableFlatTypes.add(flatType);
                }
            }
            
            if (availableFlatTypes.isEmpty()) {
                System.out.println("No eligible flat types with remaining units available.");
                return;
            }
            
            String[][] data = new String[availableFlatTypes.size()][4];
            for (int i = 0; i < availableFlatTypes.size(); i++) {
                FlatType flatType = availableFlatTypes.get(i);
                data[i][0] = String.valueOf(i + 1);
                data[i][1] = flatType.getDisplayName();
                data[i][2] = String.valueOf(remainingUnits.get(flatType));
                data[i][3] = "Yes";
            }
            
            TablePrinter.printTable(headers, data);
            
            // Let the officer select a flat type for the applicant
            System.out.print("\nSelect flat type number (0 to cancel): ");
            int flatTypeChoice = scanner.nextInt();
            scanner.nextLine();
            
            if (flatTypeChoice == 0) {
                System.out.println("Flat booking canceled.");
                return;
            }
            
            if (flatTypeChoice < 1 || flatTypeChoice > availableFlatTypes.size()) {
                System.out.println("Invalid flat type selection.");
                return;
            }
            
            FlatType selectedFlatType = availableFlatTypes.get(flatTypeChoice - 1);
            
            // Confirm the flat selection
            System.out.printf("You are about to book a %s for %s (NRIC: %s).%n", 
                selectedFlatType.getDisplayName(), applicant.getName(), applicant.getNric());
            System.out.print("Confirm booking? (Y/N): ");
            
            String confirmation = scanner.nextLine();
            if (!confirmation.equalsIgnoreCase("Y")) {
                System.out.println("Booking canceled.");
                return;
            }
            
            // Process the booking with the selected flat type
            if (applicationManager.bookFlatWithType(application, selectedFlatType)) {
                System.out.println("\nFlat booked successfully!");
                System.out.printf("A %s from %s has been allocated to %s.%n", 
                    selectedFlatType.getDisplayName(), 
                    project.getProjectName(), 
                    applicant.getName());
                
                // Display updated remaining units information
                System.out.println("\nRemaining Units after Booking:");
                String[] updatedHeaders = {"Flat Type", "Remaining Units"};
                String[][] updatedData = new String[project.getRemainingUnits().size()][2];
                
                int i = 0;
                for (Map.Entry<FlatType, Integer> entry : project.getRemainingUnits().entrySet()) {
                    updatedData[i][0] = entry.getKey().getDisplayName();
                    updatedData[i][1] = String.valueOf(entry.getValue());
                    i++;
                }
                
                TablePrinter.printTable(updatedHeaders, updatedData);
                
                // Generate and display receipt
                String receipt = applicationManager.generateReceipt(application, officer);
                System.out.println("\nBooking Receipt:");
                System.out.println(receipt);
                
                // Save to file
                String filename = String.format("output_applicant/receipt_%s_%s.txt", 
                    applicant.getNric(), 
                    project.getProjectName().replaceAll("\\s+", "_"));
                
                try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                    writer.print(receipt);
                    System.out.println("\nReceipt saved to " + filename);
                } catch (IOException e) {
                    System.err.println("Error saving receipt: " + e.getMessage());
                }
            } else {
                System.out.println("Failed to book flat. Please try again.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid number.");
            scanner.nextLine(); // Clear invalid input
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            scanner.nextLine(); // Clear invalid input
        }
    }

    @Override
    protected void generateReceipt() {
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

        System.out.println("\nApplications:");
        for (int i = 0; i < applications.size(); i++) {
            BTOApplication app = applications.get(i);
            if (app.getStatus() == ApplicationStatus.BOOKED) {
                Applicant applicant = app.getApplicant();
                System.out.printf("%d. Applicant: %s (NRIC: %s, Age: %d, Status: %s), Type: %s, Status: %s%n",
                    i + 1,
                    applicant.getName(),
                    applicant.getNric(),
                    applicant.getAge(),
                    applicant.getMaritalStatus(),
                    app.getSelectedFlatType().getDisplayName(),
                    app.getStatus());
            }
        }

        System.out.print("\nEnter application number to generate receipt (0 to go back): ");
        try {
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice > 0 && choice <= applications.size()) {
                BTOApplication selectedApp = applications.get(choice - 1);
                if (selectedApp.getStatus() == ApplicationStatus.BOOKED) {
                    String receipt = applicationManager.generateReceipt(selectedApp, officer);
                    if (receipt != null) {
                        System.out.println("\n=== Receipt ===");
                        System.out.println(receipt);
                    } else {
                        System.out.println("Failed to generate receipt.");
                    }
                } else {
                    System.out.println("Can only generate receipts for booked applications.");
                }
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid application number.");
            scanner.nextLine(); // Clear invalid input
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            scanner.nextLine(); // Clear invalid input
        }
    }
}