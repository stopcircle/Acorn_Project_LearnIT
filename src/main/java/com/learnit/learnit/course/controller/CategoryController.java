package com.learnit.learnit.course.controller;

import com.learnit.learnit.course.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class CategoryController {
    @Autowired
    CategoryService categoryService;

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        model.addAttribute("categories", categoryService.getCategoryList());
    }

}
