package com.udacity.catpoint.securityservice.service;

import com.udacity.catpoint.imageservice.service.ImageService;
import com.udacity.catpoint.securityservice.data.SecurityRepository;
import com.udacity.catpoint.securityservice.data.Sensor;
import com.udacity.catpoint.securityservice.data.SensorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static com.udacity.catpoint.securityservice.data.AlarmStatus.NO_ALARM;
import static com.udacity.catpoint.securityservice.data.AlarmStatus.PENDING_ALARM;
import static com.udacity.catpoint.securityservice.data.ArmingStatus.ARMED_HOME;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {
  private SecurityService securityService;
  private Sensor sensor;
  private String randomName = UUID.randomUUID().toString();

  @Mock
  private ImageService imageService;
  @Mock
  private SecurityRepository securityRepository;

  @BeforeEach
  void init(){
    securityService = new SecurityService(securityRepository,imageService );
    sensor = new Sensor(randomName, SensorType.DOOR);
  }

  @Test
  @DisplayName("If alarm is armed and a sensor becomes activated, put the system into pending alarm status.")
  void ifAlarmIsArmedAndSensorIsActived_setAlarmStatusToPENDING_ALARM() {
    when(securityRepository.getAlarmStatus()).thenReturn(NO_ALARM);
    when(securityRepository.getArmingStatus()).thenReturn(ARMED_HOME);
    securityService.changeSensorActivationStatus(sensor, true);
    verify(securityRepository, times(1)).setAlarmStatus(PENDING_ALARM);
  }

  @Test
  void addStatusListener() {
  }

  @Test
  void removeStatusListener() {
  }

  @Test
  void setAlarmStatus() {
  }

  @Test
  void changeSensorActivationStatus() {
  }

  @Test
  void processImage() {
  }

  @Test
  void getAlarmStatus() {
  }

  @Test
  void getSensors() {
  }

  @Test
  void addSensor() {
  }

  @Test
  void removeSensor() {
  }

  @Test
  void getArmingStatus() {
  }
}