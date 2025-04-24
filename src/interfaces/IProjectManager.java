package interfaces;

import entity.BTOProject;
import entity.User;
import enums.FlatType;
import java.util.List;
import java.util.Map;

public interface IProjectManager {
    List<BTOProject> getAllProjects();
    List<BTOProject> getVisibleProjects();
    List<BTOProject> getVisibleProjectsForUser(User user);
    BTOProject getProject(String projectName);
    void addProject(BTOProject project);
    boolean deleteProject(String projectName);
    boolean removeProject(BTOProject project);
    void autoPublishProjects();
    boolean updateRemainingUnits(BTOProject project, FlatType flatType, int booked);
    void saveProjects();
} 