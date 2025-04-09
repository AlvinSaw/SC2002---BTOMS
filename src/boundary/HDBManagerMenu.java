/**
 * This package contains the boundary classes for the BTO Management System.
 */
package boundary;

import control.*;
import entity.*;
import enums.*;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * HDBManagerMenu class provides the user interface for HDB Managers in the BTO Management System.
 */
public class HDBManagerMenu {
    private Scanner scanner = new Scanner(System.in);
    private HDBManager manager;
    private ProjectManager projectManager;
    private ApplicationManager applicationManager;
    private EnquiryManager enquiryManager;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Constructor for HDBManagerMenu.
     * Initializes the menu with the given manager and managers.
     * @param manager The HDB Manager using the menu
     */
    public HDBManagerMenu(HDBManager manager) {
        this.manager = manager;
        this.projectManager = ProjectManager.getInstance();
        this.applicationManager = ApplicationManager.getInstance();
        this.enquiryManager = EnquiryManager.getInstance();
    }

    /**
     * Displays the HDB Manager menu and handles user interactions.
     */
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

    /**
     * Allows the HDB Manager to create a new BTO project.
     */
    private void createNewProject() {
        System.out.print("Enter project name: ");
        String projectName = scanner.nextLine();
        
        System.out.print("Enter neighborhood: ");
        String neighborhood = scanner.nextLine();
        
        Map<FlatType, Integer> flatUnits = new HashMap<>();
        for (FlatType type : FlatType.values()) {
            System.out.printf("Enter number of %s units: ", type.getDisplayName());
            int units = scanner.nextInt();
            flatUnits.put(type, units);
        }
        scanner.nextLine();
        
        LocalDate openDate = null;
        while (openDate == null) {
            System.out.print("Enter application opening date (yyyy-MM-dd): ");
            try {
                openDate = LocalDate.parse(scanner.nextLine(), DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd.");
            }
        }
        
        LocalDate closeDate = null;
        while (closeDate == null) {
            System.out.print("Enter application closing date (yyyy-MM-dd): ");
            try {
                closeDate = LocalDate.parse(scanner.nextLine(), DATE_FORMAT);
                if (closeDate.isBefore(openDate)) {
                    System.out.println("Closing date must be after opening date.");
                    closeDate = null;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd.");
            }
        }
        
        BTOProject project = new BTOProject(projectName, neighborhood, flatUnits, openDate, closeDate, manager);
        projectManager.addProject(project);
        System.out.println("Project created successfully!");
    }

    /**
     * Displays all BTO projects in the system.
     */
    private void viewAllProjects() {
        List<BTOProject> projects = projectManager.getAllProjects();
        displayProjects(projects);
    }

    /**
     * Displays the projects created by the HDB Manager.
     */
    private void viewMyProjects() {
        List<BTOProject> projects = manager.getCreatedProjects();
        displayProjects(projects);
    }

    /**
     * Displays a list of projects and provides options to view details or toggle visibility.
     * @param projects The list of projects to display
     */
    private void displayProjects(List<BTOProject> projects) {
        if (projects.isEmpty()) {
            System.out.println("No projects found.");
            return;
        }

        while (true) {
            System.out.println("\nProjects:");
            for (int i = 0; i < projects.size(); i++) {
                BTOProject project = projects.get(i);
                System.out.printf("%d. %s (%s) - %s%n",
                    i + 1,
                    project.getProjectName(),
                    project.getNeighborhood(),
                    project.isVisible() ? "Visible" : "Hidden");
            }

            System.out.println("\n1. View Project Details");
            System.out.println("2. Toggle Project Visibility");
            System.out.println("3. Go Back");
            System.out.print("Choose an option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 3) break;

            System.out.print("Enter project number: ");
            int projectNum = scanner.nextInt();
            scanner.nextLine();

            if (projectNum < 1 || projectNum > projects.size()) {
                System.out.println("Invalid project number.");
                continue;
            }

            BTOProject selected = projects.get(projectNum - 1);
            switch (choice) {
                case 1:
                    viewProjectDetails(selected);
                    break;
                case 2:
                    selected.setVisible(!selected.isVisible());
                    projectManager.saveProjects();
                    System.out.println("Project visibility toggled successfully!");
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    /**
     * Displays detailed information about a specific BTO project.
     * @param project The project to view details for
     */
    private void viewProjectDetails(BTOProject project) {
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
    }

    /**
     * Displays applications for a specific project created by the HDB Manager.
     */
    private void viewProjectApplications() {
        List<BTOProject> projects = manager.getCreatedProjects();
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
    }

    /**
     * Displays enquiries for a specific project.
     */
    private void viewProjectEnquiries() {
        List<BTOProject> projects = projectManager.getAllProjects();
        if (projects.isEmpty()) {
            System.out.println("No projects found.");
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
        List<Enquiry> enquiries = enquiryManager.getEnquiriesForProject(selected.getProjectName());
        
        if (enquiries.isEmpty()) {
            System.out.println("No enquiries for this project.");
            return;
        }

        System.out.println("\nEnquiries:");
        for (Enquiry enquiry : enquiries) {
            System.out.printf("From: %s%nContent: %s%nReply: %s%n%n",
                enquiry.getCreator().getNric(),
                enquiry.getContent(),
                enquiry.getReply() != null ? enquiry.getReply() : "No reply yet");
        }
    }

    /**
     * Manages officer registrations for a specific project.
     */
    private void manageOfficerRegistrations() {
        List<BTOProject> projects = manager.getCreatedProjects();
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

    /**
     * Generates various reports for a specific project.
     */
    private void generateReports() {
        List<BTOProject> projects = manager.getCreatedProjects();
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

    /**
     * Generates a report of all applications for a project.
     * @param applications The list of applications to include in the report
     */
    private void generateAllApplicationsReport(List<BTOApplication> applications) {
        System.out.printf("Total Applications: %d%n%n", applications.size());
        for (BTOApplication app : applications) {
            printApplicationDetails(app);
        }
    }

    /**
     * Generates a report of successful applications for a project.
     * @param applications The list of applications to include in the report
     */
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

    /**
     * Generates a report of booked flats for a project.
     * @param applications The list of applications to include in the report
     */
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

    /**
     * Generates a report of applications grouped by flat type.
     * @param applications The list of applications to include in the report
     */
    private void generateApplicationsByFlatTypeReport(List<BTOApplication> applications) {
        Map<FlatType, List<BTOApplication>> byType = new HashMap<>();
        for (BTOApplication app : applications) {
            byType.computeIfAbsent(app.getSelectedFlatType(), k -> new ArrayList<>()).add(app);
        }

        for (Map.Entry<FlatType, List<BTOApplication>> entry : byType.entrySet()) {
            System.out.printf("%s Applications: %d%n", entry.getKey().getDisplayName(), entry.getValue().size());
            for (BTOApplication app : entry.getValue()) {
                printApplicationDetails(app);
            }
            System.out.println();
        }
    }

    /**
     * Generates a report of applications grouped by marital status.
     * @param applications The list of applications to include in the report
     */
    private void generateApplicationsByMaritalStatusReport(List<BTOApplication> applications) {
        Map<MaritalStatus, List<BTOApplication>> byStatus = new HashMap<>();
        for (BTOApplication app : applications) {
            byStatus.computeIfAbsent(app.getApplicant().getMaritalStatus(), k -> new ArrayList<>()).add(app);
        }

        for (Map.Entry<MaritalStatus, List<BTOApplication>> entry : byStatus.entrySet()) {
            System.out.printf("%s Applications: %d%n", entry.getKey(), entry.getValue().size());
            for (BTOApplication app : entry.getValue()) {
                printApplicationDetails(app);
            }
            System.out.println();
        }
    }

    /**
     * Prints the details of a specific application.
     * @param app The application to print details for
     */
    private void printApplicationDetails(BTOApplication app) {
        System.out.printf("NRIC: %s%n", app.getApplicant().getNric());
        System.out.printf("Age: %d%n", app.getApplicant().getAge());
        System.out.printf("Marital Status: %s%n", app.getApplicant().getMaritalStatus());
        System.out.printf("Flat Type: %s%n", app.getSelectedFlatType().getDisplayName());
        System.out.printf("Status: %s%n", app.getStatus());
        System.out.printf("Application Date: %s%n", app.getApplicationDate());
        System.out.println();
    }

    /**
     * Allows the HDB Manager to change their password.
     */
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
