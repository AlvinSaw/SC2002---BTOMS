package entity.interfaces;

import entity.BTOApplication;
import enums.ApplicationStatus;
import java.util.List;

public interface IApplicationManageable {
    List<BTOApplication> getApplications();
    boolean canUpdateApplicationStatus(BTOApplication application, ApplicationStatus newStatus);
    boolean canGenerateReceipt(BTOApplication application);
} 