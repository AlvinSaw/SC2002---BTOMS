package interfaces;

import entity.BTOApplication;
import entity.BTOProject;
import entity.Applicant;
import entity.HDBOfficer;
import enums.ApplicationStatus;
import enums.FlatType;
import java.util.List;

public interface IApplicationManager {
    List<BTOApplication> getApplicationsForProject(String projectName);
    boolean createApplication(Applicant applicant, BTOProject project, FlatType flatType);
    boolean updateApplicationStatus(BTOApplication application, ApplicationStatus newStatus);
    boolean requestWithdrawal(BTOApplication application);
    boolean approveWithdrawal(BTOApplication application);
    boolean rejectWithdrawal(BTOApplication application);
    boolean bookFlatWithType(BTOApplication application, FlatType selectedFlatType);
    String generateReceipt(BTOApplication application, HDBOfficer officer);
    void saveApplications();
} 