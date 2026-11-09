package com.starkvms.vmsbackend.api.controller;

import com.starkvms.vmsbackend.api.dto.DatabaseHealthResponse;
import com.starkvms.vmsbackend.application.service.DatabaseConnectivityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class DatabaseHealthController {

    private final DatabaseConnectivityService databaseConnectivityService;

    public DatabaseHealthController(DatabaseConnectivityService databaseConnectivityService) {
        this.databaseConnectivityService = databaseConnectivityService;
    }

    @GetMapping("/database")
    public ResponseEntity<DatabaseHealthResponse> checkDatabase() {
        boolean available = databaseConnectivityService.isAvailable();
        DatabaseHealthResponse response = new DatabaseHealthResponse(
                available ? "UP" : "DOWN",
                available ? "UP" : "DOWN");
        return ResponseEntity.status(available ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}
