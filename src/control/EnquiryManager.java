package control;

import entity.*;
import java.util.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EnquiryManager {
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

    public List<Enquiry> getEnquiriesForProject(String projectName) {
        List<Enquiry> projectEnquiries = new ArrayList<>();
        for (Enquiry enquiry : enquiries) {
            if (enquiry.getProject().getProjectName().equals(projectName)) {
                projectEnquiries.add(enquiry);
            }
        }
        return projectEnquiries;
    }

    public List<Enquiry> getEnquiriesForUser(String nric) {
        List<Enquiry> userEnquiries = new ArrayList<>();
        for (Enquiry enquiry : enquiries) {
            if (enquiry.getCreator().getNric().equals(nric)) {
                userEnquiries.add(enquiry);
            }
        }
        return userEnquiries;
    }

    public Enquiry getEnquiry(String id) {
        for (Enquiry enquiry : enquiries) {
            if (enquiry.getId().equals(id)) {
                return enquiry;
            }
        }
        return null;
    }

    public Enquiry createEnquiry(User creator, BTOProject project, String content) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Enquiry enquiry = new Enquiry(id, creator, project, content);
        enquiries.add(enquiry);
        project.addEnquiry(enquiry);
        saveEnquiries();
        return enquiry;
    }

    public boolean updateEnquiry(String id, String content, User user) {
        Enquiry enquiry = getEnquiry(id);
        if (enquiry != null && enquiry.canEdit(user)) {
            enquiry.setContent(content);
            saveEnquiries();
            return true;
        }
        return false;
    }

    public boolean deleteEnquiry(String id, User user) {
        Enquiry enquiry = getEnquiry(id);
        if (enquiry != null && enquiry.canEdit(user) && !enquiry.hasReply()) {
            enquiries.remove(enquiry);
            saveEnquiries();
            return true;
        }
        return false;
    }

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