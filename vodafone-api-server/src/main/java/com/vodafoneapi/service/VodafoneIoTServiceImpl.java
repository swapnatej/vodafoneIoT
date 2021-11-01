package com.vodafoneapi.service;

import com.vodafoneapi.dto.VodafoneIoTResponse;
import com.vodafoneapi.entity.VodafoneIoT;
import com.vodafoneapi.repository.VodafoneIoTRepository;
import com.vodafoneapi.utils.IoTFIleRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.vodafoneapi.utils.VodafoneIoTConstants.*;

@Log4j2
@Service
public class VodafoneIoTServiceImpl implements VodafoneIoTService{
    @Autowired
    private VodafoneIoTRepository vodafoneIoTRepository;

    /***
     * To batch loading the data provided in the csv file.
     * @param IoTFIleRequest
     */
    @Override
    public ResponseEntity loadIoTEventsFromFile(IoTFIleRequest request)  {
        log.info("started loading event file...");
        String csvFilePath = request.getFilePath();
        try (BufferedReader lineReader = new BufferedReader(new FileReader(csvFilePath));){
        String lineText = null;
        // skip header line
        lineReader.readLine();
        List<VodafoneIoT> iotDataList = new ArrayList<>();
        while ((lineText = lineReader.readLine()) != null) {
            String[] data = lineText.split(",");
            iotDataList.add(getStringToVodafoneIoTMapping(data));
        }
        vodafoneIoTRepository.saveAll(iotDataList);
        } catch (FileNotFoundException e) {
            log.error(NO_DATA_FILE_FOUND_EXCEPTION);
            log.error(e.getMessage());
            return new ResponseEntity<>(NO_DATA_FILE_FOUND_EXCEPTION, HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            log.error(TECHNICAL_EXCEPTION);
            log.error(e.getMessage());
            return new ResponseEntity<>(TECHNICAL_EXCEPTION, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        log.info(DATA_REFRESHED);
        return new ResponseEntity<>(DATA_REFRESHED,HttpStatus.OK);
    }

    /***
     * Return the Customer details based on Product Id and timestamp
     * @param productId
     * @param tstmp
     * @return ResponseEntity<VodafoneIoTResponse>
     */
    @Override
    public ResponseEntity<VodafoneIoTResponse> getIotUserDetail(String productId, Long tstmp) {
        log.info("Getting details of ID {}",productId);
        LocalDateTime dateTime = null;
        if(tstmp == null) {
            dateTime = LocalDateTime.now();
        }else{
            dateTime = Instant.ofEpochMilli(tstmp)
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        log.info("date time is {}",dateTime);
            List<VodafoneIoT> iotDataList = vodafoneIoTRepository.findByIdAndDateTimeList(productId,dateTime);
            if(ObjectUtils.isEmpty(iotDataList)){
                throw new NoSuchElementException("ERROR: Id "+ productId +" not found");
            }
            if(productId.contains(CYCLE_PLUS_TRACKER_CODE) ) {
                if(iotDataList.size() < 3)
                    return  mapVodafoneIoTResponse(iotDataList.get(0), STATUS_NA);
                else if (iotDataList.get(0).getLongitude().equals(iotDataList.get(1).getLongitude()) && iotDataList.get(1).getLongitude().equals(iotDataList.get(2).getLongitude())
                        && iotDataList.get(0).getLatitude().equals(iotDataList.get(1).getLatitude()) && iotDataList.get(1).getLatitude().equals(iotDataList.get(2).getLatitude())) {
                    return mapVodafoneIoTResponse(iotDataList.get(0),STATUS_INACTIVE);
                }
            }
            return mapVodafoneIoTResponse(iotDataList.get(0),null);
    }

    /***
     * Maps the expected response from the entity details
     * @param iotData
     * @param status
     * @return ResponseEntity<VodafoneIoTResponse>
     */
    public ResponseEntity<VodafoneIoTResponse> mapVodafoneIoTResponse(VodafoneIoT iotData, String status ){
        if(!iotData.getAirplaneMode() && iotData.getLongitude() == null && iotData.getLatitude() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, DEVICE_COULD_NOT_BE_LOCATED_EXCEPTION);
        }
        VodafoneIoTResponse response = new VodafoneIoTResponse();
        response.setId(iotData.getProductId());
        response.setDatetime(iotData.getDateTime());
        response.setLongitude(iotData.getLongitude() == null ? null : iotData.getLongitude());
        response.setLatitude(iotData.getLatitude() == null ? null : iotData.getLatitude());
        response.setBattery(getBatteryStatus(iotData.getBattery()));
        response.setName(iotData.getProductId().contains(CYCLE_PLUS_TRACKER_CODE)? CYCLE_PLUS_TRACKER : GENERAL_TRACKER);
        response.setStatus(status != null ? status : iotData.getLatitude() != null && iotData.getLongitude() != null ? STATUS_ACTIVE : STATUS_INACTIVE);
        response.setDescription(!iotData.getAirplaneMode() ? LOCATION_IDENTIFIED : LOCATION_NOT_AVAILABLE);
        return new ResponseEntity<VodafoneIoTResponse>(response,HttpStatus.OK);
    }

    /**
     * To derive the Battery Status from the Battery percentage
     * @param battery
     * @return String
     */
    public String getBatteryStatus(Float battery) {
        if(battery >= 98F){
            return BATTERY_FULL;
        }else if(battery < 98F && battery >= 60F){
            return BATTERY_HIGH;
        }else if(battery < 60F && battery >= 40F){
            return BATTERY_MEDIUM;
        }else if(battery < 40F && battery >= 10F){
            return BATTERY_LOW;
        }else{
            return BATTERY_CRITICAL;
        }
    }

    /**
     * Convert CSV line into VodafoneIot Object
     * @param data
     * @return VodafoneIoT
     */
    public VodafoneIoT getStringToVodafoneIoTMapping(String[] data) {
        VodafoneIoT iot = new VodafoneIoT();
        iot.setDateTime(Instant.ofEpochMilli(Long.parseLong(data[0])).atZone(ZoneId.systemDefault()).toLocalDateTime());
        iot.setEventId(Long.parseLong(data[1]));
        iot.setProductId(data[2]);
        iot.setLatitude((data[3]).isEmpty() ?  null : Float.parseFloat(data[3]));
        iot.setLongitude((data[4]).isEmpty() ? null : Float.parseFloat(data[4]));
        iot.setBattery((data[5]).isEmpty() ? null : Float.parseFloat(data[5])*100);
        iot.setLight(data[6]);
        iot.setAirplaneMode(getBooleanValue(data[7]));
        return iot;
    }

    /**
     * get Boolean value for Off and On text as False and True respectively
     * @param s
     * @return Boolean value
     */
    public Boolean getBooleanValue(String s){
        return !s.isEmpty() && s.equalsIgnoreCase(OFF) ? Boolean.FALSE : Boolean.TRUE;
    }
}
