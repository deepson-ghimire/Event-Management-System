//package com.example.demo.category;
//
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//public class CategoryService {
//
//    @Autowired
//    private CategoryRepository categoryRepository;
//
//    public Category saveCategory(Category category) {
//        return categoryRepository.save(category);
//    }
//
//    public List<Category> getAllCategories() {
//        return categoryRepository.findAll();
//    }
//
//    public Category getCategoryById(Integer id) {
//        return categoryRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Category not found"));
//    }
//
//    public void deleteCategory(Integer id) {
//        categoryRepository.deleteById(id);
//    }
//}
//
