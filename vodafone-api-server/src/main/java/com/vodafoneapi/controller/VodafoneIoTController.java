package com.vodafoneapi.controller;

import com.vodafoneapi.dto.VodafoneIoTResponse;
import com.vodafoneapi.service.VodafoneIoTService;
import com.vodafoneapi.utils.IoTFIleRequest;
import com.vodafoneapi.utils.VodafoneIoTConstants;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;


@Log4j2
@RestController
public class VodafoneIoTController {

    @Autowired
    private VodafoneIoTService vodafoneIoTService;

    @ApiOperation(value = "To batch loading the data provided in the csv file.")
    @PostMapping(value = VodafoneIoTConstants.LOAD_IOT_EVENTS_FILE_PATH)
    public ResponseEntity loadIoTEventsFromFile(@RequestBody IoTFIleRequest request){
        return vodafoneIoTService.loadIoTEventsFromFile(request);
    }

    @ApiOperation(value = "To get the Customer details based on Product Id and timestamp")
    @GetMapping(value =VodafoneIoTConstants.LOAD_IOT_EVENTS_FILE_PATH)
    public ResponseEntity<VodafoneIoTResponse> getIotUserDetail(@NotEmpty @RequestParam(name = "productId") String productId,
                                                @RequestParam(name = "tstmp", required = false) Long tstmp){
        return vodafoneIoTService.getIotUserDetail(productId,tstmp);
    }


}
