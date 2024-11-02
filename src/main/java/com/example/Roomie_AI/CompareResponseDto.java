package com.example.Roomie_AI;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompareResponseDto {
    private Double score;
    private String comment;

    public CompareResponseDto(Double score, String comment) {
        this.score = score;
        this.comment = comment;
    }

}
