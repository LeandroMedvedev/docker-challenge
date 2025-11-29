package com.leandro.docker_challenge;

import jakarta.persistence.*;

@Entity
@Table(name= "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private String status;

    // Construtor vazio (obrigatório JPA)
    public Task() {}

    // Construtor útil
    public Task(String description, String status) {
        this.description = description;
        this.status = status;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
}
