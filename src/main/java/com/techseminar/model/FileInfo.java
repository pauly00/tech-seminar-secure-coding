package com.techseminar.model;

public class FileInfo {
    private int id;
    private int bbsId;
    private String filename;
    private String filerealname;

    public FileInfo() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBbsId() { return bbsId; }
    public void setBbsId(int bbsId) { this.bbsId = bbsId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getFilerealname() { return filerealname; }
    public void setFilerealname(String filerealname) { this.filerealname = filerealname; }
}
