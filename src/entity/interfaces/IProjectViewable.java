package entity.interfaces;

import entity.BTOProject;
import java.util.List;

public interface IProjectViewable {
    List<BTOProject> getViewableProjects();
    boolean canViewProject(BTOProject project);
} 