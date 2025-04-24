package interfaces;

import entity.Enquiry;
import entity.User;
import entity.BTOProject;
import entity.HDBManager;
import java.util.List;

public interface IEnquiryManager {
    List<Enquiry> getEnquiriesForProject(String projectName);
    List<Enquiry> getEnquiriesForUser(String nric);
    Enquiry getEnquiry(String id);
    Enquiry createEnquiry(User creator, BTOProject project, String content);
    boolean updateEnquiry(String id, String content, User user);
    boolean deleteEnquiry(String id, User user);
    boolean addReply(String id, String reply, User user);
    boolean replyToEnquiry(String enquiryId, String reply, HDBManager manager);
    void saveEnquiries();
} 