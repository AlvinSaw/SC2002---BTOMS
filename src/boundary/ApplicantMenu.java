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
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number (1-7).");
                scanner.nextLine(); // Clear invalid input
            }
        }
    }

    protected void viewAvailableProjects() {
        List<BTOProject> projects = projectManager.getVisibleProjectsForUser(applicant);
        if (projects.isEmpty()) {
            System.out.println("No available projects found.");
            return;
        }

        // Current application for status check
        BTOApplication currentApplication = applicant.getCurrentApplication();
        String currentProjectName = null;
        
        // If applicant has UNSUCCESSFUL or WITHDRAWN application, note the current project name to exclude it
        if (currentApplication != null && 
            (currentApplication.getStatus() == ApplicationStatus.UNSUCCESSFUL || 
             currentApplication.getStatus() == ApplicationStatus.WITHDRAWN)) {
            currentProjectName = currentApplication.getProject().getProjectName();
        }

        // 过滤出符合条件的项目
        List<BTOProject> eligibleProjects = new ArrayList<>();
        for (BTOProject project : projects) {
            // 检查项目是否对用户可见
            if (!project.isVisible()) {
                continue;
            }
            
            // Skip the project that the applicant has an unsuccessful or withdrawn application for
            if (currentProjectName != null && project.getProjectName().equals(currentProjectName)) {
                continue;
            }

            // 检查用户年龄和婚姻状态
            if (applicant.getMaritalStatus() == MaritalStatus.SINGLE) {
                // 单身人士必须35岁以上
                if (applicant.getAge() < 35) {
                    continue;
                }
                // 单身人士只能申请2房式
                if (!project.getFlatUnits().containsKey(FlatType.TWO_ROOM)) {
                    continue;
                }
            } else {
                // 已婚人士必须21岁以上
                if (applicant.getAge() < 21) {
                    continue;
                }
            }

            eligibleProjects.add(project);
        }

        if (eligibleProjects.isEmpty()) {
            System.out.println("No available projects found that match your eligibility criteria.");
            
            // Special message if they have an unsuccessful/withdrawn application
            if (currentProjectName != null) {
                System.out.println("Note: The project you previously applied for (" + currentProjectName + 
                                   ") has been excluded from the list.");
            }
            
            if (applicant.getMaritalStatus() == MaritalStatus.SINGLE && applicant.getAge() < 35) {
                System.out.println("As a single applicant, you must be 35 years or older to apply.");
            } else if (applicant.getMaritalStatus() == MaritalStatus.MARRIED && applicant.getAge() < 21) {
                System.out.println("As a married applicant, you must be 21 years or older to apply.");
            }
            return;
        }

        // 显示排序/筛选选项
        System.out.println("\nSort/Filter Options:");
        System.out.println("1. Sort by Neighborhood (A-Z)");
        System.out.println("2. Sort by Available Units (High to Low)");
        System.out.println("3. Filter by Neighborhood");
        System.out.println("4. View All Projects");
        System.out.print("Enter your choice: ");
        
        int sortChoice = 4; // Default to view all projects
        try {
            sortChoice = scanner.nextInt();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Showing all projects by default.");
            scanner.nextLine(); // Clear the invalid input
        }

        List<BTOProject> displayProjects = new ArrayList<>(eligibleProjects);

        switch (sortChoice) {
            case 1:
                // 按地区名称排序
                displayProjects.sort(Comparator.comparing(BTOProject::getNeighborhood));
                break;
            case 2:
                // 按可用单位总数排序
                displayProjects.sort((p1, p2) -> {
                    int total1 = p1.getRemainingUnits().values().stream().mapToInt(Integer::intValue).sum();
                    int total2 = p2.getRemainingUnits().values().stream().mapToInt(Integer::intValue).sum();
                    return Integer.compare(total2, total1); // 降序排序
                });
                break;
            case 3:
                // 按地区筛选
                System.out.print("Enter neighborhood to filter (leave empty to cancel): ");
                String filterNeighborhood = scanner.nextLine().trim();
                if (!filterNeighborhood.isEmpty()) {
                    displayProjects.removeIf(p -> !p.getNeighborhood().equalsIgnoreCase(filterNeighborhood));
                }
                break;
            case 4:
                // 保持原顺序
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
        int choice = 0; // Default to go back
        try {
            choice = scanner.nextInt();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Returning to the previous menu.");
            scanner.nextLine(); // Clear the invalid input
            return;
        }

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

        BTOApplication currentApplication = applicant.getCurrentApplication();
        
        // Only consider application as "active" if it's not UNSUCCESSFUL or WITHDRAWN
        boolean hasActiveApplication = currentApplication != null && 
            currentApplication.getStatus() != ApplicationStatus.UNSUCCESSFUL && 
            currentApplication.getStatus() != ApplicationStatus.WITHDRAWN;
        
        if (hasActiveApplication) {
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
        // Check if the applicant already has an application
        BTOApplication currentApplication = applicant.getCurrentApplication();
        
        // If applicant has a current application, check if it's active
        if (currentApplication != null) {
            // If application is unsuccessful or withdrawn, they can apply for a new project
            if (currentApplication.getStatus() == ApplicationStatus.UNSUCCESSFUL || 
                currentApplication.getStatus() == ApplicationStatus.WITHDRAWN) {
                // Check if they're trying to apply for the same project
                if (currentApplication.getProject().getProjectName().equals(project.getProjectName())) {
                    System.out.println("You cannot apply for a project that you previously had an unsuccessful or withdrawn application for.");
                    return;
                }
                // Otherwise, continue with the application process
            } else {
                // For other statuses (PENDING, SUCCESSFUL, BOOKED), don't allow new application
                System.out.println("You already have an active application. You cannot apply for multiple projects.");
                return;
            }
        }

        // 检查年龄和婚姻状态要求
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
            // 单身人士只能申请2房式
            if (applicant.getMaritalStatus() == MaritalStatus.SINGLE && type != FlatType.TWO_ROOM) {
                continue;
            }
            // 已婚人士可以申请所有房型
            availableTypes.add(type);
            System.out.printf("%d. %s%n", availableTypes.size(), type.getDisplayName());
        }

        if (availableTypes.isEmpty()) {
            System.out.println("No flat types available for your eligibility.");
            return;
        }

        System.out.print("Enter your choice (0 to cancel): ");
        int choice = 0; // Default to cancel
        try {
            choice = scanner.nextInt();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Application cancelled.");
            scanner.nextLine(); // Clear the invalid input
            return;
        }

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

        System.out.println("\n====== MY APPLICATION DETAILS ======");
        System.out.println("Project: " + application.getProject().getProjectName());
        
        // For BOOKED applications, show both original and assigned flat types
        if (application.getStatus() == ApplicationStatus.BOOKED && application.getAssignedFlatType() != null) {
            System.out.println("Originally Applied For: " + application.getSelectedFlatType().getDisplayName());
            System.out.println("-----------------------------------");
            System.out.println("ASSIGNED FLAT TYPE: " + application.getAssignedFlatType().getDisplayName());
            System.out.println("-----------------------------------");
        } else {
            System.out.println("Flat Type: " + application.getSelectedFlatType().getDisplayName());
        }
        
        System.out.println("Status: " + application.getStatus());
        System.out.println("Application Date: " + formattedDate);
        
        // Handle specific status messages and actions
        if (application.getStatus() == ApplicationStatus.UNSUCCESSFUL) {
            System.out.println("\nYour application was unsuccessful.");
            System.out.println("You can apply for other BTO projects by selecting 'View Available Projects' from the main menu.");
            System.out.println("Note: You won't be able to apply for this same project again.");
            return;
        }
        
        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            System.out.println("\nYour application withdrawal has been approved.");
            System.out.println("You can now apply for other BTO projects by selecting 'View Available Projects' from the main menu.");
            System.out.println("Note: You won't be able to apply for this same project again.");
            return;
        }
        
        if (application.isWithdrawalRequested()) {
            System.out.println("\nWithdrawal Request Status: PENDING");
            System.out.println("Your withdrawal request has been submitted and is awaiting approval.");
            System.out.println("Once approved, you will be able to apply for another project.");
            return;
        }

        if (!application.isWithdrawalRequested()) {
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
        int choice = 0; // Default to go back
        try {
            choice = scanner.nextInt();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Returning to the previous menu.");
            scanner.nextLine(); // Clear the invalid input
            return;
        }

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
        
        int choice = 3; // Default to go back
        try {
            choice = scanner.nextInt();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Returning to the previous menu.");
            scanner.nextLine(); // Clear the invalid input
            return;
        }

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
        int projectNum = 0; // Default to go back
        try {
            projectNum = scanner.nextInt();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Returning to the previous menu.");
            scanner.nextLine(); // Clear the invalid input
            return;
        }

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
        System.out.print("Enter new password (minimum 6 characters with at least 1 letter and 1 number): ");
        String newPassword = scanner.nextLine();
        
        if (oldPassword.equals(newPassword)) {
            System.out.println("New password cannot be the same as the current password.");
            return;
        }
        
        if (newPassword.length() < 6) {
            System.out.println("Password must be at least 6 characters long.");
            return;
        }
        
        boolean hasLetter = false;
        boolean hasDigit = false;
        
        for (char c : newPassword.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        
        if (!hasLetter) {
            System.out.println("Password must contain at least one letter.");
            return;
        }
        
        if (!hasDigit) {
            System.out.println("Password must contain at least one number.");
            return;
        }
        
        if (UserManager.getInstance().changePassword(oldPassword, newPassword)) {
            System.out.println("Password changed successfully!");
            // No longer logging out - just return to the menu
        } else {
            System.out.println("Failed to change password. Please check your current password.");
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

        // 生成收据内容
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
        
        // Use assigned flat type if available, otherwise use selected type
        FlatType flatTypeToShow = application.getAssignedFlatType() != null ? 
            application.getAssignedFlatType() : application.getSelectedFlatType();
        receipt.append("Flat Type: ").append(flatTypeToShow.getDisplayName()).append("\n\n");
        
        receipt.append("Booking Details:\n");
        receipt.append("Booking Date: ").append(application.getApplicationDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))).append("\n");
        receipt.append("Status: ").append(application.getStatus()).append("\n\n");
        
        receipt.append("Please bring this receipt to your appointment with the HDB officer.\n");
        receipt.append("This receipt serves as proof of your flat booking.");

        // 创建输出目录
        File outputDir = new File("output_applicant");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // 生成文件名
        String fileName = "receipt_" + applicant.getNric() + "_" + 
                         application.getProject().getProjectName().replace(" ", "_") + ".txt";
        File receiptFile = new File(outputDir, fileName);

        // 保存收据到文件
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