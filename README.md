# BTO Management System

This is a command line application for managing HDB BTO projects. The system allows applicants to view and apply for BTO projects, HDB officers to process applications and inquiries, and HDB managers to manage projects and generate reports.

## Features

### Applicant Features
- View a list of available projects
- Apply for a BTO project
- View application status
- Withdraw an application
- Create and manage inquiries

### HDB Officer Features
- Sign up to join a project team
- View and respond to project inquiries
- Process application status
- Process withdrawal requests
- Process bookings

### HDB Manager Features
- Create, edit and delete BTO projects
- Manage project visibility
- View all projects
- Approve HDB officer registrations
- Generate reports

## Project Structure
```
.
├── src/
│   ├── boundary
│   ├── control
│   ├── entity
│   └── enums
├── database
│   ├── users.txt
│   ├── projects.txt
│   ├── applications.txt
│   └── enquiries.txt
└── README.md
```

## Data Format

### users.txt
```
NRIC,password,age,marital_status,user_type
```

### projects.txt
```
project_name|neighborhood|flat_units|open_date|close_date|manager_nric|visibility|officers
```

### applications.txt
```
applicant_nric|project_name|flat_type|status|withdrawal_requested
```

### enquiries.txt
```
id|creator_nric|project_name|content|reply
```

## Run Program
```bash
java src.boundary.MainMenu
```
