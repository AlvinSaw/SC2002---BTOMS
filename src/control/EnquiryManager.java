package control;

import entity.*;
import interfaces.*;
import java.util.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EnquiryManager implements IEnquiryManager {
    private static EnquiryManager instance;
    private List<Enquiry> enquiries;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private EnquiryManager() {
        enquiries = new ArrayList<>();
        loadEnquiries();
    }

    public static EnquiryManager getInstance() {
        if (instance == null) {
            instance = new EnquiryManager();
        }
        return instance;
    }

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

    @Override
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

    @Override
    public List<Enquiry> getEnquiriesForProject(String projectName) {
        List<Enquiry> projectEnquiries = new ArrayList<>();
        for (Enquiry enquiry : enquiries) {
            if (enquiry.getProject().getProjectName().equals(projectName)) {
                projectEnquiries.add(enquiry);
            }
        }
        return projectEnquiries;
    }

    @Override
    public List<Enquiry> getEnquiriesForUser(String nric) {
        List<Enquiry> userEnquiries = new ArrayList<>();
        for (Enquiry enquiry : enquiries) {
            if (enquiry.getCreator().getNric().equals(nric)) {
                userEnquiries.add(enquiry);
            }
        }
        return userEnquiries;
    }

    @Override
    public Enquiry getEnquiry(String id) {
        for (Enquiry enquiry : enquiries) {
            if (enquiry.getId().equals(id)) {
                return enquiry;
            }
        }
        return null;
    }

    @Override
    public Enquiry createEnquiry(User creator, BTOProject project, String content) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Enquiry enquiry = new Enquiry(id, creator, project, content);
        enquiries.add(enquiry);
        project.addEnquiry(enquiry);
        saveEnquiries();
        return enquiry;
    }

    @Override
    public boolean updateEnquiry(String id, String content, User user) {
        Enquiry enquiry = getEnquiry(id);
        if (enquiry != null && enquiry.canEdit(user)) {
            enquiry.setContent(content);
            saveEnquiries();
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteEnquiry(String id, User user) {
        Enquiry enquiry = getEnquiry(id);
        if (enquiry != null && enquiry.canEdit(user) && !enquiry.hasReply()) {
            enquiries.remove(enquiry);
            saveEnquiries();
            return true;
        }
        return false;
    }

    @Override
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

    /**
     * Formats a datetime for display in reports
     * @param dateTime The datetime to format
     * @return A formatted string representation of the datetime
     */
    public String formatDateTimeForReport(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
    }

    @Override
    public boolean replyToEnquiry(String enquiryId, String reply, HDBManager manager) {
        for (Enquiry enquiry : enquiries) {
            if (enquiry.getId().equals(enquiryId)) {
                if (manager.getManagedProjects().contains(enquiry.getProject())) {
                    enquiry.addReply(reply); // Changed from setReply to addReply to properly set reply time
                    saveEnquiries();
                    return true;
                }
                return false;
            }
        }
        return false;
    }
}