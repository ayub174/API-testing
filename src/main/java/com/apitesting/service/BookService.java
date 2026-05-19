package com.apitesting.service;

import com.apitesting.exception.ResourceNotFoundException;
import com.apitesting.model.Book;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final ConcurrentHashMap<Long, Book> books = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @PostConstruct
    public void initData() {
        save(new Book(null, "Java Programming", "James Gosling", 299.0, 15, "Programming"));
        save(new Book(null, "Clean Code", "Robert C. Martin", 349.0, 8, "Programming"));
        save(new Book(null, "Sagan om ringen", "J.R.R. Tolkien", 199.0, 25, "Fantasy"));
        save(new Book(null, "1984", "George Orwell", 149.0, 0, "Fiction"));
        save(new Book(null, "Sapiens", "Yuval Noah Harari", 249.0, 12, "Non-fiction"));
    }

    public List<Book> findAll(String genre, String author, Double minPrice, Double maxPrice, String sortBy) {
        List<Book> result = new ArrayList<>(books.values());

        if (genre != null && !genre.isBlank()) {
            result = result.stream()
                    .filter(b -> b.getGenre() != null && b.getGenre().equalsIgnoreCase(genre))
                    .collect(Collectors.toList());
        }

        if (author != null && !author.isBlank()) {
            result = result.stream()
                    .filter(b -> b.getAuthor() != null && b.getAuthor().toLowerCase().contains(author.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (minPrice != null) {
            result = result.stream()
                    .filter(b -> b.getPrice() >= minPrice)
                    .collect(Collectors.toList());
        }

        if (maxPrice != null) {
            result = result.stream()
                    .filter(b -> b.getPrice() <= maxPrice)
                    .collect(Collectors.toList());
        }

        if (sortBy != null) {
            switch (sortBy.toLowerCase()) {
                case "price" -> result.sort(Comparator.comparing(Book::getPrice));
                case "title" -> result.sort(Comparator.comparing(Book::getTitle));
                case "author" -> result.sort(Comparator.comparing(Book::getAuthor));
                default -> result.sort(Comparator.comparing(Book::getId));
            }
        }

        return result;
    }

    public Book findById(Long id) {
        Book book = books.get(id);
        if (book == null) {
            throw new ResourceNotFoundException("Boken med id " + id + " hittades inte");
        }
        return book;
    }

    public Book save(Book book) {
        if (book.getId() == null) {
            book.setId(idCounter.getAndIncrement());
        } else if (book.getId() >= idCounter.get()) {
            idCounter.set(book.getId() + 1);
        }
        books.put(book.getId(), book);
        return book;
    }

    public Book update(Long id, Book updated) {
        Book existing = findById(id);
        existing.setTitle(updated.getTitle());
        existing.setAuthor(updated.getAuthor());
        existing.setPrice(updated.getPrice());
        existing.setStock(updated.getStock());
        existing.setGenre(updated.getGenre());
        return existing;
    }

    public Book partialUpdate(Long id, Book updates) {
        Book existing = findById(id);
        if (updates.getTitle() != null) existing.setTitle(updates.getTitle());
        if (updates.getAuthor() != null) existing.setAuthor(updates.getAuthor());
        if (updates.getPrice() != null) existing.setPrice(updates.getPrice());
        if (updates.getStock() != null) existing.setStock(updates.getStock());
        if (updates.getGenre() != null) existing.setGenre(updates.getGenre());
        return existing;
    }

    public void delete(Long id) {
        if (!books.containsKey(id)) {
            throw new ResourceNotFoundException("Boken med id " + id + " hittades inte");
        }
        books.remove(id);
    }

    public Optional<Book> findByTitle(String title) {
        return books.values().stream()
                .filter(b -> b.getTitle().equalsIgnoreCase(title))
                .findFirst();
    }

    public long count() {
        return books.size();
    }
}
