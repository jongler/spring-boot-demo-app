package com.kfir.demo.springbootapp.model;

public record PostPayload(String title, String body, int userId, int id) {}