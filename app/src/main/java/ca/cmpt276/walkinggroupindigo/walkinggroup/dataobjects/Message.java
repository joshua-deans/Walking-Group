package ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects;

public class Message extends IdItemBase {
    // message has been stored
    private String text;
    // message send to User
    private User toUser;
    // message send from the User
    private User fromUser;
    // indicate message has read
    private boolean read;
    // indicate message is emergency
    private boolean emergency;

    public Message(String text, User toUser, User fromUser, boolean read, boolean emergency) {
        this.text = text;
        this.toUser = toUser;
        this.fromUser = fromUser;
        this.read = read;
        this.emergency = emergency;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getToUser() {
        return toUser;
    }

    public void setToUser(User toUser) {
        this.toUser = toUser;
    }

    public User getFromUser() {
        return fromUser;
    }

    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isEmergency() {
        return emergency;
    }

    public void setEmergency(boolean emergency) {
        this.emergency = emergency;
    }
}