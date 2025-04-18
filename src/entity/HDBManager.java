package entity;

import enums.MaritalStatus;
import enums.UserType;
import entity.interfaces.IProjectManageable;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class HDBManager extends User implements IProjectManageable {
    private List<BTOProject> createdProjects;
    private BTOProject currentProject;

    public HDBManager(String nric, String password, int age, MaritalStatus maritalStatus) {
        super(nric, password, age, maritalStatus, UserType.HDB_MANAGER);
        this.createdProjects = new ArrayList<>();
        this.currentProject = null;
    }

    @Override
    public List<BTOProject> getManagedProjects() {
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

    @Override
    public boolean canCreateProject(BTOProject project) {
        // Check if project name is unique
        if (!isProjectNameUnique(project.getProjectName())) {
            return false;
        }

        // Check if there is an ongoing project
        if (currentProject != null) {
            return false;
        }

        // Check if project dates are valid
        LocalDate now = LocalDate.now();
        return !project.getApplicationOpenDate().isBefore(now) &&
               !project.getApplicationCloseDate().isBefore(project.getApplicationOpenDate());
    }

    @Override
    public boolean canUpdateProjectVisibility(BTOProject project) {
        return createdProjects.contains(project);
    }

    @Override
    public boolean isProjectNameUnique(String projectName) {
        for (BTOProject project : createdProjects) {
            if (project.getProjectName().equals(projectName)) {
                return false;
            }
        }
        return true;
    }

    public boolean canHandleNewProject(BTOProject newProject) {
        if (currentProject == null) return true;
        
        return !currentProject.isApplicationPeriodOverlapping(newProject);
    }
} 