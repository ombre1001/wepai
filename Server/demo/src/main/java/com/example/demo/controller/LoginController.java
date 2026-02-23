package com.example.demo.controller;

import com.example.demo.data.dto.AuthDTO;
import com.example.demo.data.vo.Result;
import com.example.demo.service.LoginService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/auth")
public class LoginController {

    @Resource
    private LoginService loginService;

    @PostMapping("/register")
    public ResponseEntity<Result> register(@RequestBody AuthDTO registerReqDTO) {
        return loginService.register(registerReqDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<Result> login(@RequestBody AuthDTO loginReqDTO) {
        return loginService.login(loginReqDTO);
    }

}
