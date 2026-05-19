package com.apitesting.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Endpoints för att öva på olika HTTP-statuskoder, headers och content-types.
 */
@RestController
@RequestMapping("/api/status")
public class StatusController {

    // GET /api/status/health - Healthcheck
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    // GET /api/status/{code} - Returnerar en specifik statuskod
    @GetMapping("/{code}")
    public ResponseEntity<Map<String, Object>> returnStatus(@PathVariable int code) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", code);
        response.put("message", HttpStatus.valueOf(code).getReasonPhrase());
        return ResponseEntity.status(code).body(response);
    }

    // GET /api/status/delay/{seconds} - Fördröjt svar (för timeout-testning)
    @GetMapping("/delay/{seconds}")
    public ResponseEntity<Map<String, Object>> delay(@PathVariable int seconds) throws InterruptedException {
        if (seconds < 0 || seconds > 10) {
            throw new IllegalArgumentException("Antal sekunder måste vara mellan 0 och 10");
        }
        Thread.sleep(seconds * 1000L);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Svar efter " + seconds + " sekunders fördröjning");
        return ResponseEntity.ok(response);
    }

    // GET /api/status/headers - Returnerar request headers (bra för att inspektera vad Postman skickar)
    @GetMapping("/headers")
    public ResponseEntity<Map<String, String>> headers(@RequestHeader Map<String, String> headers) {
        return ResponseEntity.ok(headers);
    }

    // GET /api/status/custom-headers - Returnerar svar med custom response-headers
    @GetMapping("/custom-headers")
    public ResponseEntity<Map<String, String>> customHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Custom-Header", "Hej-från-API:et");
        headers.add("X-Request-Id", "req-" + System.currentTimeMillis());
        headers.add("X-Rate-Limit-Remaining", "99");

        Map<String, String> body = new HashMap<>();
        body.put("message", "Kolla response headers i Postman");
        return ResponseEntity.ok().headers(headers).body(body);
    }

    // GET /api/status/xml - Returnerar XML istället för JSON
    @GetMapping(value = "/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> xmlResponse() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<response>" +
                "<status>OK</status>" +
                "<message>Detta är ett XML-svar</message>" +
                "</response>";
        return ResponseEntity.ok(xml);
    }

    // GET /api/status/text - Returnerar plain text
    @GetMapping(value = "/text", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> textResponse() {
        return ResponseEntity.ok("Detta är ett plain text-svar");
    }

    // POST /api/status/echo - Returnerar precis det som skickades in (bra för debug)
    @PostMapping("/echo")
    public ResponseEntity<Map<String, Object>> echo(@RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        response.put("received", body);
        response.put("size", body == null ? 0 : body.size());
        return ResponseEntity.ok(response);
    }
}
