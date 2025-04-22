package boundary;

import control.*;
import entity.*;
import enums.*;
import java.util.*;
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class ApplicantMenu {
    protected Scanner scanner = new Scanner(System.in);
    protected Applicant applicant;
    protected ProjectManager projectManager;
    protected ApplicationManager applicationManager;
    protected EnquiryManager enquiryManager;
    protected UserManager userManager;

    public ApplicantMenu(Applicant applicant) {
        this.applicant = applicant;
        this.projectManager = ProjectManager.getInstance();
        this.applicationManager = ApplicationManager.getInstance();
        this.enquiryManager = EnquiryManager.getInstance();
        this.userManager = UserManager.getInstance();
    }

    public void show() {
        while (true) {
            System.out.println("\n=== Applicant Menu ===");
            System.out.println("1. View Available Projects");
            System.out.println("2. View My Application");
            System.out.println("3. View My Enquiries");
            System.out.println("4. Create New Enquiry");
            System.out.println("5. Generate Receipt");
            System.out.println("6. Change Password");
            System.out.println("7. Logout");
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
                    generateReceipt();
                    break;
                case 6:
                    changePassword();
                    break;
                case 7:
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

        // Filter eligible projects
        List<BTOProject> eligibleProjects = new ArrayList<>();
        for (BTOProject project : projects) {
            // Check if project is visible
            if (!project.isVisible()) {
                continue;
            }

            // Check user's age and marital status
            if (applicant.getMaritalStatus() == MaritalStatus.SINGLE) {
                // Single applicants must be 35 or older
                if (applicant.getAge() < 35) {
                    continue;
                }
                // Single applicants can only apply for 2-room flats
                if (!project.getFlatUnits().containsKey(FlatType.TWO_ROOM)) {
                    continue;
                }
            } else {
                // Married applicants must be 21 or older
                if (applicant.getAge() < 21) {
                    continue;
                }
            }

            eligibleProjects.add(project);
        }

        if (eligibleProjects.isEmpty()) {
            System.out.println("No available projects found that match your eligibility criteria.");
            if (applicant.getMaritalStatus() == MaritalStatus.SINGLE && applicant.getAge() < 35) {
                System.out.println("As a single applicant, you must be 35 years or older to apply.");
            } else if (applicant.getMaritalStatus() == MaritalStatus.MARRIED && applicant.getAge() < 21) {
                System.out.println("As a married applicant, you must be 21 years or older to apply.");
            }
            return;
        }

        // Display sort/filter options
        System.out.println("\nSort/Filter Options:");
        System.out.println("1. Sort by Neighborhood (A-Z)");
        System.out.println("2. Sort by Available Units (High to Low)");
        System.out.println("3. Filter by Neighborhood");
        System.out.println("4. View All Projects");
        System.out.print("Enter your choice: ");
        
        int sortChoice = scanner.nextInt();
        scanner.nextLine();

        List<BTOProject> displayProjects = new ArrayList<>(eligibleProjects);

        switch (sortChoice) {
            case 1:
                // Sort by neighborhood name
                displayProjects.sort(Comparator.comparing(BTOProject::getNeighborhood));
                break;
            case 2:
                // Sort by total available units
                displayProjects.sort((p1, p2) -> {
                    int total1 = p1.getRemainingUnits().values().stream().mapToInt(Integer::intValue).sum();
                    int total2 = p2.getRemainingUnits().values().stream().mapToInt(Integer::intValue).sum();
                    return Integer.compare(total2, total1); // Descending order
                });
                break;
            case 3:
                // Filter by neighborhood
                System.out.print("Enter neighborhood to filter (leave empty to cancel): ");
                String filterNeighborhood = scanner.nextLine().trim();
                if (!filterNeighborhood.isEmpty()) {
                    displayProjects.removeIf(p -> !p.getNeighborhood().equalsIgnoreCase(filterNeighborhood));
                }
                break;
            case 4:
                // Keep original order
                break;
            default:
                System.out.println("Invalid choice. Showing all projects.");
        }

        if (displayProjects.isEmpty()) {
            System.out.println("No projects match your filter criteria.");
            return;
        }

        System.out.println("\nAvailable Projects:");
        for (int i = 0; i < displayProjects.size(); i++) {
            BTOProject project = displayProjects.get(i);
            int totalUnits = project.getRemainingUnits().values().stream().mapToInt(Integer::intValue).sum();
            System.out.printf("%d. %s (%s) - Available Units: %d%n", 
                i + 1, 
                project.getProjectName(), 
                project.getNeighborhood(),
                totalUnits);
        }

        System.out.print("Enter project number to view details (0 to go back): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); 

        if (choice > 0 && choice <= displayProjects.size()) {
            BTOProject selected = displayProjects.get(choice - 1);
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

        if (applicant.getCurrentApplication() != null) {
            System.out.println("\nYou already have an active application for another project.");
            System.out.println("Would you like to view your current application? (Y/N): ");
            String choice = scanner.nextLine();
            
            if (choice.equalsIgnoreCase("Y")) {
                viewMyApplication();
            }
        } else {
            System.out.print("\nWould you like to apply for this project? (Y/N): ");
            String choice = scanner.nextLine();
            
            if (choice.equalsIgnoreCase("Y")) {
                applyForProject(project);
            }
        }
    }

    protected void applyForProject(BTOProject project) {
        // Check if user already has an active application
        if (applicant.getCurrentApplication() != null) {
            System.out.println("You already have an active application. You cannot apply for multiple projects.");
            return;
        }

        // Check age and marital status requirements
        if (applicant.getMaritalStatus() == MaritalStatus.SINGLE && applicant.getAge() < 35) {
            System.out.println("As a single applicant, you must be 35 years or older to apply.");
            return;
        } else if (applicant.getMaritalStatus() == MaritalStatus.MARRIED && applicant.getAge() < 21) {
            System.out.println("As a married applicant, you must be 21 years or older to apply.");
            return;
        }

        System.out.println("\nSelect Flat Type:");
        List<FlatType> availableTypes = new ArrayList<>();
        
        for (FlatType type : project.getFlatUnits().keySet()) {
            // Single applicants can only apply for 2-room flats
            if (applicant.getMaritalStatus() == MaritalStatus.SINGLE && type != FlatType.TWO_ROOM) {
                continue;
            }
            // Married applicants can apply for all flat types
            availableTypes.add(type);
            System.out.printf("%d. %s%n", availableTypes.size(), type.getDisplayName());
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
            BTOProject project = projects.get(i);
            if (applicant instanceof HDBOfficer) {
                HDBOfficer officer = (HDBOfficer) applicant;
                if (officer.getAssignedProject() != null && officer.getAssignedProject().equals(project)) {
                    continue;
                }
            }
            System.out.printf("%d. %s%n", i + 1, project.getProjectName());
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
        if (applicant instanceof HDBOfficer) {
            HDBOfficer officer = (HDBOfficer) applicant;
            if (officer.getAssignedProject() != null && officer.getAssignedProject().equals(selected)) {
                System.out.println("You cannot create an enquiry for your assigned project.");
                return;
            }
        }

        System.out.print("Enter your enquiry: ");
        String content = scanner.nextLine();

        if (enquiryManager.createEnquiry(applicant, selected, content) != null) {
            System.out.println("Enquiry created successfully!");
        } else {
            System.out.println("Failed to create enquiry.");
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

    protected void generateReceipt() {
        BTOApplication application = applicant.getCurrentApplication();
        if (application == null) {
            System.out.println("You have no active application.");
            return;
        }

        if (application.getStatus() != ApplicationStatus.BOOKED) {
            System.out.println("Can only generate receipts for booked applications.");
            return;
        }

        // Generate receipt content
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== HDB Flat Booking Receipt ===\n\n");
        receipt.append("Applicant Details:\n");
        receipt.append("Name: ").append(applicant.getName()).append("\n");
        receipt.append("NRIC: ").append(applicant.getNric()).append("\n");
        receipt.append("Age: ").append(applicant.getAge()).append("\n");
        receipt.append("Marital Status: ").append(applicant.getMaritalStatus()).append("\n\n");
        
        receipt.append("Project Details:\n");
        receipt.append("Project Name: ").append(application.getProject().getProjectName()).append("\n");
        receipt.append("Location: ").append(application.getProject().getNeighborhood()).append("\n");
        receipt.append("Flat Type: ").append(application.getSelectedFlatType().getDisplayName()).append("\n\n");
        
        receipt.append("Booking Details:\n");
        receipt.append("Booking Date: ").append(application.getApplicationDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))).append("\n");
        receipt.append("Status: ").append(application.getStatus()).append("\n\n");
        
        receipt.append("Please bring this receipt to your appointment with the HDB officer.\n");
        receipt.append("This receipt serves as proof of your flat booking.");

        // Create output directory
        File outputDir = new File("output_applicant");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Generate filename
        String fileName = "receipt_" + applicant.getNric() + "_" + 
                         application.getProject().getProjectName().replace(" ", "_") + ".txt";
        File receiptFile = new File(outputDir, fileName);

        // Save receipt to file
        try (PrintWriter writer = new PrintWriter(new FileWriter(receiptFile))) {
            writer.println(receipt.toString());
            System.out.println("\nReceipt has been generated and saved as: " + receiptFile.getAbsolutePath());
            System.out.println("Please bring this receipt to your appointment with the HDB officer.");
        } catch (IOException e) {
            System.out.println("Failed to generate receipt file.");
            e.printStackTrace();
        }
    }

    private void register() {
        System.out.print("Enter NRIC: ");
        String nric = scanner.nextLine();

        if (!nric.matches("^[ST]\\d{7}[A-Z]$")) {
            System.out.println("Invalid NRIC format. Must be S/T followed by 7 digits and a letter.");
            return;
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Enter age: ");
        int age = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Select marital status:");
        System.out.println("1. Single");
        System.out.println("2. Married");
        int maritalChoice = scanner.nextInt();
        scanner.nextLine();

        MaritalStatus maritalStatus = (maritalChoice == 1) ? MaritalStatus.SINGLE : MaritalStatus.MARRIED;

        System.out.print("Enter name: ");
        String name = scanner.nextLine();

        if (userManager.register(nric, password, age, maritalStatus, UserType.APPLICANT, name)) {
            System.out.println("Registration successful! Please login.");
        } else {
            System.out.println("Registration failed. NRIC might already be registered.");
        }
    }
}