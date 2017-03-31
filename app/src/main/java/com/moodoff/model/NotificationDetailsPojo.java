package com.moodoff.model;

/**
 * Created by santanu on 29/03/17.
 */

public class NotificationDetailsPojo {
    private String fromUser;
    private String toUser;
    private String songName;
    private String type;
    private boolean sendDone;
    private String timeStamp;

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public boolean isSendDone() {
        return sendDone;
    }

    public void setSendDone(boolean sendDone) {
        this.sendDone = sendDone;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
