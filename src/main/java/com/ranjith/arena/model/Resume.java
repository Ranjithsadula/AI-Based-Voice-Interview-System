package com.ranjith.arena.model;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="resumes") public class Resume {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; private String candidateName; private String fileName; private String contentType; private LocalDateTime uploadedAt;
 @Lob @Column(columnDefinition="LONGTEXT") private String extractedText;
 public Long getId(){return id;} public String getCandidateName(){return candidateName;} public void setCandidateName(String v){candidateName=v;} public String getFileName(){return fileName;} public void setFileName(String v){fileName=v;} public String getContentType(){return contentType;} public void setContentType(String v){contentType=v;} public LocalDateTime getUploadedAt(){return uploadedAt;} public void setUploadedAt(LocalDateTime v){uploadedAt=v;} public String getExtractedText(){return extractedText;} public void setExtractedText(String v){extractedText=v;}
}
