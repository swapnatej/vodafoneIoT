package com.vodafoneapi.service;

import com.vodafoneapi.utils.IoTFIleRequest;
import com.vodafoneapi.dto.VodafoneIoTResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface VodafoneIoTService {
    ResponseEntity loadIoTEventsFromFile(IoTFIleRequest request);

    ResponseEntity<VodafoneIoTResponse> getIotUserDetail(String productId, Long tstmp);
}
