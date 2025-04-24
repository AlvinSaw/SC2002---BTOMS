# SC2002 - Build-To-Order (BTO) Management System

## AY 24/25 SEM 2 | FDAD GROUP 5

### Declaration of Original Work for SC2002 Assignment 

We hereby declare that the attached group assignment has been researched, undertaken, completed, and submitted as a collective effort by the group members listed below. 
We have honored the principles of academic integrity and have upheld Student Code of Academic Conduct in the completion of this work. 
We understand that if plagiarism is found in the assignment, then lower marks or no marks will be awarded for the assessed work. In addition, disciplinary actions may be taken. 

| NAME | Course | Lab | Signature/Date |
| --- | --- | --- | --- |
| Bazil Boh Zhuang Kai | DSAI | FDAD | Bazil 24-04-25 |
| Saw Yong Xuen | DSAI | FDAD | SYX 24-04-25 |
| Tan Qi En | CS | FDAD | QiEn 24-04-25 |
| Zhou Runhe | CE | FDAD | Runhe 24-04-25 |

## 1.0 Design Overview

### 1.1 Basic Features

#### User Specific Role Based Access
Once logged in, users will be granted access to the distinct features of the system according to the role they were assigned with, these namely being:

**Applicants**
- Mainly only allowed to view the list of available housing projects that they are eligible for
- Apply or withdraw from said projects, and lodge related enquiries

**HDB Officers**
- Have the same access as applicants as long as eligible for making applications
- HDB officers are given administrative privileges to handle flat booking, register to handle for projects and handle applicant enquiries

**HDB Managers**
- Able to create, edit and delete BTO project listings
- Process applications of project handled
- Approve officer's registration to own project

#### Flow of Application and Management
Applicants have an overview detailed description of housing projects and its eligibility criteria for application. Once applicants have applied for the listings, HDB managers are then able to review the applications through this system and decide whether to approve or deny the request. If it is approved, HDB officers can proceed with flat booking. Along with the application, this system will also handle the withdrawals which will be handled by HDB Managers.

#### Enquiry Feature
Allows applicants to submit queries regarding housing projects to the administrative team. The latter, which are both the HDB officers and managers, can then view and respond.

### 1.2 Design Pattern

To enforce the unique responsibilities of each user group, we adopted a protection proxy design pattern along with role based access control where BTOMS acts as the centralised terminal for users to access features that are relevant to them based on their assigned roles.

### 1.3 System Architecture
To ensure the modularity, extensibility and reusability of our code, we applied the system architecture of Model View Controller (MVC). 

- **Model**: Encapsulates core data such as user information, project details and applications records while defining attributes, relationships and domain-specific validation rules
- **View**: Displaying intuitive interfaces for HDB Officer, HDB Manager and applicants
- **Controller**: Handles functional logic such as processing applications, managing projects and user operations

By doing so, we are able to ensure a looser coupling between modules while maintaining high cohesion.

## 2.0 Design Considerations

### 2.1 OO Concepts Applied

#### 2.1.1 Abstraction
Abstraction here lets the rest of the system work at a high level without having to know how that actually happens.

Abstraction is implemented through several ways:
- **Common Base Classes**: We group shared fields and methods into abstract parents like User (NRIC, name, password handling) and ApplicantMenu (scanner setup, common menu options) so that each specific role (Manager, Officer, Applicant) only needs to fill in its own details.
- **Managers as Facade**: We hide the complex processing logic behind high-level calls. The call checks the old application's status, removes it if needed, creates a new BTOApplication, ties it to both the Applicant and the BTOProject, and saves everything to disk, but none of the menu code knows about this.
- **Hiding Internal Logic**: UI classes (menus) never see how data is stored, parsed, or validated—they just call methods like loadProjects() or updateApplicationStatus().
- **Data Abstraction**: Complex structures (a map of flat sizes to counts inside BTOProject) are only exposed through simple methods like getRemainingUnits() or updateRemainingUnits(), so callers work with "units available" rather than juggling maps themselves.

This approach allows the system to work with high-level concepts without needing to know implementation details.

#### 2.1.2 Encapsulation
Since the system involved a huge amount of confidential user data, encapsulation is applied across the whole BTO Management System to ensure data security.

