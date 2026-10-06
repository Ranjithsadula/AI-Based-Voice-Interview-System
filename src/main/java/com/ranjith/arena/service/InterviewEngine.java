package com.ranjith.arena.service;
import org.springframework.stereotype.Service; import java.util.*;
@Service public class InterviewEngine {
 public record Question(String round,String text){ }
 public List<Question> plan(String role,String resume){ String r=(resume==null?"":resume).toLowerCase(); List<Question> q=new ArrayList<>();
 q.add(new Question("HR","Good morning. Please introduce yourself and tell me about your technical background."));
 if(r.contains("java")) q.add(new Question("Technical","I can see Java in your resume. Explain the four main OOP principles with a practical Java example.")); else q.add(new Question("Technical","Explain the main programming concepts you are strongest in and where you used them in a project."));
 if(r.contains("hibernate")) q.add(new Question("Technical","You mention Hibernate. Why would you choose Hibernate over writing JDBC code directly, and how does object-relational mapping work?"));
 if(r.contains("spring")) q.add(new Question("Technical","Explain how Spring Boot handles dependency injection. Give an example from a project you built."));
 if(r.contains("mysql")||r.contains("sql")) q.add(new Question("Coding","Write or explain a SQL query to find duplicate records in a table and then explain how you would remove duplicates safely."));
 if(r.contains("parking")) q.add(new Question("Project","You mention a Smart Parking project. Walk me through its architecture, database design, and one improvement you would make."));
 if(r.contains("pharmabot")||r.contains("nlp")) q.add(new Question("Project","Tell me how your PharmaBot works from user query to final response. Why did you use TF-IDF and Logistic Regression?"));
 q.add(new Question("Coding","Suppose you receive an array of integers. Explain an efficient approach to find the first non-repeating element and its time complexity."));
 q.add(new Question("Scenario","A production API suddenly becomes slow after a new release. What steps would you take to identify and fix the problem?"));
 q.add(new Question("HR","Tell me about a difficult problem you faced while building a project. What did you personally do to solve it?"));
 q.add(new Question("HR","Why do you want this role, and what technical skill are you currently improving?"));
 q.add(new Question("Technical","What is the difference between checked and unchecked exceptions in Java? When would you create a custom exception?"));
 q.add(new Question("Coding","Explain how you would design a Java method to validate balanced parentheses using a stack. Mention complexity."));
 q.add(new Question("HR","Do you have any questions for the interviewer?")); return q; }
 public Question adaptive(String previous,String role,String resume,int number){ String a=(previous==null?"":previous).toLowerCase();
 if(a.contains("hibernate")) return new Question("Technical","You mentioned Hibernate. Can you explain lazy versus eager fetching and give a situation where each is useful?");
 if(a.contains("jdbc")) return new Question("Technical","You said JDBC gives more SQL control. How would you manage connections, prepared statements, and transactions safely?");
 if(a.contains("project")) return new Question("Project","What was your exact contribution to that project, and how would you prove that your design works under real users?");
 if(a.contains("team")) return new Question("HR","You mentioned teamwork. Tell me about a disagreement in a team and how you handled it professionally.");
 if(a.contains("java")) return new Question("Technical","You mentioned Java. What happens when an object is created, and what role do constructors and garbage collection play?");
 return new Question("Technical","Can you give a concrete example to support your previous answer and explain the trade-off you considered?"); }
}
