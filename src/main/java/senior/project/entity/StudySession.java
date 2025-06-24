//package senior.project.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//@Entity
//@Table(name = "study_sessions")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class StudySession {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String date;
//    private String startTime;
//    private String endTime;
//
//    private String sessionType;
//
//    @ManyToOne
//    @JoinColumn(name = "user_uid", referencedColumnName = "user_id")
//    private User user;
//
//    @ManyToOne
//    // This is the section that needs to be corrected
//    @JoinColumns({
//            @JoinColumn(name = "term_id"),
//            @JoinColumn(name = "course_code")
//    })
//    private Course course;
//
//    @Column(nullable = true)
//    private String topicName;
//}