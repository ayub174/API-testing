package com.apitesting.controller;

import com.apitesting.model.Book;
import com.apitesting.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // GET /api/books - Hämta alla böcker, med valfria query-parametrar för filtrering och sortering
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllBooks(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false, defaultValue = "id") String sortBy) {

        List<Book> books = bookService.findAll(genre, author, minPrice, maxPrice, sortBy);

        Map<String, Object> response = new HashMap<>();
        response.put("count", books.size());
        response.put("books", books);
        return ResponseEntity.ok(response);
    }

    // GET /api/books/{id} - Hämta en specifik bok
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        Book book = bookService.findById(id);
        return ResponseEntity.ok(book);
    }

    // POST /api/books - Skapa en ny bok
    @PostMapping
    public ResponseEntity<Book> createBook(@Valid @RequestBody Book book) {
        Book created = bookService.save(book);
        URI location = URI.create("/api/books/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    // PUT /api/books/{id} - Ersätt en bok komplett
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @Valid @RequestBody Book book) {
        Book updated = bookService.update(id, book);
        return ResponseEntity.ok(updated);
    }

    // PATCH /api/books/{id} - Partiell uppdatering
    @PatchMapping("/{id}")
    public ResponseEntity<Book> partialUpdateBook(@PathVariable Long id, @RequestBody Book book) {
        Book updated = bookService.partialUpdate(id, book);
        return ResponseEntity.ok(updated);
    }

    // DELETE /api/books/{id} - Ta bort en bok
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/books/count - Räkna alla böcker
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> count() {
        Map<String, Long> response = new HashMap<>();
        response.put("count", bookService.count());
        return ResponseEntity.ok(response);
    }

    // GET /api/books/search - Sök en bok via titel
    @GetMapping("/search")
    public ResponseEntity<?> searchByTitle(@RequestParam String title) {
        return bookService.findByTitle(title)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Ingen bok hittades med titeln: " + title);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
                });
    }
}
