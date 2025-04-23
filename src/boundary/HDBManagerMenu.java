package boundary;

import control.*;
import entity.*;
import enums.*;
import util.TablePrinter;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.io.*;

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
            
            try {
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
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number (1-9).");
                scanner.nextLine(); // Clear invalid input
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                scanner.nextLine(); // Clear invalid input
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
        
        boolean autoPublish = false;
        while (true) {
            System.out.print("Enable auto-publish when opening date is reached? (Y/N): ");
            String choice = scanner.nextLine();
            if (choice.equalsIgnoreCase("Y")) {
                autoPublish = true;
                break;
            } else if (choice.equalsIgnoreCase("N")) {
                autoPublish = false;
                break;
            } else {
                System.out.println("Invalid input. Please enter Y or N.");
            }
        }
        
        BTOProject project = new BTOProject(name, neighborhood, flatUnits, openDate, closeDate, manager, maxOfficerSlots, autoPublish);
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
        
        // Use a simpler format for dates to ensure better alignment
        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        String[] headers = {"No.", "Project Name", "Neighborhood", "Status", "Application Period"};
        String[][] data = new String[projects.size()][5];
        
        for (int i = 0; i < projects.size(); i++) {
            BTOProject project = projects.get(i);
            String openDate = project.getApplicationOpenDate().format(displayFormat);
            String closeDate = project.getApplicationCloseDate().format(displayFormat);
            
            data[i][0] = String.valueOf(i + 1);
            data[i][1] = project.getProjectName();
            data[i][2] = project.getNeighborhood();
            data[i][3] = project.isVisible() ? "Visible" : "Hidden";
            data[i][4] = openDate + " - " + closeDate;
        }
        
        TablePrinter.printTable(headers, data);

        System.out.print("Enter project number to view details (0 to go back): ");
        try {
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice > 0 && choice <= projects.size()) {
                BTOProject selected = projects.get(choice - 1);
                viewProjectDetails(selected, false);
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid project number.");
            scanner.nextLine();
        }
    }

    private void viewMyProjects() {
        List<BTOProject> projects = manager.getManagedProjects();
        if (projects.isEmpty()) {
            System.out.println("You have no projects.");
            return;
        }

        System.out.println("\nMy Projects:");
        System.out.println("Sorting Options:");
        System.out.println("1. Default order (sort by date)");
        System.out.println("2. Alphabetical order (A-Z)");
        System.out.print("Choose sorting option: ");
        
        try {
            int sortOption = scanner.nextInt();
            scanner.nextLine();
            
            if (sortOption == 2) {
                // Sort projects alphabetically by name
                projects.sort(Comparator.comparing(BTOProject::getProjectName));
                System.out.println("\nProjects sorted alphabetically (A-Z)");
            } else {
                System.out.println("\nProjects in default order (sorted by date)");
            }
            
            // Use a simpler format for dates to ensure better alignment
            DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            String[] headers = {"No.", "Project Name", "Neighborhood", "Status", "Application Period"};
            String[][] data = new String[projects.size()][5];
            
            for (int i = 0; i < projects.size(); i++) {
                BTOProject project = projects.get(i);
                String openDate = project.getApplicationOpenDate().format(displayFormat);
                String closeDate = project.getApplicationCloseDate().format(displayFormat);
                
                data[i][0] = String.valueOf(i + 1);
                data[i][1] = project.getProjectName();
                data[i][2] = project.getNeighborhood();
                data[i][3] = project.isVisible() ? "Visible" : "Hidden";
                data[i][4] = openDate + " - " + closeDate;
            }
            
            TablePrinter.printTable(headers, data);

            System.out.print("Enter project number to view details (0 to go back): ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice > 0 && choice <= projects.size()) {
                BTOProject selected = projects.get(choice - 1);
                viewProjectDetails(selected, true);
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid number.");
            scanner.nextLine();
        }
    }

    private void viewProjectDetails(BTOProject project, boolean isOwnedProject) {
        System.out.println("\nProject Details:");
        
        // Project basic information table
        String[] basicHeaders = {"Property", "Value"};
        String[][] basicData = {
            {"Name", project.getProjectName()},
            {"Neighborhood", project.getNeighborhood()},
            {"Application Period", project.getApplicationOpenDate() + " to " + project.getApplicationCloseDate()},
            {"Visibility", project.isVisible() ? "Visible" : "Hidden"},
            {"Auto-Publish", project.isAutoPublish() ? "Enabled" : "Disabled"},
            {"Manager", project.getManager().getNric()}
        };
        
        TablePrinter.printTable(basicHeaders, basicData);
        
        // Flat units table
        System.out.println("\nFlat Units:");
        String[] flatHeaders = {"Flat Type", "Total Units", "Remaining Units"};
        String[][] flatData = new String[project.getFlatUnits().size()][3];
        
        int i = 0;
        for (Map.Entry<FlatType, Integer> entry : project.getFlatUnits().entrySet()) {
            flatData[i][0] = entry.getKey().getDisplayName();
            flatData[i][1] = String.valueOf(entry.getValue());
            flatData[i][2] = String.valueOf(project.getRemainingUnits().get(entry.getKey()));
            i++;
        }
        
        TablePrinter.printTable(flatHeaders, flatData);
        
        // Assigned officers table
        System.out.println("\nAssigned Officers:");
        List<HDBOfficer> officers = project.getOfficers();
        if (officers.isEmpty()) {
            System.out.println("No officers assigned.");
        } else {
            String[] officerHeaders = {"No.", "NRIC", "Name", "Status"};
            String[][] officerData = new String[officers.size()][4];
            
            for (i = 0; i < officers.size(); i++) {
                HDBOfficer officer = officers.get(i);
                officerData[i][0] = String.valueOf(i + 1);
                officerData[i][1] = officer.getNric();
                officerData[i][2] = officer.getName();
                officerData[i][3] = officer.isRegistrationApproved() ? "Approved" : "Pending";
            }
            
            TablePrinter.printTable(officerHeaders, officerData);
        }

        if (isOwnedProject) {
            System.out.println("\n1. Toggle Project Visibility");
            System.out.println("2. Edit Project Details");
            System.out.println("3. Delete Project");
            System.out.println("4. Go Back");
            System.out.print("Choose an option: ");
            
            try {
                int choice = scanner.nextInt();
                scanner.nextLine();
                
                switch (choice) {
                    case 1:
                        boolean newVisibility = !project.isVisible();
                        project.setVisible(newVisibility);
                        
                        // If visibility is being set to hidden, disable auto-publish to prevent contradictions
                        if (!newVisibility && project.isAutoPublish()) {
                            project.setAutoPublish(false);
                            System.out.println("Project visibility set to Hidden. Auto-publish has been disabled to prevent visibility conflicts.");
                        } else {
                            System.out.println("Project visibility toggled successfully!");
                        }
                        
                        projectManager.saveProjects();
                        System.out.println("\nUpdated project details:");
                        viewProjectDetails(project, isOwnedProject);  // Reprint project details after toggle
                        return;
                    case 2:
                        editProjectDetails(project);
                        break;
                    case 3:
                        deleteProject(project);
                        break;
                    case 4:
                        return;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid option.");
                scanner.nextLine();
            }
        } else {
            System.out.println("\nPress Enter to go back...");
            scanner.nextLine();
        }
    }
    
    private void deleteProject(BTOProject project) {
        LocalDate now = LocalDate.now();
        
        // Check if project is currently open for applications - don't allow deletion
        if (project.isApplicationOpen()) {
            System.out.println("\nError: Cannot delete a project that is currently open for applications.");
            System.out.println("This project is accepting applications until " + 
                              project.getApplicationCloseDate().format(DATE_FORMAT) + ".");
            System.out.println("Please try again after the application period has ended.");
            return;
        }
        
        // Check if project has applications and it's not yet closed
        List<BTOApplication> applications = applicationManager.getApplicationsForProject(project.getProjectName());
        if (!applications.isEmpty() && now.isBefore(project.getApplicationCloseDate())) {
            System.out.println("\nError: Cannot delete a project that has applications before its closing date.");
            System.out.println("This project has " + applications.size() + " application(s) and closes on " + 
                              project.getApplicationCloseDate().format(DATE_FORMAT) + ".");
            return;
        }
        
        // Project deletion is allowed if:
        // 1. The project hasn't opened yet (future project), or
        // 2. The project's application period has ended (closed project)
        
        String projectStatus;
        if (now.isBefore(project.getApplicationOpenDate())) {
            projectStatus = "unopened (future)";
        } else if (now.isAfter(project.getApplicationCloseDate())) {
            projectStatus = "closed";
        } else {
            projectStatus = "unknown state";  // This should not happen due to earlier checks
        }
        
        System.out.println("\nThis " + projectStatus + " project can be deleted.");
        
        // Check if the project has any applications
        if (!applications.isEmpty()) {
            System.out.println("Warning: This project has " + applications.size() + " applications.");
            System.out.println("Deleting this project will affect these applications.");
        }
        
        System.out.print("\nAre you sure you want to delete this project? (Y/N): ");
        String confirmation = scanner.nextLine();
        
        if (confirmation.equalsIgnoreCase("Y")) {
            // Ask for password confirmation for security
            System.out.print("Enter your password to confirm deletion: ");
            String password = scanner.nextLine();
            
            // Use login to verify the password instead of the non-existent verifyPassword method
            if (UserManager.getInstance().login(manager.getNric(), password)) {
                // Use ProjectManager's deleteProject method instead of handling deletion directly
                if (projectManager.deleteProject(project.getProjectName())) {
                    System.out.println("\nProject '" + project.getProjectName() + "' has been deleted successfully.");
                } else {
                    System.out.println("Failed to delete the project. Please try again later.");
                }
                // Re-login as the current user since we used login for verification
                UserManager.getInstance().login(manager.getNric(), password);
            } else {
                System.out.println("Incorrect password. Project deletion cancelled.");
            }
        } else {
            System.out.println("Project deletion cancelled.");
        }
    }

    private void editProjectDetails(BTOProject project) {
        System.out.println("\nEdit Project Details:");
        System.out.println("1. Edit Neighborhood");
        System.out.println("2. Edit Application Dates");
        System.out.println("3. Toggle Auto-Publish Setting");
        System.out.println("4. Go Back");
        System.out.print("Choose an option: ");
        
        try {
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    System.out.print("Enter new neighborhood: ");
                    String neighborhood = scanner.nextLine();
                    project.setNeighborhood(neighborhood);
                    projectManager.saveProjects();
                    System.out.println("Neighborhood updated successfully!");
                    System.out.println("\nUpdated project details:");
                    viewProjectDetails(project, true);  // Reprint project details after edit
                    return;
                    
                case 2:
                    editProjectDates(project);
                    return;
                    
                case 3:
                    // Toggle auto-publish setting
                    project.setAutoPublish(!project.isAutoPublish());
                    projectManager.saveProjects();
                    System.out.println("Auto-publish setting toggled to: " + 
                                      (project.isAutoPublish() ? "ENABLED" : "DISABLED"));
                    System.out.println("\nUpdated project details:");
                    viewProjectDetails(project, true);  // Reprint project details after toggle
                    return;
                    
                case 4:
                    return;
                    
                default:
                    System.out.println("Invalid option.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid option.");
            scanner.nextLine();
        }
    }
    
    private void editProjectDates(BTOProject project) {
        LocalDate now = LocalDate.now();
        
        // Check if project has applications - if yes, only allow editing close date
        boolean hasApplications = !project.getApplications().isEmpty();
        
        if (hasApplications) {
            System.out.println("Note: This project has existing applications.");
            System.out.println("You can only modify the closing date. The opening date cannot be changed.");
            
            LocalDate closeDate = null;
            while (closeDate == null) {
                System.out.print("Enter new application close date (yyyy-MM-dd): ");
                try {
                    closeDate = LocalDate.parse(scanner.nextLine(), DATE_FORMAT);
                    
                    // Make sure new close date is after open date and today
                    if (closeDate.isBefore(project.getApplicationOpenDate())) {
                        System.out.println("Close date must be after open date.");
                        closeDate = null;
                    } else if (closeDate.isBefore(now)) {
                        System.out.println("Close date cannot be before today.");
                        closeDate = null;
                    }
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date format. Please use yyyy-MM-dd.");
                }
            }
            
            project.setApplicationCloseDate(closeDate);
            projectManager.saveProjects();
            System.out.println("Application closing date updated successfully!");
            System.out.println("\nUpdated project details:");
            viewProjectDetails(project, true);  // Reprint project details after date changes
            return;
        }
        
        // Original behavior for projects without applications (can modify both dates)
        LocalDate openDate = null;
        while (openDate == null) {
            System.out.print("Enter new application open date (yyyy-MM-dd): ");
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
        
        LocalDate closeDate = null;
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
        System.out.println("Application dates updated successfully!");
        System.out.println("\nUpdated project details:");
        viewProjectDetails(project, true);  // Reprint project details after date changes
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
        try {
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

            // Display applications table
            System.out.println("\nApplications:");
            String[] headers = {"No.", "NRIC", "Name", "Flat Type", "Status", "Withdrawal"};
            String[][] data = new String[applications.size()][6];
            
            for (int i = 0; i < applications.size(); i++) {
                BTOApplication app = applications.get(i);
                data[i][0] = String.valueOf(i + 1); 
                data[i][1] = app.getApplicant().getNric();
                data[i][2] = app.getApplicant().getName();
                data[i][3] = app.getSelectedFlatType().getDisplayName();
                data[i][4] = app.getStatus().toString();
                data[i][5] = app.isWithdrawalRequested() ? "Requested" : "-";
            }
            
            TablePrinter.printTable(headers, data);
            
            // Display flat type distribution statistics
            Map<FlatType, Integer> flatTypeCounts = new HashMap<>();
            for (BTOApplication app : applications) {
                flatTypeCounts.merge(app.getSelectedFlatType(), 1, Integer::sum);
            }
            
            System.out.println("\nFlat Type Distribution:");
            String[] statHeaders = {"Flat Type", "Applications", "Percentage"};
            String[][] statData = new String[flatTypeCounts.size()][3];
            
            int row = 0;
            for (Map.Entry<FlatType, Integer> entry : flatTypeCounts.entrySet()) {
                double percentage = (entry.getValue() * 100.0) / applications.size();
                statData[row][0] = entry.getKey().getDisplayName();
                statData[row][1] = String.valueOf(entry.getValue());
                statData[row][2] = String.format("%.1f%%", percentage);
                row++;
            }
            
            TablePrinter.printTable(statHeaders, statData);
            
            // Options menu for applications
            System.out.println("\nOptions:");
            System.out.println("1. View Detailed Application Information");
            System.out.println("2. Process Application Status");
            System.out.println("3. Process Withdrawal Requests");
            System.out.println("4. Go Back");
            System.out.print("Choose an option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    System.out.print("Enter application number: ");
                    int appNum = scanner.nextInt();
                    scanner.nextLine();
                    
                    if (appNum < 1 || appNum > applications.size()) {
                        System.out.println("Invalid application number.");
                    } else {
                        BTOApplication selected_app = applications.get(appNum - 1);
                        displayDetailedApplicationInfo(selected_app, selected);
                    }
                    break;
                case 2:
                    processApplicationStatus(applications, selected);
                    break;
                case 3:
                    processWithdrawalRequests(applications, selected);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid project number.");
            scanner.nextLine();
        }
    }
    
    private void processApplicationStatus(List<BTOApplication> applications, BTOProject project) {
        System.out.println("\nProcess Application Status:");
    
        System.out.print("Enter application number: ");
        
        try {
            int appNum = scanner.nextInt();
            scanner.nextLine();
            
            if (appNum < 1 || appNum > applications.size()) {
                System.out.println("Invalid application number.");
                return;
            }
            
            BTOApplication application = applications.get(appNum - 1);
            ApplicationStatus currentStatus = application.getStatus();
            Applicant applicant = application.getApplicant();
            
            System.out.printf("\nProcessing application for %s (NRIC: %s)%n", 
                applicant.getName(), applicant.getNric());
            System.out.println("Current Status: " + currentStatus);
            
            // Only allow certain status transitions
            if (currentStatus == ApplicationStatus.UNSUCCESSFUL) {
                System.out.println("\nYou can change this application's status to SUCCESSFUL.");
                System.out.print("Change to SUCCESSFUL? (Y/N): ");
                String confirm = scanner.nextLine();
                
                if (confirm.equalsIgnoreCase("Y")) {
                    if (applicationManager.updateApplicationStatus(application, ApplicationStatus.SUCCESSFUL)) {
                        System.out.println("Application status updated from UNSUCCESSFUL to SUCCESSFUL!");
                        System.out.println("The HDB Officer can now process this application for flat booking.");
                    } else {
                        System.out.println("Failed to update application status.");
                    }
                }
            } else if (currentStatus == ApplicationStatus.PENDING) {
                System.out.println("\nOptions:");
                System.out.println("1. Mark as SUCCESSFUL");
                System.out.println("2. Mark as UNSUCCESSFUL");
                System.out.println("3. Go back");
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
                    case 3:
                        return;
                    default:
                        System.out.println("Invalid option.");
                        return;
                }
                
                if (applicationManager.updateApplicationStatus(application, newStatus)) {
                    System.out.println("Application status updated to " + newStatus + " successfully!");
                } else {
                    System.out.println("Failed to update application status.");
                }
            } else {
                System.out.println("This application is already in " + currentStatus + " status.");
                System.out.println("No changes needed or possible at this stage.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid number.");
            scanner.nextLine();
        }
    }

    private void processWithdrawalRequests(List<BTOApplication> applications, BTOProject project) {
        // Filter to show only applications with withdrawal requests
        List<BTOApplication> withdrawalRequests = new ArrayList<>();
        for (BTOApplication app : applications) {
            if (app.isWithdrawalRequested()) {
                withdrawalRequests.add(app);
            }
        }
        
        if (withdrawalRequests.isEmpty()) {
            System.out.println("No withdrawal requests pending for this project.");
            return;
        }
        
        System.out.println("\nPending Withdrawal Requests:");
        String[] headers = {"No.", "NRIC", "Name", "Flat Type", "Status"};
        String[][] data = new String[withdrawalRequests.size()][5];
        
        for (int i = 0; i < withdrawalRequests.size(); i++) {
            BTOApplication app = withdrawalRequests.get(i);
            data[i][0] = String.valueOf(i + 1);
            data[i][1] = app.getApplicant().getNric();
            data[i][2] = app.getApplicant().getName();
            data[i][3] = app.getSelectedFlatType().getDisplayName();
            data[i][4] = app.getStatus().toString();
        }
        
        TablePrinter.printTable(headers, data);
        
        System.out.print("Enter application number to process (0 to go back): ");
        try {
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            if (choice == 0) {
                return;
            }
            
            if (choice < 1 || choice > withdrawalRequests.size()) {
                System.out.println("Invalid application number.");
                return;
            }
            
            BTOApplication selectedApp = withdrawalRequests.get(choice - 1);
            Applicant applicant = selectedApp.getApplicant();
            
            System.out.printf("\nProcessing withdrawal request for %s (NRIC: %s)%n", 
                applicant.getName(), applicant.getNric());
            System.out.println("Flat Type: " + selectedApp.getSelectedFlatType().getDisplayName());
            System.out.println("Application Status: " + selectedApp.getStatus());
            
            System.out.println("\n1. Approve Withdrawal");
            System.out.println("2. Reject Withdrawal");
            System.out.println("3. Go Back");
            System.out.print("Choose an option: ");
            
            int option = scanner.nextInt();
            scanner.nextLine();
            
            switch (option) {
                case 1:
                    // If the application is BOOKED, explain that units will be returned to the pool
                    if (selectedApp.getStatus() == ApplicationStatus.BOOKED) {
                        System.out.println("\nNote: This application has BOOKED status.");
                        System.out.println("Approving this withdrawal will return the unit to the available pool.");
                        System.out.print("Confirm approval? (Y/N): ");
                        
                        String confirm = scanner.nextLine();
                        if (!confirm.equalsIgnoreCase("Y")) {
                            System.out.println("Withdrawal approval cancelled.");
                            return;
                        }
                    }
                    
                    if (applicationManager.approveWithdrawal(selectedApp)) {
                        System.out.println("Withdrawal request approved successfully!");
                        System.out.println("The applicant can now apply for another project.");
                        
                        // If it was a BOOKED application, show updated flat units
                        if (selectedApp.getStatus() == ApplicationStatus.BOOKED) {
                            System.out.println("\nUpdated Flat Units:");
                            String[] unitHeaders = {"Flat Type", "Available Units"};
                            String[][] unitData = new String[project.getRemainingUnits().size()][2];
                            
                            int i = 0;
                            for (Map.Entry<FlatType, Integer> entry : project.getRemainingUnits().entrySet()) {
                                unitData[i][0] = entry.getKey().getDisplayName();
                                unitData[i][1] = String.valueOf(entry.getValue());
                                i++;
                            }
                            
                            TablePrinter.printTable(unitHeaders, unitData);
                        }
                    } else {
                        System.out.println("Failed to approve withdrawal request.");
                    }
                    break;
                    
                case 2:
                    // Reset the withdrawal request flag but keep the application
                    if (applicationManager.rejectWithdrawal(selectedApp)) {
                        System.out.println("Withdrawal request rejected.");
                        System.out.println("The application will remain active.");
                    } else {
                        System.out.println("Failed to reject withdrawal request.");
                    }
                    break;
                    
                case 3:
                    return;
                    
                default:
                    System.out.println("Invalid option.");
            }
            
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid number.");
            scanner.nextLine();
        }
    }

    private void viewProjectEnquiries() {
        // Change to use manager's created projects instead of all projects
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
        try {
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
            List<Enquiry> enquiries = enquiryManager.getEnquiriesForProject(selected.getProjectName());
            
            if (enquiries.isEmpty()) {
                System.out.println("No enquiries for this project.");
                return;
            }

            System.out.println("\nEnquiries:");
            String[] headers = {"No.", "From", "Content", "Reply Status"};
            String[][] data = new String[enquiries.size()][4];
            
            for (int i = 0; i < enquiries.size(); i++) {
                Enquiry enquiry = enquiries.get(i);
                data[i][0] = String.valueOf(i + 1);
                data[i][1] = enquiry.getCreator().getNric() + " (" + enquiry.getCreator().getName() + ")";
                data[i][2] = enquiry.getContent();
                data[i][3] = enquiry.hasReply() ? "Replied" : "Pending";
            }
            
            TablePrinter.printTable(headers, data);
            
            // Display detailed view of enquiries with replies in a more organized format (not using tables)
            boolean hasReplies = false;
            System.out.println("\nDetailed Enquiries with Replies:");
            for (int i = 0; i < enquiries.size(); i++) {
                Enquiry enquiry = enquiries.get(i);
                if (enquiry.hasReply()) {
                    hasReplies = true;
                    String separator = "-------------------------------------------";
                    System.out.println("\n" + separator);
                    System.out.println("Enquiry #" + (i + 1));
                    System.out.println("From: " + enquiry.getCreator().getNric() + " (" + enquiry.getCreator().getName() + ")");
                    System.out.println("Question: " + enquiry.getContent());
                    System.out.println("Reply: " + enquiry.getReply());
                    System.out.println(separator);
                }
            }
            
            if (!hasReplies) {
                System.out.println("No replied enquiries found.");
            }
            
            // Since we're now only showing projects the manager owns, this check is redundant
            // but keeping it for safety
            if (manager.getManagedProjects().contains(selected)) {
                System.out.print("\nEnter enquiry number to reply (0 to go back): ");
                try {
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
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a valid enquiry number.");
                    scanner.nextLine();
                }
            } else {
                System.out.println("\nPress Enter to go back...");
                scanner.nextLine();
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid project number.");
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
        String[] projectHeaders = {"No.", "Project Name", "Neighborhood", "Officers Assigned", "Max Officers"};
        String[][] projectData = new String[projects.size()][5];
        
        for (int i = 0; i < projects.size(); i++) {
            BTOProject project = projects.get(i);
            projectData[i][0] = String.valueOf(i + 1);
            projectData[i][1] = project.getProjectName();
            projectData[i][2] = project.getNeighborhood();
            projectData[i][3] = String.valueOf(project.getOfficers().size());
            projectData[i][4] = String.valueOf(project.getMaxOfficerSlots());
        }
        
        TablePrinter.printTable(projectHeaders, projectData);

        System.out.print("Enter project number: ");
        try {
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
            String[] officerHeaders = {"No.", "Name", "NRIC", "Age", "Marital Status"};
            String[][] officerData = new String[pendingOfficers.size()][5];
            
            for (int i = 0; i < pendingOfficers.size(); i++) {
                HDBOfficer officer = pendingOfficers.get(i);
                officerData[i][0] = String.valueOf(i + 1);
                officerData[i][1] = officer.getName();
                officerData[i][2] = officer.getNric();
                officerData[i][3] = String.valueOf(officer.getAge());
                officerData[i][4] = officer.getMaritalStatus().toString();
            }
            
            TablePrinter.printTable(officerHeaders, officerData);

            System.out.print("Enter officer number to approve/reject (0 to go back): ");
            try {
                int officerNum = scanner.nextInt();
                scanner.nextLine();

                if (officerNum == 0) {
                    return;
                }

                if (officerNum > 0 && officerNum <= pendingOfficers.size()) {
                    System.out.println("\nOptions:");
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
                    } else {
                        System.out.println("Invalid option selected.");
                    }
                } else {
                    System.out.println("Invalid officer number.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid officer number.");
                scanner.nextLine();
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid project number.");
            scanner.nextLine();
        }
    }

    private void generateReports() {
        List<BTOProject> projects = manager.getManagedProjects();
        if (projects.isEmpty()) {
            System.out.println("You have no projects.");
            return;
        }

        System.out.println("\nSelect Project:");
        String[] projectHeaders = {"No.", "Project Name", "Neighborhood"};
        String[][] projectData = new String[projects.size()][3];
        
        for (int i = 0; i < projects.size(); i++) {
            projectData[i][0] = String.valueOf(i + 1);
            projectData[i][1] = projects.get(i).getProjectName();
            projectData[i][2] = projects.get(i).getNeighborhood();
        }
        
        TablePrinter.printTable(projectHeaders, projectData);

        System.out.print("Enter project number: ");
        try {
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
            
            try {
                int choice = scanner.nextInt();
                scanner.nextLine();

                System.out.println("\nReport:");
                switch (choice) {
                    case 1:
                        generateAllApplicationsReport(applications, selected.getProjectName());
                        break;
                    case 2:
                        generateSuccessfulApplicationsReport(applications, selected.getProjectName());
                        break;
                    case 3:
                        generateBookedFlatsReport(applications, selected.getProjectName());
                        break;
                    case 4:
                        generateApplicationsByFlatTypeReport(applications, selected.getProjectName());
                        break;
                    case 5:
                        generateApplicationsByMaritalStatusReport(applications, selected.getProjectName());
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
                
                // Ask if user wants to save the report to a file
                System.out.print("\nSave report to a file? (Y/N): ");
                String saveChoice = scanner.nextLine();
                if (saveChoice.equalsIgnoreCase("Y")) {
                    String reportType = getReportTypeName(choice);
                    String filename = "output_reports/" + selected.getProjectName().replaceAll("\\s+", "_") 
                                     + "_" + reportType + ".txt";
                    saveReportToFile(filename);
                    System.out.println("Report saved to " + filename);
                }
                
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid option.");
                scanner.nextLine();
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid project number.");
            scanner.nextLine();
        }
    }
    
    private String getReportTypeName(int choice) {
        switch (choice) {
            case 1: return "All_Applications";
            case 2: return "Successful_Applications";
            case 3: return "Booked_Flats";
            case 4: return "By_FlatType";
            case 5: return "By_MaritalStatus";
            default: return "Report";
        }
    }
    
    private void saveReportToFile(String filename) {
        try {
            // Create a new PrintStream that will capture System.out
            PrintStream originalOut = System.out;
            ByteArrayOutputStream reportContent = new ByteArrayOutputStream();
            PrintStream reportOut = new PrintStream(reportContent);
            
            // Temporarily redirect System.out to our buffer
            System.setOut(reportOut);
            
            // Re-run the last report to capture its output
            executeLastReport();
            
            // Restore the original System.out
            System.setOut(originalOut);
            reportOut.close();
            
            // Write the captured content to a file
            try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                writer.print(reportContent.toString());
            }
            
        } catch (IOException e) {
            System.err.println("Error saving report to file: " + e.getMessage());
        }
    }
    
    // Store the last report parameters to re-run it when saving to file
    private BTOProject lastReportProject;
    private List<BTOApplication> lastReportApplications;
    private int lastReportType;
    
    private void executeLastReport() {
        if (lastReportApplications == null || lastReportProject == null) {
            System.out.println("No report data available");
            return;
        }
        
        switch (lastReportType) {
            case 1:
                generateAllApplicationsReport(lastReportApplications, lastReportProject.getProjectName());
                break;
            case 2:
                generateSuccessfulApplicationsReport(lastReportApplications, lastReportProject.getProjectName());
                break;
            case 3:
                generateBookedFlatsReport(lastReportApplications, lastReportProject.getProjectName());
                break;
            case 4:
                generateApplicationsByFlatTypeReport(lastReportApplications, lastReportProject.getProjectName());
                break;
            case 5:
                generateApplicationsByMaritalStatusReport(lastReportApplications, lastReportProject.getProjectName());
                break;
        }
    }

    private void generateAllApplicationsReport(List<BTOApplication> applications, String projectName) {
        lastReportProject = projectManager.getProject(projectName);
        lastReportApplications = applications;
        lastReportType = 1;

        System.out.println("ALL APPLICATIONS REPORT - " + projectName);
        System.out.println("===============================================");
        System.out.printf("Total Applications: %d%n%n", applications.size());
        
        // Sort applications by status for better organization
        List<BTOApplication> sortedApps = new ArrayList<>(applications);
        sortedApps.sort((a1, a2) -> a1.getStatus().compareTo(a2.getStatus()));
        
        // Define date formatter for consistent display
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // Display summary statistics by application status
        Map<ApplicationStatus, Integer> statusCounts = new HashMap<>();
        for (BTOApplication app : applications) {
            statusCounts.merge(app.getStatus(), 1, Integer::sum);
        }
        
        System.out.println("APPLICATION STATUS BREAKDOWN:");
        String[] statusHeaders = {"Status", "Count", "Percentage"};
        String[][] statusData = new String[statusCounts.size()][3];
        
        int i = 0;
        for (Map.Entry<ApplicationStatus, Integer> entry : statusCounts.entrySet()) {
            double percentage = (entry.getValue() * 100.0) / applications.size();
            statusData[i][0] = entry.getKey().toString();
            statusData[i][1] = String.valueOf(entry.getValue());
            statusData[i][2] = String.format("%.1f%%", percentage);
            i++;
        }
        
        TablePrinter.printTable(statusHeaders, statusData);
        
        System.out.println("\nDETAILED APPLICANT INFORMATION:");
        System.out.println("================================");
        
        // Display each application in a vertical format with detailed applicant information
        for (i = 0; i < sortedApps.size(); i++) {
            BTOApplication app = sortedApps.get(i);
            Applicant applicant = app.getApplicant();
            
            String separator = "-----------------------------------------";
            System.out.println(separator);
            System.out.println("Application #" + (i+1) + " (" + app.getStatus() + ")");
            
            // Applicant Details
            System.out.println("\nAPPLICANT DETAILS:");
            System.out.println("NRIC: " + applicant.getNric());
            System.out.println("Name: " + applicant.getName());
            System.out.println("Age: " + applicant.getAge());
            System.out.println("Marital Status: " + applicant.getMaritalStatus());
            
            // Application Details
            System.out.println("\nAPPLICATION DETAILS:");
            System.out.println("Flat Type: " + app.getSelectedFlatType().getDisplayName());
            System.out.println("Application Date: " + app.getApplicationDate().format(dateFormatter));
            if (app.isWithdrawalRequested()) {
                System.out.println("Withdrawal: Requested");
            }
            
            // Eligibility Information
            System.out.println("\nELIGIBILITY INFORMATION:");
            System.out.println("Eligible Flat Types:");
            for (FlatType type : FlatType.values()) {
                if (applicant.canApplyForFlatType(type)) {
                    System.out.println("- " + type.getDisplayName());
                }
            }
            
            System.out.println(separator);
            System.out.println(); // Add extra line for spacing between applications
        }
        
        if (applications.isEmpty()) {
            System.out.println("No applications found for this project.");
        }
    }

    private void generateSuccessfulApplicationsReport(List<BTOApplication> applications, String projectName) {
        lastReportProject = projectManager.getProject(projectName);
        lastReportApplications = applications;
        lastReportType = 2;

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

    private void generateBookedFlatsReport(List<BTOApplication> applications, String projectName) {
        lastReportProject = projectManager.getProject(projectName);
        lastReportApplications = applications;
        lastReportType = 3;

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

    private void generateApplicationsByFlatTypeReport(List<BTOApplication> applications, String projectName) {
        lastReportProject = projectManager.getProject(projectName);
        lastReportApplications = applications;
        lastReportType = 4;

        Map<FlatType, List<BTOApplication>> appsByFlatType = new HashMap<>();
        
        // Group applications by flat type
        for (BTOApplication app : applications) {
            FlatType flatType = app.getSelectedFlatType();
            if (!appsByFlatType.containsKey(flatType)) {
                appsByFlatType.put(flatType, new ArrayList<>());
            }
            appsByFlatType.get(flatType).add(app);
        }
        
        // Display summary
        System.out.println("APPLICATIONS BY FLAT TYPE REPORT - " + projectName);
        String[] headers = {"Flat Type", "Number of Applications", "Percentage"};
        String[][] data = new String[appsByFlatType.size()][3];
        
        int i = 0;
        for (Map.Entry<FlatType, List<BTOApplication>> entry : appsByFlatType.entrySet()) {
            double percentage = (entry.getValue().size() * 100.0) / applications.size();
            data[i][0] = entry.getKey().getDisplayName();
            data[i][1] = String.valueOf(entry.getValue().size());
            data[i][2] = String.format("%.1f%%", percentage);
            i++;
        }
        
        TablePrinter.printTable(headers, data);
        
        // Display detailed applicant information for each flat type
        System.out.println("\nDETAILED APPLICANT INFORMATION BY FLAT TYPE");
        System.out.println("==============================================");
        
        for (Map.Entry<FlatType, List<BTOApplication>> entry : appsByFlatType.entrySet()) {
            FlatType flatType = entry.getKey();
            List<BTOApplication> flatTypeApps = entry.getValue();
            
            System.out.println("\n" + flatType.getDisplayName() + " APPLICATIONS (" + flatTypeApps.size() + ")");
            System.out.println(String.join("", Collections.nCopies(flatType.getDisplayName().length() + 14 + String.valueOf(flatTypeApps.size()).length() + 2, "-")));
            
            for (int j = 0; j < flatTypeApps.size(); j++) {
                BTOApplication app = flatTypeApps.get(j);
                Applicant applicant = app.getApplicant();
                
                System.out.println("\nApplicant #" + (j+1));
                System.out.println("Name: " + applicant.getName());
                System.out.println("NRIC: " + applicant.getNric());
                System.out.println("Age: " + applicant.getAge());
                System.out.println("Marital Status: " + applicant.getMaritalStatus());
                System.out.println("Application Status: " + app.getStatus());
                System.out.println("Application Date: " + app.getApplicationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                if (app.isWithdrawalRequested()) {
                    System.out.println("Withdrawal Requested: Yes");
                }
                System.out.println();
            }
        }
    }

    private void generateApplicationsByMaritalStatusReport(List<BTOApplication> applications, String projectName) {
        lastReportProject = projectManager.getProject(projectName);
        lastReportApplications = applications;
        lastReportType = 5;

        Map<MaritalStatus, List<BTOApplication>> appsByMaritalStatus = new HashMap<>();
        
        // Group applications by marital status
        for (BTOApplication app : applications) {
            MaritalStatus status = app.getApplicant().getMaritalStatus();
            if (!appsByMaritalStatus.containsKey(status)) {
                appsByMaritalStatus.put(status, new ArrayList<>());
            }
            appsByMaritalStatus.get(status).add(app);
        }
        
        // Display summary
        System.out.println("APPLICATIONS BY MARITAL STATUS REPORT - " + projectName);
        String[] headers = {"Marital Status", "Number of Applications", "Percentage"};
        String[][] data = new String[appsByMaritalStatus.size()][3];
        
        int i = 0;
        for (Map.Entry<MaritalStatus, List<BTOApplication>> entry : appsByMaritalStatus.entrySet()) {
            double percentage = (entry.getValue().size() * 100.0) / applications.size();
            data[i][0] = entry.getKey().toString();
            data[i][1] = String.valueOf(entry.getValue().size());
            data[i][2] = String.format("%.1f%%", percentage);
            i++;
        }
        
        TablePrinter.printTable(headers, data);
        
        // Display detailed applicant information for each marital status
        System.out.println("\nDETAILED APPLICANT INFORMATION BY MARITAL STATUS");
        System.out.println("=================================================");
        
        for (Map.Entry<MaritalStatus, List<BTOApplication>> entry : appsByMaritalStatus.entrySet()) {
            MaritalStatus status = entry.getKey();
            List<BTOApplication> statusApps = entry.getValue();
            
            System.out.println("\n" + status.toString() + " APPLICANTS (" + statusApps.size() + ")");
            System.out.println(String.join("", Collections.nCopies(status.toString().length() + 13 + String.valueOf(statusApps.size()).length() + 2, "-")));
            
            for (int j = 0; j < statusApps.size(); j++) {
                BTOApplication app = statusApps.get(j);
                Applicant applicant = app.getApplicant();
                
                System.out.println("\nApplicant #" + (j+1));
                System.out.println("Name: " + applicant.getName());
                System.out.println("NRIC: " + applicant.getNric());
                System.out.println("Age: " + applicant.getAge());
                System.out.println("Flat Type: " + app.getSelectedFlatType().getDisplayName());
                System.out.println("Application Status: " + app.getStatus());
                System.out.println("Application Date: " + app.getApplicationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                if (app.isWithdrawalRequested()) {
                    System.out.println("Withdrawal Requested: Yes");
                }
                
                // Eligibility Information 
                System.out.println("Eligible Flat Types:");
                for (FlatType type : FlatType.values()) {
                    if (applicant.canApplyForFlatType(type)) {
                        System.out.println("  - " + type.getDisplayName());
                    }
                }
                System.out.println();
            }
        }
    }

    private void printApplicationDetails(BTOApplication app) {
        // Define date formatter for consistent display
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Applicant applicant = app.getApplicant();
        
        // Print application overview with header
        String separator = "=================================================";
        System.out.println(separator);
        System.out.println("APPLICATION DETAILS - " + app.getProjectName());
        System.out.println(separator);
        
        // Basic application information
        System.out.println("Application Status: " + app.getStatus());
        System.out.println("Application Date: " + app.getApplicationDate().format(dateFormatter));
        System.out.println("Flat Type: " + app.getSelectedFlatType().getDisplayName());
        if (app.isWithdrawalRequested()) {
            System.out.println("Withdrawal: Requested");
        }
        
        // Detailed applicant information
        System.out.println("\nAPPLICANT INFORMATION:");
        System.out.println("-----------------------");
        System.out.println("Name: " + applicant.getName());
        System.out.println("NRIC: " + applicant.getNric());
        System.out.println("Age: " + applicant.getAge());
        System.out.println("Marital Status: " + applicant.getMaritalStatus());
        
        // Add housing eligibility information if available
        System.out.println("\nELIGIBILITY INFORMATION:");
        System.out.println("-----------------------");
        System.out.println("Eligible Flat Types: ");
        for (FlatType type : FlatType.values()) {
            if (applicant.canApplyForFlatType(type)) {
                System.out.println("- " + type.getDisplayName());
            }
        }
        
        System.out.println(separator);
        System.out.println(); // Add extra line for spacing between applications
    }

    private void displayDetailedApplicationInfo(BTOApplication application, BTOProject project) {
        System.out.println("\nDetailed Application Information:");
        System.out.println("==================================");
        
        Applicant applicant = application.getApplicant();
        
        // Basic application information
        String[] basicHeaders = {"Property", "Value"};
        String[][] basicData = {
            {"Project", project.getProjectName()},
            {"NRIC", applicant.getNric()},
            {"Applicant Name", applicant.getName()},
            {"Age", String.valueOf(applicant.getAge())},
            {"Marital Status", applicant.getMaritalStatus().toString()},
            {"Flat Type", application.getSelectedFlatType().getDisplayName()},
            {"Application Status", application.getStatus().toString()},
            {"Application Date", application.getApplicationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))},
            {"Withdrawal Requested", application.isWithdrawalRequested() ? "Yes" : "No"}
        };
        
        TablePrinter.printTable(basicHeaders, basicData);
        
        // Eligibility information
        System.out.println("\nEligibility Information:");
        System.out.println("Eligible Flat Types:");
        for (FlatType type : FlatType.values()) {
            if (applicant.canApplyForFlatType(type)) {
                System.out.println("- " + type.getDisplayName());
            }
        }
        
        // Actions
        System.out.println("\nActions:");
        System.out.println("1. Process Application Status");
        if (application.isWithdrawalRequested()) {
            System.out.println("2. Process Withdrawal Request");
        }
        if (application.getStatus() == ApplicationStatus.SUCCESSFUL) {
            System.out.println(application.isWithdrawalRequested() ? "3" : "2" + ". View Flat Booking Options");
        }
        System.out.println((application.isWithdrawalRequested() || application.getStatus() == ApplicationStatus.SUCCESSFUL) ? "4" : "3" + ". Go Back");
        
        System.out.print("Choose an option: ");
        try {
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    List<BTOApplication> apps = new ArrayList<>();
                    apps.add(application);
                    processApplicationStatus(apps, project);
                    break;
                case 2:
                    if (application.isWithdrawalRequested()) {
                        List<BTOApplication> withdrawalApps = new ArrayList<>();
                        withdrawalApps.add(application);
                        processWithdrawalRequests(withdrawalApps, project);
                    } else if (application.getStatus() == ApplicationStatus.SUCCESSFUL) {
                        System.out.println("View Flat Booking Options - Please direct the applicant to an HDB Officer for booking.");
                    } else {
                        return;
                    }
                    break;
                case 3:
                    if (application.isWithdrawalRequested() && application.getStatus() == ApplicationStatus.SUCCESSFUL) {
                        System.out.println("View Flat Booking Options - Please direct the applicant to an HDB Officer for booking.");
                    } else {
                        return;
                    }
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid number.");
            scanner.nextLine();
        }
    }

    private void changePassword() {
        System.out.print("Enter current password: ");
        String oldPassword = scanner.nextLine();
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();
        
        if (oldPassword.equals(newPassword)) {
            System.out.println("New password cannot be the same as the old password.");
            return;
        }
        
        if (UserManager.getInstance().changePassword(oldPassword, newPassword)) {
            System.out.println("Password changed successfully!");
        } else {
            System.out.println("Failed to change password. Please ensure your current password is correct.");
        }
    }
}