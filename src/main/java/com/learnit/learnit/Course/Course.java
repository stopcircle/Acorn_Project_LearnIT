package com.learnit.learnit.Course;

import lombok.Data;

@Data
public class Course {
    private int courseId;
    private String title;
    private int price;
    private String status;
}
