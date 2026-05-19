package com.apitesting.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class Book {

    private Long id;

    @NotBlank(message = "Titel får inte vara tom")
    @Size(min = 1, max = 200, message = "Titel måste vara mellan 1 och 200 tecken")
    private String title;

    @NotBlank(message = "Författare får inte vara tom")
    private String author;

    @NotNull(message = "Pris får inte vara null")
    @Min(value = 0, message = "Pris måste vara större än eller lika med 0")
    private Double price;

    @Min(value = 0, message = "Lagersaldo kan inte vara negativt")
    private Integer stock;

    private String genre;

    private Boolean available;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public Book() {
    }

    public Book(Long id, String title, String author, Double price, Integer stock, String genre) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.price = price;
        this.stock = stock;
        this.genre = genre;
        this.available = stock != null && stock > 0;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
        this.available = stock != null && stock > 0;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
