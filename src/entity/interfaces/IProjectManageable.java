package entity.interfaces;

import entity.BTOProject;
import java.util.List;

public interface IProjectManageable {
    List<BTOProject> getManagedProjects();
    boolean canCreateProject(BTOProject project);
    boolean canUpdateProjectVisibility(BTOProject project);
    boolean isProjectNameUnique(String projectName);
} 