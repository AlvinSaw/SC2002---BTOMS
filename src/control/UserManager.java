/**
 * This package contains the control classes for managing the business logic of the BTO Management System.
 */
package control;

import entity.*;
import enums.*;
import java.util.*;
import java.io.*;

/**
 * UserManager class handles the management of users in the BTO Management System.
 */
public class UserManager {
    private static UserManager instance;
    private Map<String, User> users;
    private User currentUser;

    private UserManager() {
        users = new HashMap<>();
        loadUsers();
    }

    /**
     * Returns the singleton instance of UserManager.
     * @return The UserManager instance
     */
    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    /**
     * Loads users from the database file.
     */
    private void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader("database/users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String nric = parts[0];
                String password = parts[1];
                int age = Integer.parseInt(parts[2]);
                MaritalStatus maritalStatus = MaritalStatus.valueOf(parts[3]);
                UserType userType = UserType.valueOf(parts[4]);

                User user;
                switch (userType) {
                    case APPLICANT:
                        user = new Applicant(nric, password, age, maritalStatus);
                        break;
                    case HDB_OFFICER:
                        user = new HDBOfficer(nric, password, age, maritalStatus);
                        break;
                    case HDB_MANAGER:
                        user = new HDBManager(nric, password, age, maritalStatus);
                        break;
                    default:
                        continue;
                }
                users.put(nric, user);
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    /**
     * Logs in a user with the given NRIC and password.
     * @param nric The NRIC of the user
     * @param password The password of the user
     * @return True if login is successful, otherwise false
     */
    public boolean login(String nric, String password) {
        User user = users.get(nric);
        if (user != null && user.validatePassword(password)) {
            currentUser = user;
            return true;
        }
        return false;
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        currentUser = null;
    }

    /**
     * Changes the password for the current user.
     * @param oldPassword The current password
     * @param newPassword The new password
     * @return True if the password is changed successfully
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        if (currentUser != null && currentUser.validatePassword(oldPassword)) {
            currentUser.setPassword(newPassword);
            saveUsers();
            return true;
        }
        return false;
    }

    /**
     * Retrieves the currently logged-in user.
     * @return The current User
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Retrieves a user by their NRIC.
     * @param nric The NRIC of the user
     * @return The User if found, otherwise null
     */
    public User getUser(String nric) {
        return users.get(nric);
    }

    /**
     * Saves all users to the database file.
     */
    private void saveUsers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("database/users.txt"))) {
            for (User user : users.values()) {
                writer.println(String.format("%s,%s,%d,%s,%s",
                    user.getNric(),
                    user.getPassword(),
                    user.getAge(),
                    user.getMaritalStatus(),
                    user.getUserType()));
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }
}
