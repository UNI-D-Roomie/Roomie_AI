package com.example.Roomie_AI;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;

@RestController
public class GPTController {

    @Autowired
    private GPTService gptService;

    @PostMapping("/compare-images")
    public CompareResponseDto compareImages(@RequestBody CompareRequestDto request) {
        try {
            return gptService.compareImages(request.getCompareType(), request.getBeforeUrl(), request.getAfterUrl());
        } catch (IOException e) {
            // Return an error response if there's an IOException
            return new CompareResponseDto(0.0, "Error processing images: " + e.getMessage());
        }
    }
}

