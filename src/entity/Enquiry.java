/**
 * This package contains the entity classes representing the core data models of the BTO Management System.
 */
package entity;

import java.time.LocalDateTime;

/**
 * Enquiry class represents an enquiry made by a user for a BTO project.
 */
public class Enquiry {
    private String id;
    private User creator;
    private BTOProject project;
    private String content;
    private String reply;
    private LocalDateTime creationTime;
    private LocalDateTime replyTime;

    /**
     * Constructor for Enquiry.
     * @param id The unique ID of the enquiry
     * @param creator The user who created the enquiry
     * @param project The BTO project the enquiry is about
     * @param content The content of the enquiry
     */
    public Enquiry(String id, User creator, BTOProject project, String content) {
        this.id = id;
        this.creator = creator;
        this.project = project;
        this.content = content;
        this.reply = null;
        this.creationTime = LocalDateTime.now();
        this.replyTime = null;
    }

    /**
     * Retrieves the unique ID of the enquiry.
     * @return The enquiry ID
     */
    public String getId() { return id; }

    /**
     * Retrieves the user who created the enquiry.
     * @return The creator of the enquiry
     */
    public User getCreator() { return creator; }

    /**
     * Retrieves the BTO project the enquiry is about.
     * @return The BTOProject
     */
    public BTOProject getProject() { return project; }

    /**
     * Retrieves the content of the enquiry.
     * @return The enquiry content
     */
    public String getContent() { return content; }

    /**
     * Retrieves the reply to the enquiry.
     * @return The reply content, or null if no reply exists
     */
    public String getReply() { return reply; }

    /**
     * Retrieves the creation time of the enquiry.
     * @return The creation time
     */
    public LocalDateTime getCreationTime() { return creationTime; }

    /**
     * Retrieves the reply time of the enquiry.
     * @return The reply time, or null if no reply exists
     */
    public LocalDateTime getReplyTime() { return replyTime; }

    /**
     * Updates the content of the enquiry.
     * @param content The new content for the enquiry
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Adds a reply to the enquiry.
     * @param reply The reply content
     */
    public void addReply(String reply) {
        this.reply = reply;
        this.replyTime = LocalDateTime.now();
    }

    /**
     * Checks if the user can edit the enquiry.
     * @param user The user attempting to edit the enquiry
     * @return True if the user can edit, otherwise false
     */
    public boolean canEdit(User user) {
        return creator.getNric().equals(user.getNric());
    }

    /**
     * Checks if the enquiry has a reply.
     * @return True if the enquiry has a reply, otherwise false
     */
    public boolean hasReply() {
        return reply != null;
    }
}
