package com.banksystem.webbasedbankingsystem.dto;

public class LoanDocumentDTO {
    private Long id;
    private String category;
    private String fileName;
    private String fileType;

    public LoanDocumentDTO() {}

    public LoanDocumentDTO(Long id, String category, String fileName, String fileType) {
        this.id = id;
        this.category = category;
        this.fileName = fileName;
        this.fileType = fileType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
}