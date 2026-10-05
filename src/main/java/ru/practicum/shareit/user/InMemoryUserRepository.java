package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1L;

    @Override
    public synchronized User save(User user) {
        if (user.getId() == null) {
            user.setId(nextId++);
        }

        users.put(user.getId(), user);
        return user;
    }

    @Override
    public synchronized Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public synchronized List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public synchronized Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(user -> email.equals(user.getEmail()))
                .findFirst();
    }

    @Override
    public synchronized void deleteById(Long id) {
        users.remove(id);
    }
}
