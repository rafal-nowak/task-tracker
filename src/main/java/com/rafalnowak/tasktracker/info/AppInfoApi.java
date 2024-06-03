package com.rafalnowak.tasktracker.info;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/info")
@RequiredArgsConstructor
public class AppInfoApi {
    private final AppInfoProperties appInfoProperties;

    @GetMapping
    public AppInfoProperties getAppInfo() {
        return appInfoProperties;
    }
}
