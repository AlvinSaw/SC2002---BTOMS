package utils;

import enums.*;
import java.util.*;
import java.io.*;
import control.*;

public class PasswordMigrationTool {
    public static void main(String[] args) {
        try {
            List<String> userLines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader("database/users.txt.bak"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    userLines.add(line);
                }
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter("database/users.txt"))) {
                for (String line : userLines) {
                    String[] parts = line.split(",");
                    String nric = parts[0];
                    String oldPassword = parts[1];
                    int age = Integer.parseInt(parts[2]);
                    MaritalStatus maritalStatus = MaritalStatus.valueOf(parts[3]);
                    UserType userType = UserType.valueOf(parts[4]);

                    String hashedPassword = PasswordHasher.hashPassword(oldPassword);
                    writer.println(String.format("%s,%s,%d,%s,%s",
                        nric,
                        hashedPassword,
                        age,
                        maritalStatus,
                        userType));
                }
            }

            System.out.println("Password migration completed!");
            System.out.println("Data migrated from users.txt.bak to users.txt");

        } catch (IOException e) {
            System.err.println("Error during migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 