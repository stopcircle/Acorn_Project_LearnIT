package com.learnit.learnit.course;

import com.learnit.learnit.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class CourseApiController {

    private final CourseService courseService;

    @GetMapping("/api/courses")
    public PageResponse<CourseDTO> list(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "all") String tab,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return courseService.getCoursesPage(categoryId, sort, tab, page, size);
    }

    @GetMapping("/courses/{id}")
    public CourseDTO getCourseById(@PathVariable("id") long id) {
        CourseDTO course = courseService.getCourseById(id);

        if (course == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "강의를 찾을 수 없습니다. id=" + id
            );
        }

        return course;
    }
}
