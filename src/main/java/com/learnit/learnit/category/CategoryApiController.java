package com.learnit.learnit.category;
import com.learnit.learnit.category.CategoryDTO;
import com.learnit.learnit.category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryApiController {

    private final CategoryService categoryService;

    @GetMapping("/api/categories")
    public List<CategoryDTO> list() {
        return categoryService.getCategoryList();
    }
}