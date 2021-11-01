package com.vodafoneapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "vodafone_iot")
public class VodafoneIoT {
    @Id
    @Column(name = "event_id", nullable = false)
    private Long EventId;
    @Column(name = "date_time")
    private LocalDateTime DateTime;
    @Column(name = "product_id")
    private String ProductId;
    @Column(name = "latitude")
    private Float Latitude;
    @Column(name = "longitude")
    private Float Longitude;
    @Column(name = "battery")
    private Float Battery;
    @Column(name = "light")
    private String Light;
    @Column(name = "airplane_mode")
    private Boolean AirplaneMode;
}
