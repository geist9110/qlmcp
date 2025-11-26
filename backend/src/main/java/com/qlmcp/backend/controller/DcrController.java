package com.qlmcp.backend.controller;

import com.qlmcp.backend.dto.ClientRegistrationDto;
import com.qlmcp.backend.service.DcrService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dcr")
public class DcrController {

    private final DcrService dcrService;

    @PostMapping("/register")
    public ResponseEntity<ClientRegistrationDto.Response> registerClient(
            @RequestBody ClientRegistrationDto.Request request) {
        return ResponseEntity.ok(dcrService.registerClient(ClientRegistrationDto.toCommand(request)));
    }
}
