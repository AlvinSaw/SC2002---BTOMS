package boundary;

import control.*;
import entity.*;
import enums.*;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class HDBManagerMenu {
    private Scanner scanner = new Scanner(System.in);
    private HDBManager manager;
    private ProjectManager projectManager;
    private ApplicationManager applicationManager;
    private EnquiryManager enquiryManager;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public HDBManagerMenu(HDBManager manager) {
        this.manager = manager;
        this.projectManager = ProjectManager.getInstance();
        this.applicationManager = ApplicationManager.getInstance();
        this.enquiryManager = EnquiryManager.getInstance();
    }

    public void show() {
        while (true) {
            System.out.println("\n=== HDB Manager Menu ===");
            System.out.println("1. Create New Project");
            System.out.println("2. View All Projects");
            System.out.println("3. View My Projects");
            System.out.println("4. View Project Applications");
            System.out.println("5. View Project Enquiries");
            System.out.println("6. Manage Officer Registrations");
            System.out.println("7. Generate Reports");
            System.out.println("8. Change Password");
            System.out.println("9. Logout");
            System.out.print("Choose an option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    createNewProject();
                    break;
                case 2:
                    viewAllProjects();
                    break;
                case 3:
                    viewMyProjects();
                    break;
                case 4:
                    viewProjectApplications();
                    break;
                case 5:
                    viewProjectEnquiries();
                    break;
                case 6:
                    manageOfficerRegistrations();
                    break;
                case 7:
                    generateReports();
                    break;
                case 8:
                    changePassword();
                    break;
                case 9:
                    UserManager.getInstance().logout();
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void createNewProject() {
        String name = null;
        while (name == null) {
            System.out.print("Enter project name: ");
            name = scanner.nextLine();
            if (name.length() < 2) {
                System.out.println("Project name must be at least 2 characters long. Please try again.");
                name = null;
                continue;
            }
            if (Character.isDigit(name.charAt(0))) {
                System.out.println("Project name cannot start with a number. Please try again.");
                name = null;
                continue;
            }
            if (projectManager.getProject(name) != null) {
                System.out.println("Project name already exists. Please choose a different name.");
                name = null;
            }
        }
        
        System.out.print("Enter neighborhood: ");
        String neighborhood = scanner.nextLine();
        
        Map<FlatType, Integer> flatUnits = new HashMap<>();
        for (FlatType type : FlatType.values()) {
            while (true) {
                try {
                    System.out.printf("Enter number of %s units: ", type.getDisplayName());
                    int units = scanner.nextInt();
                    scanner.nextLine();
                    if (units < 0) {
                        System.out.println("Number of units cannot be negative. Please try again.");
                        continue;
                    }
                    flatUnits.put(type, units);
                    break;
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.nextLine();
                }
            }
        }
        
        LocalDate now = LocalDate.now();
        LocalDate openDate = null;
        LocalDate closeDate = null;
        
        while (openDate == null) {
            System.out.print("Enter application open date (yyyy-MM-dd): ");
            try {
                openDate = LocalDate.parse(scanner.nextLine(), DATE_FORMAT);
                if (openDate.isBefore(now)) {
                    System.out.println("Open date cannot be before today.");
                    openDate = null;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd.");
            }
        }
        
        while (closeDate == null) {
            System.out.print("Enter application close date (yyyy-MM-dd): ");
            try {
                closeDate = LocalDate.parse(scanner.nextLine(), DATE_FORMAT);
                if (closeDate.isBefore(openDate)) {
                    System.out.println("Close date must be after open date.");
                    closeDate = null;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd.");
            }
        }

        int maxOfficerSlots = 0;
        while (maxOfficerSlots <= 0) {
            try {
                System.out.print("Enter maximum number of officers for this project: ");
                maxOfficerSlots = scanner.nextInt();
                scanner.nextLine();
                if (maxOfficerSlots <= 0) {
                    System.out.println("Number of officers must be greater than 0. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
            }
        }
        
        BTOProject project = new BTOProject(name, neighborhood, flatUnits, openDate, closeDate, manager, maxOfficerSlots);
        if (manager.canHandleNewProject(project)) {
            projectManager.addProject(project);
            manager.addCreatedProject(project);
            System.out.println("Project created successfully!");
        } else {
            System.out.println("Cannot create project: Application period overlaps with existing project.");
        }
    }

    private void viewAllProjects() {
        List<BTOProject> projects = projectManager.getAllProjects();
        if (projects.isEmpty()) {
            System.out.println("No projects available.");
            return;
        }

        System.out.println("\nAll Projects:");
        for (int i = 0; i < projects.size(); i++) {
            BTOProject project = projects.get(i);
            System.out.printf("%d. %s (%s)%n", i + 1, project.getProjectName(), project.getNeighborhood());
        }

        System.out.print("Enter project number to view details (0 to go back): ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice > 0 && choice <= projects.size()) {
            BTOProject selected = projects.get(choice - 1);
            viewProjectDetails(selected, false);
        }
    }

    private void viewMyProjects() {
        List<BTOProject> projects = manager.getManagedProjects();
        if (projects.isEmpty()) {
            System.out.println("You have no projects.");
            return;
        }

        System.out.println("\nMy Projects:");
        for (int i = 0; i < projects.size(); i++) {
            BTOProject project = projects.get(i);
            System.out.printf("%d. %s (%s)%n", i + 1, project.getProjectName(), project.getNeighborhood());
        }

        System.out.print("Enter project number to view details (0 to go back): ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice > 0 && choice <= projects.size()) {
            BTOProject selected = projects.get(choice - 1);
            viewProjectDetails(selected, true);
        }
    }

    private void viewProjectDetails(BTOProject project, boolean isOwnedProject) {
        System.out.println("\nProject Details:");
        System.out.println("Name: " + project.getProjectName());
        System.out.println("Neighborhood: " + project.getNeighborhood());
        System.out.println("Application Period: " + project.getApplicationOpenDate() + " to " + project.getApplicationCloseDate());
        System.out.println("Visibility: " + (project.isVisible() ? "Visible" : "Hidden"));
        System.out.println("Manager: " + project.getManager().getNric());
        System.out.println("\nFlat Units:");
        
        for (Map.Entry<FlatType, Integer> entry : project.getFlatUnits().entrySet()) {
            System.out.printf("%s: %d units (Remaining: %d)%n",
                entry.getKey().getDisplayName(),
                entry.getValue(),
                project.getRemainingUnits().get(entry.getKey()));
        }

        System.out.println("\nAssigned Officers:");
        List<HDBOfficer> officers = project.getOfficers();
        if (officers.isEmpty()) {
            System.out.println("No officers assigned.");
        } else {
            for (HDBOfficer officer : officers) {
                System.out.printf("NRIC: %s (Status: %s)%n",
                    officer.getNric(),
                    officer.isRegistrationApproved() ? "Approved" : "Pending");
            }
        }

        if (isOwnedProject) {
            System.out.println("\n1. Toggle Project Visibility");
            System.out.println("2. Edit Project Details");
            System.out.println("3. Go Back");
            System.out.print("Choose an option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    project.setVisible(!project.isVisible());
                    projectManager.saveProjects();
                    System.out.println("Project visibility toggled successfully!");
                    break;
                case 2:
                    editProjectDetails(project);
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        } else {
            System.out.println("\nPress Enter to go back...");
            scanner.nextLine();
        }
    }

    private void editProjectDetails(BTOProject project) {
        System.out.println("\nEdit Project Details:");
        System.out.println("1. Edit Neighborhood");
        System.out.println("2. Edit Flat Units");
        System.out.println("3. Edit Application Period");
        System.out.println("4. Go Back");
        System.out.print("Choose an option: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                System.out.print("Enter new neighborhood: ");
                String neighborhood = scanner.nextLine();
                project.setNeighborhood(neighborhood);
                projectManager.saveProjects();
                System.out.println("Neighborhood updated successfully!");
                break;
            case 2:
                editFlatUnits(project);
                break;
            case 3:
                editApplicationPeriod(project);
                break;
            case 4:
                return;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void editFlatUnits(BTOProject project) {
        System.out.println("\nEdit Flat Units:");
        Map<FlatType, Integer> flatUnits = new HashMap<>();
        for (FlatType type : FlatType.values()) {
            System.out.printf("Current %s units: %d%n", 
                type.getDisplayName(), 
                project.getFlatUnits().get(type));
            System.out.printf("Enter new number of %s units (0 to keep current): ", 
                type.getDisplayName());
            int units = scanner.nextInt();
            scanner.nextLine();
            flatUnits.put(type, units > 0 ? units : project.getFlatUnits().get(type));
        }
        
        project.setFlatUnits(flatUnits);
        projectManager.saveProjects();
        System.out.println("Flat units updated successfully!");
    }

    private void editApplicationPeriod(BTOProject project) {
        System.out.println("\nEdit Application Period:");
        LocalDate openDate = null;
        LocalDate closeDate = null;
        
        while (openDate == null) {
            System.out.print("Enter new application open date (yyyy-MM-dd): ");
            try {
                openDate = LocalDate.parse(scanner.nextLine(), DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd.");
            }
        }
        
        while (closeDate == null) {
            System.out.print("Enter new application close date (yyyy-MM-dd): ");
            try {
                closeDate = LocalDate.parse(scanner.nextLine(), DATE_FORMAT);
                if (closeDate.isBefore(openDate)) {
                    System.out.println("Close date must be after open date.");
                    closeDate = null;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd.");
            }
        }
        
        project.setApplicationOpenDate(openDate);
        project.setApplicationCloseDate(closeDate);
        projectManager.saveProjects();
        System.out.println("Application period updated successfully!");
    }

    private void viewProjectApplications() {
        List<BTOProject> projects = manager.getManagedProjects();
        if (projects.isEmpty()) {
            System.out.println("You have no projects.");
            return;
        }

        System.out.println("\nSelect Project:");
        for (int i = 0; i < projects.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, projects.get(i).getProjectName());
        }
        System.out.println("0. Go Back");

        System.out.print("Enter project number: ");
        int projectNum = scanner.nextInt();
        scanner.nextLine();

        if (projectNum == 0) {
            return;
        }

        if (projectNum < 1 || projectNum > projects.size()) {
            System.out.println("Invalid project number.");
            return;
        }

        BTOProject selected = projects.get(projectNum - 1);
        List<BTOApplication> applications = applicationManager.getApplicationsForProject(selected.getProjectName());
        
        if (applications.isEmpty()) {
            System.out.println("No applications for this project.");
            return;
        }

        System.out.println("\nApplications:");
        for (BTOApplication app : applications) {
            System.out.printf("NRIC: %s, Type: %s, Status: %s%s%n",
                app.getApplicant().getNric(),
                app.getSelectedFlatType().getDisplayName(),
                app.getStatus(),
                app.isWithdrawalRequested() ? " (Withdrawal Requested)" : "");
        }
        
        System.out.println("\nPress Enter to go back...");
        scanner.nextLine();
    }

    private void viewProjectEnquiries() {
        List<BTOProject> allProjects = projectManager.getAllProjects();
        if (allProjects.isEmpty()) {
            System.out.println("No projects available.");
            return;
        }

        System.out.println("\nSelect Project:");
        for (int i = 0; i < allProjects.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, allProjects.get(i).getProjectName());
        }
        System.out.println("0. Go Back");

        System.out.print("Enter project number: ");
        int projectNum = scanner.nextInt();
        scanner.nextLine();

        if (projectNum == 0) {
            return;
        }

        if (projectNum < 1 || projectNum > allProjects.size()) {
            System.out.println("Invalid project number.");
            return;
        }

        BTOProject selected = allProjects.get(projectNum - 1);
        List<Enquiry> enquiries = enquiryManager.getEnquiriesForProject(selected.getProjectName());
        
        if (enquiries.isEmpty()) {
            System.out.println("No enquiries for this project.");
            return;
        }

        System.out.println("\nEnquiries:");
        for (int i = 0; i < enquiries.size(); i++) {
            Enquiry enquiry = enquiries.get(i);
            System.out.printf("%d. From: %s%n   Content: %s%n   Reply: %s%n%n",
                i + 1,
                enquiry.getCreator().getNric(),
                enquiry.getContent(),
                enquiry.getReply() != null ? enquiry.getReply() : "No reply yet");
        }

        if (manager.getManagedProjects().contains(selected)) {
            System.out.print("Enter enquiry number to reply (0 to go back): ");
            int enquiryNum = scanner.nextInt();
            scanner.nextLine();

            if (enquiryNum > 0 && enquiryNum <= enquiries.size()) {
                Enquiry selectedEnquiry = enquiries.get(enquiryNum - 1);
                if (selectedEnquiry.hasReply()) {
                    System.out.println("This enquiry has already been replied to.");
                } else {
                    System.out.print("Enter your reply: ");
                    String reply = scanner.nextLine();
                    if (enquiryManager.replyToEnquiry(selectedEnquiry.getId(), reply, manager)) {
                        System.out.println("Reply submitted successfully!");
                    } else {
                        System.out.println("Failed to submit reply.");
                    }
                }
            }
        } else {
            System.out.println("\nPress Enter to go back...");
            scanner.nextLine();
        }
    }

    private void manageOfficerRegistrations() {
        List<BTOProject> projects = manager.getManagedProjects();
        if (projects.isEmpty()) {
            System.out.println("You have no projects.");
            return;
        }

        System.out.println("\nSelect Project:");
        for (int i = 0; i < projects.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, projects.get(i).getProjectName());
        }

        System.out.print("Enter project number: ");
        int projectNum = scanner.nextInt();
        scanner.nextLine();

        if (projectNum < 1 || projectNum > projects.size()) {
            System.out.println("Invalid project number.");
            return;
        }

        BTOProject selected = projects.get(projectNum - 1);
        List<HDBOfficer> officers = selected.getOfficers();
        List<HDBOfficer> pendingOfficers = new ArrayList<>();
        
        for (HDBOfficer officer : officers) {
            if (!officer.isRegistrationApproved()) {
                pendingOfficers.add(officer);
            }
        }

        if (pendingOfficers.isEmpty()) {
            System.out.println("No pending officer registrations.");
            return;
        }

        System.out.println("\nPending Registrations:");
        for (int i = 0; i < pendingOfficers.size(); i++) {
            HDBOfficer officer = pendingOfficers.get(i);
            System.out.printf("%d. NRIC: %s%n", i + 1, officer.getNric());
        }

        System.out.print("Enter officer number to approve/reject (0 to go back): ");
        int officerNum = scanner.nextInt();
        scanner.nextLine();

        if (officerNum > 0 && officerNum <= pendingOfficers.size()) {
            System.out.println("1. Approve");
            System.out.println("2. Reject");
            System.out.print("Choose an option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();

            HDBOfficer officer = pendingOfficers.get(officerNum - 1);
            if (choice == 1) {
                officer.setRegistrationApproved(true);
                projectManager.saveProjects();
                System.out.println("Officer registration approved!");
            } else if (choice == 2) {
                selected.getOfficers().remove(officer);
                officer.setAssignedProject(null);
                projectManager.saveProjects();
                System.out.println("Officer registration rejected!");
            }
        }
    }

    private void generateReports() {
        List<BTOProject> projects = manager.getManagedProjects();
        if (projects.isEmpty()) {
            System.out.println("You have no projects.");
            return;
        }

        System.out.println("\nSelect Project:");
        for (int i = 0; i < projects.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, projects.get(i).getProjectName());
        }

        System.out.print("Enter project number: ");
        int projectNum = scanner.nextInt();
        scanner.nextLine();

        if (projectNum < 1 || projectNum > projects.size()) {
            System.out.println("Invalid project number.");
            return;
        }

        BTOProject selected = projects.get(projectNum - 1);
        List<BTOApplication> applications = applicationManager.getApplicationsForProject(selected.getProjectName());

        System.out.println("\nSelect Report Type:");
        System.out.println("1. All Applications");
        System.out.println("2. Successful Applications");
        System.out.println("3. Booked Flats");
        System.out.println("4. Applications by Flat Type");
        System.out.println("5. Applications by Marital Status");
        System.out.print("Choose an option: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();

        System.out.println("\nReport:");
        switch (choice) {
            case 1:
                generateAllApplicationsReport(applications);
                break;
            case 2:
                generateSuccessfulApplicationsReport(applications);
                break;
            case 3:
                generateBookedFlatsReport(applications);
                break;
            case 4:
                generateApplicationsByFlatTypeReport(applications);
                break;
            case 5:
                generateApplicationsByMaritalStatusReport(applications);
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void generateAllApplicationsReport(List<BTOApplication> applications) {
        System.out.printf("Total Applications: %d%n%n", applications.size());
        for (BTOApplication app : applications) {
            printApplicationDetails(app);
        }
    }

    private void generateSuccessfulApplicationsReport(List<BTOApplication> applications) {
        List<BTOApplication> successful = new ArrayList<>();
        for (BTOApplication app : applications) {
            if (app.getStatus() == ApplicationStatus.SUCCESSFUL) {
                successful.add(app);
            }
        }
        
        System.out.printf("Total Successful Applications: %d%n%n", successful.size());
        for (BTOApplication app : successful) {
            printApplicationDetails(app);
        }
    }

    private void generateBookedFlatsReport(List<BTOApplication> applications) {
        List<BTOApplication> booked = new ArrayList<>();
        for (BTOApplication app : applications) {
            if (app.getStatus() == ApplicationStatus.BOOKED) {
                booked.add(app);
            }
        }
        
        System.out.printf("Total Booked Flats: %d%n%n", booked.size());
        for (BTOApplication app : booked) {
            printApplicationDetails(app);
        }
    }

    private void generateApplicationsByFlatTypeReport(List<BTOApplication> applications) {
        Map<FlatType, Integer> counts = new HashMap<>();
        for (BTOApplication app : applications) {
            counts.merge(app.getSelectedFlatType(), 1, Integer::sum);
        }
        
        System.out.println("Applications by Flat Type:");
        for (Map.Entry<FlatType, Integer> entry : counts.entrySet()) {
            System.out.printf("%s: %d applications%n", entry.getKey().getDisplayName(), entry.getValue());
        }
    }

    private void generateApplicationsByMaritalStatusReport(List<BTOApplication> applications) {
        Map<MaritalStatus, Integer> counts = new HashMap<>();
        for (BTOApplication app : applications) {
            counts.merge(app.getApplicant().getMaritalStatus(), 1, Integer::sum);
        }
        
        System.out.println("Applications by Marital Status:");
        for (Map.Entry<MaritalStatus, Integer> entry : counts.entrySet()) {
            System.out.printf("%s: %d applications%n", entry.getKey(), entry.getValue());
        }
    }

    private void printApplicationDetails(BTOApplication app) {
        System.out.printf("NRIC: %s%n", app.getApplicant().getNric());
        System.out.printf("Age: %d%n", app.getApplicant().getAge());
        System.out.printf("Marital Status: %s%n", app.getApplicant().getMaritalStatus());
        System.out.printf("Flat Type: %s%n", app.getSelectedFlatType().getDisplayName());
        System.out.printf("Status: %s%n", app.getStatus());
        System.out.printf("Application Date: %s%n", app.getApplicationDate());
        System.out.println();
    }

    private void changePassword() {
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