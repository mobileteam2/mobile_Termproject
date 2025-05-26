package com.example.mobile_termproject.Notification;

public class NotificationItem {
    public String packageName;
    public long postTime;
    public String title;
    public String subText;
    public String text;
    public String bigText;
    public String infoText;
    public String titleBig;

    public NotificationItem(String packageName, long postTime, String title, String subText,
                            String text, String bigText, String infoText, String titleBig) {
        this.packageName = packageName;
        this.postTime = postTime;
        this.title = title;
        this.subText = subText;
        this.text = text;
        this.bigText = bigText;
        this.infoText = infoText;
        this.titleBig = titleBig;
    }
}
