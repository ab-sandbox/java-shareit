package ru.practicum.shareit.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class BaseClient {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected ResponseEntity<Object> get(String path) {
        return send(HttpMethod.GET, path, null, null, null);
    }

    protected ResponseEntity<Object> get(String path, long userId) {
        return send(HttpMethod.GET, path, userId, null, null);
    }

    protected ResponseEntity<Object> get(String path, Long userId,
                                         Map<String, Object> parameters) {
        return send(HttpMethod.GET, path, userId, parameters, null);
    }

    protected <T> ResponseEntity<Object> post(String path, T body) {
        return send(HttpMethod.POST, path, null, null, body);
    }

    protected <T> ResponseEntity<Object> post(String path, long userId, T body) {
        return send(HttpMethod.POST, path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> post(String path, Long userId,
                                              Map<String, Object> parameters, T body) {
        return send(HttpMethod.POST, path, userId, parameters, body);
    }

    protected <T> ResponseEntity<Object> put(String path, long userId, T body) {
        return send(HttpMethod.PUT, path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> put(String path, long userId,
                                             Map<String, Object> parameters, T body) {
        return send(HttpMethod.PUT, path, userId, parameters, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, T body) {
        return send(HttpMethod.PATCH, path, null, null, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, long userId) {
        return send(HttpMethod.PATCH, path, userId, null, null);
    }

    protected <T> ResponseEntity<Object> patch(String path, long userId, T body) {
        return send(HttpMethod.PATCH, path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, Long userId,
                                               Map<String, Object> parameters, T body) {
        return send(HttpMethod.PATCH, path, userId, parameters, body);
    }

    protected ResponseEntity<Object> delete(String path) {
        return send(HttpMethod.DELETE, path, null, null, null);
    }

    protected ResponseEntity<Object> delete(String path, long userId) {
        return send(HttpMethod.DELETE, path, userId, null, null);
    }

    protected ResponseEntity<Object> delete(String path, Long userId,
                                            Map<String, Object> parameters) {
        return send(HttpMethod.DELETE, path, userId, parameters, null);
    }

    private ResponseEntity<Object> send(
            HttpMethod method,
            String path,
            Long userId,
            Map<String, Object> parameters,
            Object body) {

        HttpEntity<Object> request = new HttpEntity<>(body, createHeaders(userId));

        try {
            ResponseEntity<Object> response = rest.exchange(
                    path,
                    method,
                    request,
                    Object.class,
                    parameters != null ? parameters : Map.of()
            );

            return ResponseEntity
                    .status(response.getStatusCode())
                    .body(response.getBody());
        } catch (HttpStatusCodeException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());
        }
    }

    private HttpHeaders createHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        if (userId != null) {
            headers.set(USER_ID_HEADER, userId.toString());
        }

        return headers;
    }
}
