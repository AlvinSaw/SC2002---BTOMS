package interfaces;

import entity.User;
import enums.UserType;
import enums.MaritalStatus;

public interface IUserManager {
    boolean login(String nric, String password);
    void logout();
    boolean changePassword(String oldPassword, String newPassword);
    User getCurrentUser();
    User getUser(String nric);
    void saveUsers();
} 