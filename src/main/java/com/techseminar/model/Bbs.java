package com.techseminar.model;

import java.time.LocalDateTime;

public class Bbs {
    private int id;
    private int bbsId;
    private String bbsTitle;
    private String bbsUserId;
    private LocalDateTime bbsDate;
    private String bbsContent;
    private boolean isPrivate;

    public Bbs() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBbsId() { return bbsId; }
    public void setBbsId(int bbsId) { this.bbsId = bbsId; }

    public String getBbsTitle() { return bbsTitle; }
    public void setBbsTitle(String bbsTitle) { this.bbsTitle = bbsTitle; }

    public String getBbsUserId() { return bbsUserId; }
    public void setBbsUserId(String bbsUserId) { this.bbsUserId = bbsUserId; }

    public LocalDateTime getBbsDate() { return bbsDate; }
    public void setBbsDate(LocalDateTime bbsDate) { this.bbsDate = bbsDate; }

    public String getBbsContent() { return bbsContent; }
    public void setBbsContent(String bbsContent) { this.bbsContent = bbsContent; }

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }
}
