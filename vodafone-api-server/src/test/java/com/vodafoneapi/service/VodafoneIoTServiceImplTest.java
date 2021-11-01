package com.vodafoneapi.service;

import com.vodafoneapi.dto.VodafoneIoTResponse;
import com.vodafoneapi.entity.VodafoneIoT;
import com.vodafoneapi.repository.VodafoneIoTRepository;
import com.vodafoneapi.utils.IoTFIleRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;

import static com.vodafoneapi.utils.VodafoneIoTConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public class VodafoneIoTServiceImplTest {
    @InjectMocks
    VodafoneIoTServiceImpl service;

    @Mock
    private VodafoneIoTRepository vodafoneIoTRepository;

    @BeforeEach
    public void initEach() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("loadIoTEventsFromFileTest:  ERROR no data file found")
    void loadIoTEventsFromFileTest(){
        IoTFIleRequest request = new IoTFIleRequest();
        request.setFilePath("Test.csv");
        ResponseEntity response = service.loadIoTEventsFromFile(request);
        assertEquals(response.getBody(), NO_DATA_FILE_FOUND_EXCEPTION);
        assertEquals(NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("loadIoTEventsFromFileTest:  data refreshed")
    void loadIoTEventsFromFileTest2() throws IOException {
        IoTFIleRequest request = new IoTFIleRequest();
        request.setFilePath(getClass().getClassLoader().getResource("IoTData.csv").getPath());
        ResponseEntity response = service.loadIoTEventsFromFile(request);
        assertEquals(response.getBody(),DATA_REFRESHED);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(vodafoneIoTRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("getIotUserDetailTest: OK")
    void getIotUserDetailTest(){
        VodafoneIoT iotData = getVodafoneIoTData();
        iotData.setLatitude(51.5185F);
        iotData.setLongitude(-0.1736F);
        iotData.setAirplaneMode(Boolean.FALSE);
        when(vodafoneIoTRepository.findByIdAndDateTimeList(any(),any())).thenReturn(List.of(iotData));
        ResponseEntity<VodafoneIoTResponse> response = service.getIotUserDetail("WG11155638",null);
        assertEquals(response.getBody().getName(),CYCLE_PLUS_TRACKER);
        assertEquals(response.getBody().getDescription(),LOCATION_IDENTIFIED);
        assertEquals(response.getBody().getLatitude(),51.5185F);
        assertEquals(response.getBody().getLongitude(),-0.1736F);
        assertEquals(response.getBody().getStatus(),STATUS_NA);
        verify(vodafoneIoTRepository, times(1)).findByIdAndDateTimeList(any(),any());
    }

    @Test
    @DisplayName("getIotUserDetailTestError: Id Not Found")
    void getIotUserDetailTest2(){
        when(vodafoneIoTRepository.findByIdAndDateTimeList(any(),any())).thenReturn(List.of());
        NoSuchElementException exception = Assertions.assertThrows(NoSuchElementException.class, () -> {
            service.getIotUserDetail("WG11155638",null);
        });
        assertTrue(exception.getMessage().contains("ERROR: Id WG11155638 not found"));
        verify(vodafoneIoTRepository, times(1)).findByIdAndDateTimeList(any(),any());
    }

    @Test
    @DisplayName("getIotUserDetailTest: No change in location, STATUS_NA...")
    void getIotUserDetailTest3(){
        VodafoneIoT iotData = getVodafoneIoTData();
        iotData.setLatitude(51.5185F);
        iotData.setLongitude(-0.1736F);
        iotData.setAirplaneMode(Boolean.FALSE);
        when(vodafoneIoTRepository.findByIdAndDateTimeList(any(),any())).thenReturn(List.of(iotData,iotData,iotData));
        ResponseEntity<VodafoneIoTResponse> response = service.getIotUserDetail("WG11155638",null);
        assertEquals(response.getBody().getName(),CYCLE_PLUS_TRACKER);
        assertEquals(response.getBody().getDescription(),LOCATION_IDENTIFIED);
        assertEquals(response.getBody().getLatitude(),51.5185F);
        assertEquals(response.getBody().getLongitude(),-0.1736F);
        assertEquals(response.getBody().getStatus(),STATUS_INACTIVE);
        verify(vodafoneIoTRepository, times(1)).findByIdAndDateTimeList(any(),any());
    }

    @Test
    @DisplayName("getIotUserDetailTest: Change in the last 3 locations...")
    void getIotUserDetailTest4(){
        VodafoneIoT iotData = getVodafoneIoTData();
        iotData.setLatitude(51.5185F);
        iotData.setLongitude(-0.1736F);
        iotData.setAirplaneMode(Boolean.FALSE);
        VodafoneIoT iotData3 = getVodafoneIoTData();
        iotData3.setLatitude(52.5185F);
        iotData3.setLongitude(-0.1736F);
        iotData3.setAirplaneMode(Boolean.FALSE);

        when(vodafoneIoTRepository.findByIdAndDateTimeList(any(),any())).thenReturn(List.of(iotData,iotData,iotData3));
        ResponseEntity<VodafoneIoTResponse> response = service.getIotUserDetail("WG11155638",null);
        assertEquals(response.getBody().getName(),CYCLE_PLUS_TRACKER);
        assertEquals(response.getBody().getDescription(),LOCATION_IDENTIFIED);
        assertEquals(response.getBody().getLatitude(),51.5185F);
        assertEquals(response.getBody().getLongitude(),-0.1736F);
        assertEquals(response.getBody().getStatus(),STATUS_ACTIVE);
        verify(vodafoneIoTRepository, times(1)).findByIdAndDateTimeList(any(),any());
    }

    @Test
    @DisplayName("mapVodafoneIoTResponseTest: ResponseStatusException when AirPlane mode is off but location not present")
    void mapVodafoneIoTResponseTest(){
        VodafoneIoT iotData = getVodafoneIoTData();
        iotData.setAirplaneMode(Boolean.FALSE);
        ResponseStatusException exception = Assertions.assertThrows(ResponseStatusException.class, () -> {
            service.mapVodafoneIoTResponse(iotData,null);
        });
        assertTrue(exception.getMessage().contains(DEVICE_COULD_NOT_BE_LOCATED_EXCEPTION));

    }
    @Test
    @DisplayName("mapVodafoneIoTResponseTest: Success when AirPlane mode is off and location is present")
    void mapVodafoneIoTResponseTest4(){
        VodafoneIoT iotData = getVodafoneIoTData();
        iotData.setAirplaneMode(Boolean.FALSE);
        iotData.setLatitude(51.5185F);
        iotData.setLongitude(-0.1736F);
        ResponseEntity<VodafoneIoTResponse> response = service.mapVodafoneIoTResponse(iotData,null);
        assertEquals(response.getBody().getName(),CYCLE_PLUS_TRACKER);
        assertEquals(response.getBody().getDescription(),LOCATION_IDENTIFIED);
        assertEquals(response.getBody().getLatitude(),51.5185F);
        assertEquals(response.getBody().getLongitude(),-0.1736F);
        assertEquals(response.getBody().getStatus(),STATUS_ACTIVE);
    }


    @Test
    @DisplayName("mapVodafoneIoTResponseTest: without passing status value")
    void mapVodafoneIoTResponseTest2(){
        VodafoneIoT iotData = getVodafoneIoTData();
        iotData.setLatitude(51.5185F);
        iotData.setLongitude(-0.1736F);
        iotData.setAirplaneMode(Boolean.FALSE);
        ResponseEntity<VodafoneIoTResponse> response = service.mapVodafoneIoTResponse(iotData,null);
        assertEquals(response.getBody().getName(),CYCLE_PLUS_TRACKER);
        assertEquals(response.getBody().getDescription(),LOCATION_IDENTIFIED);
        assertEquals(response.getBody().getLatitude(),51.5185F);
        assertEquals(response.getBody().getLongitude(),-0.1736F);
        assertEquals(response.getBody().getStatus(),STATUS_ACTIVE);
    }


    @Test
    @DisplayName("mapVodafoneIoTResponseTest: with passing status value")
    void mapVodafoneIoTResponseTest3(){
        VodafoneIoT iotData = getVodafoneIoTData();
        iotData.setLatitude(51.5185F);
        iotData.setLongitude(-0.1736F);
        iotData.setAirplaneMode(Boolean.FALSE);
        ResponseEntity<VodafoneIoTResponse> response = service.mapVodafoneIoTResponse(iotData, STATUS_INACTIVE);
        assertEquals(response.getBody().getName(),CYCLE_PLUS_TRACKER);
        assertEquals(response.getBody().getDescription(),LOCATION_IDENTIFIED);
        assertEquals(response.getBody().getLatitude(),51.5185F);
        assertEquals(response.getBody().getLongitude(),-0.1736F);
        assertEquals(response.getBody().getStatus(),STATUS_INACTIVE);
    }

    @Test
    @DisplayName("getBatteryStatusTest : OK")
    void getBatteryStatusTest(){
        assertEquals(BATTERY_FULL,service.getBatteryStatus(Float.valueOf(99)));
        assertEquals(BATTERY_FULL,service.getBatteryStatus(Float.valueOf(98)));
        assertEquals(BATTERY_HIGH,service.getBatteryStatus(Float.valueOf(97)));
        assertEquals(BATTERY_MEDIUM,service.getBatteryStatus(Float.valueOf(45)));
        assertEquals(BATTERY_LOW,service.getBatteryStatus(Float.valueOf(10)));
        assertEquals(BATTERY_CRITICAL,service.getBatteryStatus(Float.valueOf(9)));
    }

    @Test
    @DisplayName("getBooleanValueTestTest:  OK")
    void getBooleanValueTest(){
        Boolean valueFalse = service.getBooleanValue("OFF");
        assertFalse(valueFalse);
        Boolean valueTrue = service.getBooleanValue("ON");
        assertTrue(valueTrue);
    }

    @Test
    @DisplayName("getStringToVodafoneIoTMappingTest: OK")
    void getStringToVodafoneIoTMappingTest(){
        String[] data = "1582605077000,10001,WG11155638,51.5185,-0.1736,0.99,OFF,OFF".split(",");
        VodafoneIoT iot = service.getStringToVodafoneIoTMapping(data);
        assertEquals(Instant.ofEpochMilli(Long.parseLong("1582605077000")).atZone(ZoneId.systemDefault()).toLocalDateTime(),iot.getDateTime());
        assertEquals(51.5185F,iot.getLatitude());
        assertEquals(-0.1736F,iot.getLongitude());
        assertEquals(false,iot.getAirplaneMode());
        assertEquals("WG11155638",iot.getProductId());
        assertEquals(10001,iot.getEventId());
        assertEquals(99,iot.getBattery());
    }

    public VodafoneIoT getVodafoneIoTData(){
        VodafoneIoT ioTData = new VodafoneIoT();
        ioTData.setProductId("WG11155638");
        ioTData.setDateTime(LocalDateTime.now());
        ioTData.setEventId(101L);
        ioTData.setBattery(99F);
        return ioTData;
    }
}
