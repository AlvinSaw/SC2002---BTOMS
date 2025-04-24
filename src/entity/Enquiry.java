package entity;

import java.time.LocalDateTime;

public class Enquiry {
    private String id;
    private User creator;
    private BTOProject project;
    private String content;
    private String reply;
    private LocalDateTime creationTime;
    private LocalDateTime replyTime;

    public Enquiry(String id, User creator, BTOProject project, String content) {
        this.id = id;
        this.creator = creator;
        this.project = project;
        this.content = content;
        this.reply = null;
        this.creationTime = LocalDateTime.now();
        this.replyTime = null;
    }

    public String getId() { return id; }
    public User getCreator() { return creator; }
    public BTOProject getProject() { return project; }
    public String getContent() { return content; }
    public String getReply() { return reply; }
    public LocalDateTime getCreationTime() { return creationTime; }
    public LocalDateTime getReplyTime() { return replyTime; }

    public void setContent(String content) {
        this.content = content;
    }

    public void addReply(String reply) {
        this.reply = reply;
        this.replyTime = LocalDateTime.now();
    }

    public boolean canEdit(User user) {
        return creator.getNric().equals(user.getNric());
    }

    public boolean hasReply() {
        return reply != null;
    }
} 