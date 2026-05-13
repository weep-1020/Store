package com.weep.entity;

public class LargeFile {
    private String pic;
    private String filename;
    private String filePath;
    private Long fileSize;
    private String contentType;

    public LargeFile() {
    }

    public LargeFile(String pic, String filename, String filePath, Long fileSize, String contentType) {
        this.pic = pic;
        this.filename = filename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
    }

    public String getPic() { return pic; }
    public void setPic(String pic) { this.pic = pic; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    @Override
    public String toString() {
        return "LargeFile{" +
                "pic='" + pic + '\'' +
                ", filename='" + filename + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileSize=" + fileSize +
                ", contentType='" + contentType + '\'' +
                '}';
    }
}