Encapsulation is implemented through several ways:
- **Private attributes**: Sensitive data such as nric, password, age, marital status and name are declared as private variables within the user classes.
- **Controlled access via getters and setters**: In order to access or update these private data, public setter and getter methods need to be called. To enforce the rule that NRIC of users cannot be changed, no setter is currently placed.
- **Information hiding**: Core functional details for internal operations are hidden from the interface. For instance, the password hashing function which the users are not able to see how the hashing function works when changing password.
- **Role-based specific access**: Users under different user types will be having different access to the system data. For instance, all HDB Officers and Applicants can only view but not edit the details of a project while HDB Manager can create, edit and delete the project.
- **Constructor validation**: The constructor encapsulates initialization logic, ensuring all User objects are created in a valid state.

By isolating the data and limiting access, the system minimizes the risk of data leaks or accidental tampering.

#### 2.1.3 Inheritance
To promote code reuse and logical hierarchy within the system, inheritance is applied throughout the BTO Management System to reduce redundancy and improve maintainability.

Inheritance is implemented through several ways:
- **Structured Type Hierarchies**: The hierarchy of inheritance is not just applied to user roles but also to different menus and functionalities.
- **Efficient Code Reuse**: Shared attributes and behaviors are defined once in a base class and inherited by all user types, reducing duplication and streamlining development.
- **Consistent Interfaces with Customized Logic**: Subclasses override base methods to provide role-specific functionality while maintaining a uniform interface across the system.
- **Uniform Handling with Flexibility**: The system processes different user types in a consistent manner—such as during login—while still supporting unique behaviors through overridden methods such as showing different menus.
- **Role-Specific Enhancements**: Individual user roles extend base functionality with specialized features. For instance, HDB Officer inherits from the Applicant to get the base functionality as an applicant.

This strategy enables the reuse of shared functionality and maintain simplicity of code.

#### 2.1.4 Polymorphism
The use of polymorphism in the BTO Management System is designed to allow different user roles and application processes to be handled efficiently while maintaining clear separation of concerns.

Polymorphism implemented in several ways: 
- **Dynamic Method Dispatch**: At runtime, the system determines the actual object type and displays the appropriate menu.
- **Method Overloading**: Multiple versions of methods are called according to different parameters. This is important when it comes to sorting functions to get projects with different parameters.
- **Method Overriding**: Base methods are overridden according to different user roles. For instance, methods to get visible projects will be overridden under each different user class.

By doing so, common methods can be reused across different user roles, minimizing code duplication and promoting better software design practices.

### 2.2 OOD Principles (SOLID)

#### 2.2.1 Single Responsibility Principle [SRP]
Each class in our system has one clear purpose, making the codebase easier to maintain, test, and extend. This is especially clear in our control classes that cater to different core functionalities.

ApplicationManager is the control class for application management. It only handles application-specific operations.

SRP on a wider scale:
- **Controller Layer**: Each control class focus on handling single core processing logic (ApplicationManager, ProjectManager, UserManager)
- **Model Layer**: Each entity classes focus solely on representing data and state (BTOProject, Application, Enquiry)
- **View Layer**: Each menu class focuses on displaying relevant info to the user in different usage scenarios.

#### 2.2.2 Open-Closed Principle [OCP]
Our system allows implementation to add new features down the line without altering the existing code.

For the application status, we can simply extend by adding the enum instead of having to modify the existing code instead of having to modify code in Application Manager. For instance, we can just add in status like Project Deleted.

OCP implementation on a bigger scale:
- **Enum-based configuration**: Entity attributes like UserType, maritalStatus for user, flatType for project and ApplicationStatus for application are enum-based, thus can be easily extended and configuring the enum classes.
- **Role-based Menu**: New menu can be added instead of modifying the existing ones when new role is added in the future such as System Administrator
- **User class hierarchy**: When a new type is added, it can inherit directly from the existing user class to ensure code reusability

We applied OCP to cater for future extensibility and scalability while keeping existing code intact.

#### 2.2.3 Liskov Substitution Principle [LSP]
The Liskov Substitution Principle is upheld in our system by ensuring that subclasses like HDBOfficer and Applicant can be used interchangeably wherever their parent class User is expected, without affecting the correctness of the program.

This ensures the behaviour is consistent between subclasses and parent classes. The child class will also maintain the guarantee of parent classes.

#### 2.2.4 Interface Segregation Principle [ISP]
This is done by implementing interfaces that are simple with specific functions to avoid unintended dependencies and also allow clearer debugging process.

