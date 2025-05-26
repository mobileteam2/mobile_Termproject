package com.example.mobile_termproject.Data;

public class Expiration {
    private String frozen;
    private String refrigerated;
    private String room;

    public Expiration() {}

    public Expiration(String frozen, String refrigerated, String room) {
        this.frozen = frozen;
        this.refrigerated = refrigerated;
        this.room = room;
    }

    public String getFrozen() {
        return frozen;
    }

    public void setFrozen(String frozen) {
        this.frozen = frozen;
    }

    public String getRefrigerated() {
        return refrigerated;
    }

    public void setRefrigerated(String refrigerated) {
        this.refrigerated = refrigerated;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}

