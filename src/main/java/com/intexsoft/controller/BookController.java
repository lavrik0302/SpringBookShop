package com.intexsoft.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller
public class BookController {
    @GetMapping("/book")
       public void readBook(){

    }
    @PostMapping("/book")
    public void createBook(){

    }
    @PutMapping("/book")
    public void updateBook(){

    }
    @DeleteMapping("/book")
    public void deleteBook(){

    }
}
