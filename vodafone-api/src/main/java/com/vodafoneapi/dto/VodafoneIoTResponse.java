package com.vodafoneapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VodafoneIoTResponse {
    private String id;
    private String name;
    private LocalDateTime datetime;
    private Float longitude;
    private Float latitude;
    private String status;
    private String battery;
    private String description;
}
