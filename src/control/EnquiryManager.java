/**
 * This package contains the control classes for managing the business logic of the BTO Management System.
 */
package control;

import entity.*;
import java.util.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * EnquiryManager class handles the management of enquiries in the BTO Management System.
 */
public class EnquiryManager {
    private static EnquiryManager instance;
    private List<Enquiry> enquiries;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private EnquiryManager() {
        enquiries = new ArrayList<>();
        loadEnquiries();
    }

    /**
     * Returns the singleton instance of EnquiryManager.
     * @return The EnquiryManager instance
     */
    public static EnquiryManager getInstance() {
        if (instance == null) {
            instance = new EnquiryManager();
        }
        return instance;
    }

    /**
     * Loads enquiries from the database file.
     */
    private void loadEnquiries() {
        try (BufferedReader reader = new BufferedReader(new FileReader("database/enquiries.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                String id = parts[0];
                User creator = UserManager.getInstance().getUser(parts[1]);
                BTOProject project = ProjectManager.getInstance().getProject(parts[2]);
                String content = parts[3];
                
                Enquiry enquiry = new Enquiry(id, creator, project, content);
                
                if (parts.length > 4 && !parts[4].isEmpty()) {
                    enquiry.addReply(parts[4]);
                }
                
                enquiries.add(enquiry);
                project.addEnquiry(enquiry);
            }
        } catch (IOException e) {
            System.err.println("Error loading enquiries: " + e.getMessage());
        }
    }

    /**
     * Saves all enquiries to the database file.
     */
    public void saveEnquiries() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("database/enquiries.txt"))) {
            for (Enquiry enquiry : enquiries) {
                writer.println(String.format("%s|%s|%s|%s|%s",
                    enquiry.getId(),
                    enquiry.getCreator().getNric(),
                    enquiry.getProject().getProjectName(),
                    enquiry.getContent(),
                    enquiry.getReply() != null ? enquiry.getReply() : ""));
            }
        } catch (IOException e) {
            System.err.println("Error saving enquiries: " + e.getMessage());
        }
    }

    /**
     * Retrieves all enquiries for a specific project.
     * @param projectName The name of the project
     * @return A list of Enquiries for the project
     */
    public List<Enquiry> getEnquiriesForProject(String projectName) {
        List<Enquiry> projectEnquiries = new ArrayList<>();
        for (Enquiry enquiry : enquiries) {
            if (enquiry.getProject().getProjectName().equals(projectName)) {
                projectEnquiries.add(enquiry);
            }
        }
        return projectEnquiries;
    }

    /**
     * Retrieves all enquiries created by a specific user.
     * @param nric The NRIC of the user
     * @return A list of Enquiries created by the user
     */
    public List<Enquiry> getEnquiriesForUser(String nric) {
        List<Enquiry> userEnquiries = new ArrayList<>();
        for (Enquiry enquiry : enquiries) {
            if (enquiry.getCreator().getNric().equals(nric)) {
                userEnquiries.add(enquiry);
            }
        }
        return userEnquiries;
    }

    /**
     * Retrieves an enquiry by its ID.
     * @param id The ID of the enquiry
     * @return The Enquiry if found, otherwise null
     */
    public Enquiry getEnquiry(String id) {
        for (Enquiry enquiry : enquiries) {
            if (enquiry.getId().equals(id)) {
                return enquiry;
            }
        }
        return null;
    }

    /**
     * Creates a new enquiry for a project.
     * @param creator The user creating the enquiry
     * @param project The project the enquiry is about
     * @param content The content of the enquiry
     * @return The created Enquiry
     */
    public Enquiry createEnquiry(User creator, BTOProject project, String content) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Enquiry enquiry = new Enquiry(id, creator, project, content);
        enquiries.add(enquiry);
        project.addEnquiry(enquiry);
        saveEnquiries();
        return enquiry;
    }

    /**
     * Updates the content of an enquiry.
     * @param id The ID of the enquiry to update
     * @param content The new content for the enquiry
     * @param user The user making the update
     * @return True if the update is successful
     */
    public boolean updateEnquiry(String id, String content, User user) {
        Enquiry enquiry = getEnquiry(id);
        if (enquiry != null && enquiry.canEdit(user)) {
            enquiry.setContent(content);
            saveEnquiries();
            return true;
        }
        return false;
    }

    /**
     * Deletes an enquiry.
     * @param id The ID of the enquiry to delete
     * @param user The user requesting the deletion
     * @return True if the deletion is successful
     */
    public boolean deleteEnquiry(String id, User user) {
        Enquiry enquiry = getEnquiry(id);
        if (enquiry != null && enquiry.canEdit(user) && !enquiry.hasReply()) {
            enquiries.remove(enquiry);
            saveEnquiries();
            return true;
        }
        return false;
    }

    /**
     * Adds a reply to an enquiry.
     * @param id The ID of the enquiry to reply to
     * @param reply The reply content
     * @param user The user adding the reply
     * @return True if the reply is added successfully
     */
    public boolean addReply(String id, String reply, User user) {
        Enquiry enquiry = getEnquiry(id);
        if (enquiry != null && 
            (user instanceof HDBOfficer || user instanceof HDBManager) &&
            !enquiry.hasReply()) {
            enquiry.addReply(reply);
            saveEnquiries();
            return true;
        }
        return false;
    }
}
