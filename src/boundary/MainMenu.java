/**
 * This package contains the boundary classes for the BTO Management System.
 */
package boundary;

import control.*;
import entity.*;
import java.util.Scanner;
import java.io.*;

/**
 * MainMenu class provides the main entry point and user interface for the BTO Management System.
 */
public class MainMenu {
    private static Scanner scanner = new Scanner(System.in);
    private UserManager userManager;

    /**
     * Constructor for MainMenu.
     * Initializes the UserManager instance.
     */
    public MainMenu() {
        userManager = UserManager.getInstance();
    }

    /**
     * Starts the main menu loop for the application.
     */
    public void start() {
        while (true) {
            if (userManager.getCurrentUser() == null) {
                showLoginMenu();
            } else {
                showMainMenu();
            }
        }
    }

    /**
     * Displays the login menu for the user.
     */
    private void showLoginMenu() {
        System.out.println("\n=== BTO Management System ===");
        System.out.println("1. Login");
        System.out.println("2. Exit");
        System.out.print("Choose an option: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                login();
                break;
            case 2:
                System.out.println("Thank you for using BTO Management System!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    /**
     * Handles the login process for the user.
     */
    private void login() {
        System.out.print("Enter NRIC: ");
        String nric = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        
        if (userManager.login(nric, password)) {
            System.out.println("Login successful!");
        } else {
            System.out.println("Invalid credentials. Please try again.");
        }
    }

    /**
     * Displays the main menu based on the current user's role.
     */
    private void showMainMenu() {
        User currentUser = userManager.getCurrentUser();
        
        if (currentUser instanceof Applicant) {
            new ApplicantMenu((Applicant) currentUser).show();
        } else if (currentUser instanceof HDBOfficer) {
            new HDBOfficerMenu((HDBOfficer) currentUser).show();
        } else if (currentUser instanceof HDBManager) {
            new HDBManagerMenu((HDBManager) currentUser).show();
        }
    }

    /**
     * Main method to initialize the database and start the application.
     * @param args Command-line arguments
     */
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