By splitting each responsibility into its own small interface, each interface is focused (a project-handling class only implements IProjectManager), and doesn't have to provide irrelevant methods. Role based interfaces create separate interfaces for each user role while feature segregation allows each feature area to have its own set of interfaces.

#### 2.2.5 Dependency Inversion Principle [DIP]
It states that high-level modules should not depend on low-level modules and both should instead depend on interfaces and abstract classes, this is to allow flexibility and extensibility when changes are to be made, without affecting the core function of our system.

This is closely linked with ISP shown above as the IUserManager simply calls methods on abstractions which are the different interface types like (IProjectManager). This demonstrates the idea of DIP as the Applicant menu (High level module) and Project Manager (low-level module) for example depends on abstractions (interface).

- **High-level module**: ApplicantMenu (the UI logic)
- **Abstractions**: Interfaces IProjectManager, IApplicationManager, IEnquiryManager
- **Low-level modules**: ProjectManager, ApplicationManager, EnquiryManager

### 2.3 Additional Features Implemented:
- Password requirements (minimum of 6 characters, with 1 alphabet and 1 number)
- Password hash function (password is hidden to protect user privacy)
- After status is "booked", applicants are able generate receipt with details in a txt file
- Applicants are able to carry out project filtering
- Officers can view unit availabilities (2-Room and 3-Room) before assigning rooms
- Improved formatting for a clearer UI
- Managers can see additional statistics such as distribution of flat type for application details
- Managers will be prompted to re enter their password for deletion of projects
- Manager is able to create a new project that opens in the future using an auto-publish function. This auto-publish can be toggled on and off after project is open and set to visible
- Utility function such as tableprinter and systemlogger are introduced

### 2.4 Assumptions Made
- Data storage is stored in a text file rather than an actual database system
- Just NRIC and password is sufficient to login to the system, without requiring secondary verification layers like SingPass authentication
- System is developed as a Command Line Interface (CLI) application and will not be extended

## 3.0 UML Diagram

### 3.1 Class Diagram
To view the full picture of the class diagram, see the [Class Diagram](diagrams/Class%20Diagram/Class%20Diagram.png)

### 3.2 Sequence Diagram
To view the full sequence diagram and others, see the [Sequence Diagram](diagrams/Sequence%20Diagram/HDBOfficer_SequenceDiagram.png)

## 4.0 Testing
To see our full test cases table, refer to the [Test Cases](Test%20Case/Test%20Cases.pdf). The additional features we have implemented are at the bottom of the test cases table.

## 5.0 Reflection on Lessons Learned and Challenges Faced

### Reflection
One lesson we took away from this project was the skill of balancing time management and creativity. Although good time management was important to ensure that we could meet the project deadline, we also had to account for a list of creative features that we wanted to include to enhance the functionality of the system. This taught us the importance of having a strong grasp of the relevant concepts taught in this module as key general concepts such as SOLID and OOP principles had a direct impact on the way we carry out the project. Technical knowledge such as UML class and sequence diagrams allowed us to effectively communicate our ideas and brush out any differences. It also highlighted areas of our comprehension that were still lacking, providing us an opportunity to then refine our understanding and address the gaps. Overall, this project enhanced our theoretical knowledge of Java and OOP, and to apply the design and OOP principles we were taught 

### Challenges and How We Conquered Them
Given the scale of this project, it reinforced our attention to details as small mistakes such as stray colons or mistyped variable names can often result in major functionality issues down the line. This made us improve on our documentation and communication practices to maintain consistency of work across the team so as to minimise redundant work and allow us to better spot mistakes. Another challenge was incorporating the SOLID design principles as we are so used to coding without any knowledge of coding principles. Therefore, this taught us to be more aware and stringent in our coding, which will also be useful in future should we need to extend the code to meet various needs

## Project Structure

```
.
├── src/
│   ├── boundary/      # UI and menu classes
│   ├── control/       # Business logic and managers
│   ├── entity/        # Data model classes
│   ├── enums/         # Enumeration types
│   └── util/          # Utility classes
├── database/
│   ├── users.txt      # User data
│   ├── projects.txt   # Project data
│   ├── applications.txt # Application data
│   ├── enquiries.txt  # Enquiry data
│   └── logs/          # System logs
└── diagrams/          # UML diagrams
```

## Run Program
```bash
java -cp . src.boundary.MainMenu
```
