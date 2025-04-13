package control;

import entity.*;
import enums.*;
import java.util.*;
import java.io.*;

public class UserManager {
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
                String nric = parts[0];
                String hashedPassword = parts[1];
                int age = Integer.parseInt(parts[2]);
                MaritalStatus maritalStatus = MaritalStatus.valueOf(parts[3]);
                UserType userType = UserType.valueOf(parts[4]);

                User user;
                switch (userType) {
                    case APPLICANT:
                        user = new Applicant(nric, "", age, maritalStatus);
                        break;
                    case HDB_OFFICER:
                        user = new HDBOfficer(nric, "", age, maritalStatus);
                        break;
                    case HDB_MANAGER:
                        user = new HDBManager(nric, "", age, maritalStatus);
                        break;
                    default:
                        continue;
                }
                user.setPassword(hashedPassword);
                users.put(nric, user);
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    public boolean login(String nric, String password) {
        User user = users.get(nric);
        if (user != null && user.validatePassword(password)) {
            currentUser = user;
            return true;
        }
        return false;
    }

    public void logout() {
        currentUser = null;
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        if (currentUser != null && currentUser.validatePassword(oldPassword)) {
            String hashedNewPassword = PasswordHasher.hashPassword(newPassword);
            currentUser.setPassword(hashedNewPassword);
            saveUsers();
            return true;
        }
        return false;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public User getUser(String nric) {
        return users.get(nric);
    }

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