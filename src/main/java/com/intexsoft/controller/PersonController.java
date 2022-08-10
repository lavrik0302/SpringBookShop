package com.intexsoft.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
@Controller
public class PersonController {
    @GetMapping("/person")
    public void readPersonWithCartAndOrders(){

    }
    @PostMapping("/person")
    public void createPerson(){

    }
    @PutMapping("/person")
    public void updatePerson(){

    }
    @DeleteMapping("/person")
    public void deletePerson(){

    }
}
