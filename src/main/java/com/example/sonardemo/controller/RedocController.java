package com.example.sonardemo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import org.springframework.util.StreamUtils;

@RestController
@RequestMapping("/api")
public class RedocController {

    @Value("classpath:static/redoc.html")
    private Resource redocHtml;

    @GetMapping(value = "/docs", produces = MediaType.TEXT_HTML_VALUE)
    public String getRedocDocumentation() throws IOException {
        return StreamUtils.copyToString(redocHtml.getInputStream(), StandardCharsets.UTF_8);
    }
}