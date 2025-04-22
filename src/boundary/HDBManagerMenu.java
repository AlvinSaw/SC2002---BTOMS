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
                viewProjectDetails(selected, true);
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid project number.");
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
            System.out.println("3. Go Back");
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

            System.out.println("\nApplications:");
            String[] headers = {"NRIC", "Name", "Flat Type", "Status", "Withdrawal"};
            String[][] data = new String[applications.size()][5];
            
            for (int i = 0; i < applications.size(); i++) {
                BTOApplication app = applications.get(i);
                data[i][0] = app.getApplicant().getNric();
                data[i][1] = app.getApplicant().getName();
                data[i][2] = app.getSelectedFlatType().getDisplayName();
                data[i][3] = app.getStatus().toString();
                data[i][4] = app.isWithdrawalRequested() ? "Requested" : "-";
            }
            
            TablePrinter.printTable(headers, data);
            
            System.out.println("\nPress Enter to go back...");
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid project number.");
            scanner.nextLine();
        }
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
        try {
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
            
            if (manager.getManagedProjects().contains(selected)) {
                System.out.print("Enter enquiry number to reply (0 to go back): ");
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
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid option.");
                scanner.nextLine();
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid project number.");
            scanner.nextLine();
        }
    }

    private void generateAllApplicationsReport(List<BTOApplication> applications) {
        System.out.printf("Total Applications: %d%n%n", applications.size());
        
        // Define date formatter for consistent display
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        String[] headers = {"NRIC", "Name", "Age", "Marital Status", "Flat Type", "Status", "Date"};
        String[][] data = new String[applications.size()][7];
        
        for (int i = 0; i < applications.size(); i++) {
            BTOApplication app = applications.get(i);
            data[i][0] = app.getApplicant().getNric();
            data[i][1] = app.getApplicant().getName();
            data[i][2] = String.valueOf(app.getApplicant().getAge());
            data[i][3] = app.getApplicant().getMaritalStatus().toString();
            data[i][4] = app.getSelectedFlatType().getDisplayName();
            data[i][5] = app.getStatus().toString();
            data[i][6] = app.getApplicationDate().format(dateFormatter);
        }
        
        TablePrinter.printTable(headers, data);
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
        String[] headers = {"Flat Type", "Number of Applications"};
        String[][] data = new String[counts.size()][2];
        
        int i = 0;
        for (Map.Entry<FlatType, Integer> entry : counts.entrySet()) {
            data[i][0] = entry.getKey().getDisplayName();
            data[i][1] = entry.getValue().toString();
            i++;
        }
        
        TablePrinter.printTable(headers, data);
    }

    private void generateApplicationsByMaritalStatusReport(List<BTOApplication> applications) {
        Map<MaritalStatus, Integer> counts = new HashMap<>();
        for (BTOApplication app : applications) {
            counts.merge(app.getApplicant().getMaritalStatus(), 1, Integer::sum);
        }
        
        String[] headers = {"Marital Status", "Number of Applications"};
        String[][] data = new String[counts.size()][2];
        
        int i = 0;
        for (Map.Entry<MaritalStatus, Integer> entry : counts.entrySet()) {
            data[i][0] = entry.getKey().toString();
            data[i][1] = entry.getValue().toString();
            i++;
        }
        
        TablePrinter.printTable(headers, data);
    }

    private void printApplicationDetails(BTOApplication app) {
        // Define date formatter for consistent display
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        String[] headers = {"Property", "Value"};
        String[][] data = {
            {"Name", app.getApplicant().getName()},
            {"NRIC", app.getApplicant().getNric()},
            {"Age", String.valueOf(app.getApplicant().getAge())},
            {"Marital Status", app.getApplicant().getMaritalStatus().toString()},
            {"Flat Type", app.getSelectedFlatType().getDisplayName()},
            {"Status", app.getStatus().toString()},
            {"Application Date", app.getApplicationDate().format(dateFormatter)}
        };
        
        TablePrinter.printTable(headers, data);
        System.out.println(); // Add extra line for spacing between applications
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