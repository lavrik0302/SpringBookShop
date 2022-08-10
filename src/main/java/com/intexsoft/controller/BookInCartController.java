package com.intexsoft.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller
public class BookInCartController {
    @GetMapping("/bookInCart")
    public void readCart(){

    }
    @PostMapping("/bookInCart")
    public void addBookToCart(){

    }
    @PutMapping("/bookInCart")
    public void updateBookCount(){

    }
    @DeleteMapping("/bookInCart")
    public void deleteBookFromCart(){

    }
}
