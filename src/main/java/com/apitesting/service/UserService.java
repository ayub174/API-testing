package com.apitesting.service;

import com.apitesting.exception.ResourceNotFoundException;
import com.apitesting.model.User;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private final ConcurrentHashMap<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @PostConstruct
    public void initData() {
        save(new User(null, "Anna Andersson", "anna@example.com", "ADMIN"));
        save(new User(null, "Erik Eriksson", "erik@example.com", "USER"));
        save(new User(null, "Maria Svensson", "maria@example.com", "USER"));
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public User findById(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new ResourceNotFoundException("Användaren med id " + id + " hittades inte");
        }
        return user;
    }

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idCounter.getAndIncrement());
        }
        if (user.getRole() == null) {
            user.setRole("USER");
        }
        users.put(user.getId(), user);
        return user;
    }

    public User update(Long id, User updated) {
        User existing = findById(id);
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        if (updated.getRole() != null) {
            existing.setRole(updated.getRole());
        }
        return existing;
    }

    public void delete(Long id) {
        if (!users.containsKey(id)) {
            throw new ResourceNotFoundException("Användaren med id " + id + " hittades inte");
        }
        users.remove(id);
    }

    public boolean emailExists(String email) {
        return users.values().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }
}
