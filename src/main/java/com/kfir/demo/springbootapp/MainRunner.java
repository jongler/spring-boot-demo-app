package com.kfir.demo.springbootapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MainRunner implements CommandLineRunner {

  @Override
  public void run(String... args) {
    System.out.println("Hello from Spring Boot command-line app!");
  }
}
