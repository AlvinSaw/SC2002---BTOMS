package boundary;

import control.*;
import entity.*;
import interfaces.*;
import java.util.Scanner;
import java.io.*;
import java.util.Arrays;

public class MainMenu {
    private static Scanner scanner = new Scanner(System.in);
    private IUserManager userManager;
    private IProjectManager projectManager;

    public MainMenu() {
        userManager = UserManager.getInstance();
        projectManager = ProjectManager.getInstance();
    }

    public void start() {
        while (true) {
            if (userManager.getCurrentUser() == null) {
                showLoginMenu();
            } else {
                showMainMenu();
            }
        }
    }

    private void showLoginMenu() {
        while (true) {
            System.out.println("\n=== BTO Management System ===");
            System.out.println("1. Login");
            System.out.println("2. Exit");
            System.out.print("Choose an option: ");
            
            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Clear the newline character
                
                switch (choice) {
                    case 1:
                        login();
                        return;
                    case 2:
                        System.out.println("Thank you for using BTO Management System!");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid option. Please enter 1 or 2.");
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number (1 or 2).");
                scanner.nextLine(); // Clear invalid input
            }
        }
    }

    private void login() {
        System.out.print("Enter NRIC: ");
        String nric = scanner.nextLine();
        
        // Validate NRIC format before proceeding
        if (!isValidNRIC(nric)) {
            System.out.println("Invalid NRIC format. NRIC should be in the format 'S1234567A'.");
            return;
        }
        
        String password = readPassword();
        
        if (userManager.login(nric, password)) {
            User currentUser = userManager.getCurrentUser();
            
            // Auto-publish eligible projects when a user logs in
            projectManager.autoPublishProjects();
            
            System.out.println("\n=== Welcome to BTO Management System ===");
            System.out.println("Welcome, " + currentUser.getName() + "!");
            System.out.println("Role: " + currentUser.getUserType());
            System.out.println("=====================================");
        } else {
            System.out.println("Invalid credentials. Please try again.");
        }
    }

    // Helper method to read password securely without displaying it
    private String readPassword() {
        Console console = System.console();
        if (console != null) {
            // If console is available, use it to read password securely
            char[] passwordChars = console.readPassword("Enter password: ");
            String password = new String(passwordChars);
            Arrays.fill(passwordChars, ' '); // Clear the password from memory
            return password;
        } else {
            // Fallback for environments where console is not available (e.g., some IDEs)
            System.out.print("Enter password: ");
            return scanner.nextLine();
        }
    }

    // Helper method to validate NRIC format
    private boolean isValidNRIC(String nric) {
        // Singapore NRIC format: S/T followed by 7 digits and ending with a letter
        if (nric == null || nric.length() != 9) return false;
        
        char first = nric.charAt(0);
        char last = nric.charAt(8);
        
        // First character must be 'S' or 'T'
        // Last character must be a letter
        // Middle 7 characters must be digits
        return (first == 'S' || first == 'T') && 
               Character.isLetter(last) &&
               nric.substring(1, 8).matches("\\d{7}");
    }

    private void showMainMenu() {
        User currentUser = userManager.getCurrentUser();
        
        if (currentUser instanceof HDBManager) {
            new HDBManagerMenu((HDBManager) currentUser).show();
        } else if (currentUser instanceof HDBOfficer) {
            new HDBOfficerMenu((HDBOfficer) currentUser).show();
        } else if (currentUser instanceof Applicant) {
            new ApplicantMenu((Applicant) currentUser).show();
        }
    }

    public static void main(String[] args) {
        new File("database").mkdirs();
        
        String[] files = {"users.txt", "projects.txt", "applications.txt", "enquiries.txt"};
        for (String file : files) {
            try {
                new File("database/" + file).createNewFile();
            } catch (IOException e) {
                System.err.println("Error creating database file: " + e.getMessage());
            }
        }
        
        new MainMenu().start();
    }
}