package com.example.Roomie_AI;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompareRequestDto {
    private String compareType;
    private String beforeUrl;
    private String afterUrl;

    public CompareRequestDto(String compareType, String beforeUrl, String afterUrl) {
        this.compareType = compareType;
        this.beforeUrl = beforeUrl;
        this.afterUrl = afterUrl;
    }
}
