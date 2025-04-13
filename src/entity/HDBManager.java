package entity;

import enums.MaritalStatus;
import enums.UserType;
import java.util.ArrayList;
import java.util.List;

public class HDBManager extends User {
    private List<BTOProject> createdProjects;
    private BTOProject currentProject;

    public HDBManager(String nric, String password, int age, MaritalStatus maritalStatus) {
        super(nric, password, age, maritalStatus, UserType.HDB_MANAGER);
        this.createdProjects = new ArrayList<>();
        this.currentProject = null;
    }

    public List<BTOProject> getCreatedProjects() {
        return new ArrayList<>(createdProjects);
    }

    public void addCreatedProject(BTOProject project) {
        this.createdProjects.add(project);
    }

    public BTOProject getCurrentProject() {
        return currentProject;
    }

    public void setCurrentProject(BTOProject project) {
        this.currentProject = project;
    }

    public boolean canHandleNewProject(BTOProject newProject) {
        if (currentProject == null) return true;
        
        return !currentProject.isApplicationPeriodOverlapping(newProject);
    }
} 