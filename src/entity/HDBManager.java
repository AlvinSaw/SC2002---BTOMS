/**
 * This package contains the entity classes representing the core data models of the BTO Management System.
 */
package entity;

import enums.MaritalStatus;
import enums.UserType;
import java.util.ArrayList;
import java.util.List;

/**
 * HDBManager class represents an HDB Manager in the BTO Management System.
 */
public class HDBManager extends User {
    private List<BTOProject> createdProjects;
    private BTOProject currentProject;

    /**
     * Constructor for HDBManager.
     * @param nric The NRIC of the manager
     * @param password The password of the manager
     * @param age The age of the manager
     * @param maritalStatus The marital status of the manager
     */
    public HDBManager(String nric, String password, int age, MaritalStatus maritalStatus) {
        super(nric, password, age, maritalStatus, UserType.HDB_MANAGER);
        this.createdProjects = new ArrayList<>();
        this.currentProject = null;
    }

    /**
     * Retrieves the list of projects created by the manager.
     * @return A list of BTOProjects created by the manager
     */
    public List<BTOProject> getCreatedProjects() {
        return new ArrayList<>(createdProjects);
    }

    /**
     * Adds a project to the list of projects created by the manager.
     * @param project The BTOProject to add
     */
    public void addCreatedProject(BTOProject project) {
        this.createdProjects.add(project);
    }

    /**
     * Retrieves the current project being handled by the manager.
     * @return The current BTOProject
     */
    public BTOProject getCurrentProject() {
        return currentProject;
    }

    /**
     * Sets the current project being handled by the manager.
     * @param project The BTOProject to set as current
     */
    public void setCurrentProject(BTOProject project) {
        this.currentProject = project;
    }

    /**
     * Checks if the manager can handle a new project without overlapping application periods.
     * @param newProject The new BTOProject to check
     * @return True if the manager can handle the new project, otherwise false
     */
    public boolean canHandleNewProject(BTOProject newProject) {
        if (currentProject == null) return true;
        
        return !currentProject.isApplicationPeriodOverlapping(newProject);
    }
}
