package control;

import entity.*;
import enums.*;
import interfaces.*;
import util.SystemLogger;
import java.util.*;
import java.io.*;

public class UserManager implements IUserManager {
    private static UserManager instance;
    private Map<String, User> users;
    private User currentUser;

    private UserManager() {
        users = new HashMap<>();
        loadUsers();
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    private void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader("database/users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                User user = null;
                UserType userType = UserType.valueOf(parts[4]);
                
                if (userType == UserType.APPLICANT) {
                    user = new Applicant(parts[0], "", Integer.parseInt(parts[2]), 
                        MaritalStatus.valueOf(parts[3]), parts[5]);
                } else if (userType == UserType.HDB_OFFICER) {
                    user = new HDBOfficer(parts[0], parts[1], Integer.parseInt(parts[2]), 
                        MaritalStatus.valueOf(parts[3]),
                        parts[5]);
                } else if (userType == UserType.HDB_MANAGER) {
                    user = new HDBManager(parts[0], parts[1], Integer.parseInt(parts[2]), 
                        MaritalStatus.valueOf(parts[3]),
                        parts[5]);
                }
                if (user != null) {
                    user.setPassword(parts[1]);
                    users.put(parts[0], user);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    @Override
    public boolean login(String nric, String password) {
        User user = users.get(nric);
        if (user != null && user.validatePassword(password)) {
            currentUser = user;
            // Log the successful login
            SystemLogger.logLogin(user.getNric(), user.getName(), user.getUserType().toString());
            return true;
        }
        return false;
    }

    @Override
    public void logout() {
        if (currentUser != null) {
            // Log the logout event before setting currentUser to null
            SystemLogger.logLogout(currentUser.getNric(), currentUser.getName(), currentUser.getUserType().toString());
        }
        currentUser = null;
    }

    @Override
    public boolean changePassword(String oldPassword, String newPassword) {
        if (currentUser == null) {
            return false;
        }

        if (!currentUser.validatePassword(oldPassword)) {
            return false;
        }

        // Check if new password is the same as the old password
        if (oldPassword.equals(newPassword)) {
            return false;
        }
        
        // Check password constraints: minimum 6 characters with at least 1 alphabet and 1 number
        if (newPassword.length() < 6) {
            return false;
        }
        
        boolean hasLetter = false;
        boolean hasDigit = false;
        
        for (char c : newPassword.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
            
            // If both conditions are met, we can break early
            if (hasLetter && hasDigit) {
                break;
            }
        }
        
        // If either condition is not met, reject the password
        if (!hasLetter || !hasDigit) {
            return false;
        }

        currentUser.setPassword(newPassword, true);
        saveUsers();
        return true;
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public User getUser(String nric) {
        return users.get(nric);
    }

    @Override
    public void saveUsers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("database/users.txt"))) {
            for (User user : users.values()) {
                if (user instanceof Applicant) {
                    writer.printf("%s,%s,%d,%s,%s,%s%n",
                            user.getNric(), user.getPassword(), user.getAge(),
                            user.getMaritalStatus(), user.getUserType(), user.getName());
                } else {
                    writer.printf("%s,%s,%d,%s,%s,%s%n",
                            user.getNric(), user.getPassword(), user.getAge(),
                            user.getMaritalStatus(), user.getUserType(), user.getName());
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }
}
