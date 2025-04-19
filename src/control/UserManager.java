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
                    user = new HDBManager(parts[0], "", Integer.parseInt(parts[2]), 
                        MaritalStatus.valueOf(parts[3]));
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

    public boolean register(String nric, String password, int age, MaritalStatus maritalStatus, UserType userType, String name) {
        if (users.containsKey(nric)) {
            return false;
        }

        User user = null;
        if (userType == UserType.APPLICANT) {
            user = new Applicant(nric, "", age, maritalStatus, name);
        } else if (userType == UserType.HDB_OFFICER) {
            user = new HDBOfficer(nric, "", age, maritalStatus, name);
        } else if (userType == UserType.HDB_MANAGER) {
            user = new HDBManager(nric, "", age, maritalStatus);
            user.setName(name);
        }

        if (user != null) {
            user.setPassword(password, true);
            users.put(nric, user);
            saveUsers();
            return true;
        }
        return false;
    }
} 