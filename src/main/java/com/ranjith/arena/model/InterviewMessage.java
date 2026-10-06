package com.ranjith.arena.model;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="interview_messages") public class InterviewMessage {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; private Long sessionId; private String speaker; private String roundType; private int questionNumber; @Lob @Column(columnDefinition="LONGTEXT") private String message; private LocalDateTime createdAt=LocalDateTime.now();
 public Long getId(){return id;} public Long getSessionId(){return sessionId;} public void setSessionId(Long v){sessionId=v;} public String getSpeaker(){return speaker;} public void setSpeaker(String v){speaker=v;} public String getRoundType(){return roundType;} public void setRoundType(String v){roundType=v;} public int getQuestionNumber(){return questionNumber;} public void setQuestionNumber(int v){questionNumber=v;} public String getMessage(){return message;} public void setMessage(String v){message=v;} public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
