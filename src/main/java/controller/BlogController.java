package controller;

import Services.BlogServices;
import Entities.Blog;
import java.util.List;
import java.util.stream.Collectors;

public class BlogController {
    private BlogServices blogServices;

    public BlogController() {
        try {
            System.out.println("Initializing BlogController...");
            blogServices = new BlogServices();
            System.out.println("BlogController initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing BlogController: " + e.getMessage());
            throw new RuntimeException("Failed to initialize BlogController: " + e.getMessage());
        }
    }

    public void addBlog(Blog blog) {
        blogServices.add(blog);
    }

    public void updateBlog(Blog blog) {
        blogServices.update(blog);
    }

    public void deleteBlog(Blog blog) throws Exception {
        try {
            blogServices.delete(blog);
        } catch (RuntimeException e) {
            throw new Exception(e.getMessage());
        }
    }

    public List<Blog> getAllBlogs() {
        return blogServices.getAll();
    }
}
