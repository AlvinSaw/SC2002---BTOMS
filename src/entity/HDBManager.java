package entity;

import enums.MaritalStatus;
import enums.UserType;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class HDBManager extends User{
    private List<BTOProject> managedProjects;

    public HDBManager(String nric, String password, int age, MaritalStatus maritalStatus, String name) {
        super(nric, password, age, maritalStatus, UserType.HDB_MANAGER, name);
        this.managedProjects = new ArrayList<>();
    }

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