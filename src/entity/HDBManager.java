package entity;

import enums.MaritalStatus;
import enums.UserType;
import entity.interfaces.IProjectManageable;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class HDBManager extends User implements IProjectManageable {
    private List<BTOProject> managedProjects;
    private BTOProject currentProject;

    public HDBManager(String nric, String password, int age, MaritalStatus maritalStatus, String name) {
        super(nric, password, age, maritalStatus, UserType.HDB_MANAGER, name);
        this.managedProjects = new ArrayList<>();
        this.currentProject = null;
    }

    @Override
    public List<BTOProject> getManagedProjects() {
        return new ArrayList<>(managedProjects);
    }

    public void addCreatedProject(BTOProject project) {
        this.managedProjects.add(project);
    }
    
    /**
     * Removes a project from the manager's list of managed projects
     * @param project The project to remove
     * @return true if the project was removed, false if it wasn't in the list
     */
    public boolean removeCreatedProject(BTOProject project) {
        return this.managedProjects.remove(project);
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
        return managedProjects.contains(project);
    }

    @Override
    public boolean isProjectNameUnique(String projectName) {
        for (BTOProject project : managedProjects) {
            if (project.getProjectName().equals(projectName)) {
                return false;
            }
        }
        return true;
    }

    public boolean canHandleNewProject(BTOProject newProject) {
        for (BTOProject project : managedProjects) {
            if (project.isApplicationPeriodOverlapping(newProject)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public UserType getUserType() {
        return UserType.HDB_MANAGER;
    }
}