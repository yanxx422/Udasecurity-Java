package com.udacity.catpoint.securityservice.service;

import static com.udacity.catpoint.securityservice.data.AlarmStatus.ALARM;
import static com.udacity.catpoint.securityservice.data.AlarmStatus.NO_ALARM;
import static com.udacity.catpoint.securityservice.data.AlarmStatus.PENDING_ALARM;
import static com.udacity.catpoint.securityservice.data.ArmingStatus.ARMED_HOME;
import static com.udacity.catpoint.securityservice.data.ArmingStatus.DISARMED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.udacity.catpoint.imageservice.service.ImageService;
import com.udacity.catpoint.securityservice.data.SecurityRepository;
import com.udacity.catpoint.securityservice.data.Sensor;
import com.udacity.catpoint.securityservice.data.SensorType;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {
  private SecurityService securityService;
  @Mock
  private Sensor sensor;
  @Mock
  private ImageService imageService;
  @Mock
  private SecurityRepository securityRepository;

  @Mock
  BufferedImage image;

  @BeforeEach
  void init(){
    securityService = new SecurityService(securityRepository,imageService );
  }

  private Set<Sensor> getSensorList(Boolean activeStatus, int count){
    Set<Sensor> sensorList = new HashSet<>();
    for (int i = 0; i < count; i++){
      sensorList.add(new Sensor(UUID.randomUUID().toString(), SensorType.DOOR));
    }
    sensorList.forEach(it -> it.setActive(activeStatus));
    return sensorList;
  }

  @Test
  @DisplayName("If alarm is armed and a sensor becomes activated, put the system into pending alarm status.")
  void ifAlarmIsArmedAndSensorIsActive_setAlarmStatusToPENDING_ALARM() {
    when(sensor.getActive()).thenReturn(false);
    when(securityRepository.getAlarmStatus()).thenReturn(NO_ALARM);
    when(securityRepository.getArmingStatus()).thenReturn(ARMED_HOME);
    securityService.changeSensorActivationStatus(sensor, true);
    verify(securityRepository, times(1)).setAlarmStatus(eq(PENDING_ALARM));
  }

  @Test
  @DisplayName("If alarm is armed and a sensor becomes activated and the system is already pending alarm, set the alarm status to alarm.")
  void ifAlarmIsArmedAndSensorIsActiveAndSensorIsPendingAlarm_setAlarmStatusToAlarm() {
    when(sensor.getActive()).thenReturn(true);
    when(securityRepository.getAlarmStatus()).thenReturn(PENDING_ALARM);
    when(securityRepository.getArmingStatus()).thenReturn(ARMED_HOME);
    securityService.changeSensorActivationStatus(sensor, true);
    verify(securityRepository, times(1)).setAlarmStatus(eq(ALARM));
  }

  @Test
  @DisplayName("If pending alarm and all sensors are inactive, return to no alarm state.")
  void ifPendingAlarmAndSensorsAreInactive_setToNoAlarm() {
    Set<Sensor> sensorList = getSensorList(true,10);
    //when(securityRepository.getSensors()).thenReturn(sensorList);
    when(securityRepository.getAlarmStatus()).thenReturn(PENDING_ALARM);
    Sensor ASensor = sensorList.iterator().next();
    securityService.changeSensorActivationStatus(ASensor, false);
    verify(securityRepository, times(1)).setAlarmStatus(eq(NO_ALARM));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  @DisplayName("If alarm is active, change in sensor state should not affect the alarm state.")
  void ifAlarmIsActive_sensorStateChange_shouldNotAffectAlarmState(Boolean ActiveStatus){
    when(securityRepository.getAlarmStatus()).thenReturn(ALARM);
    securityService.changeSensorActivationStatus(sensor, ActiveStatus);
    verify(securityRepository,times(0)).setAlarmStatus(any());
  }

  @Test
  @DisplayName("If a sensor is activated while already active and the system is in pending state, change it to alarm state.")
  void ifPendingAndActivateAnAlreadyActivatedSensor_setToAlarm(){
    when(sensor.getActive()).thenReturn(true);
    when(securityRepository.getAlarmStatus()).thenReturn(PENDING_ALARM);
    securityService.changeSensorActivationStatus(sensor,true);
    verify(securityRepository,times(1)).setAlarmStatus(eq(ALARM));
  }

  @Test
  @DisplayName("If a sensor is deactivated while already inactive, make no changes to the alarm state.")
  void ifDeactivateAlreadyInActiveSensor_shouldNotAffectAlarmState(){
    when(sensor.getActive()).thenReturn(false);
    securityService.changeSensorActivationStatus(sensor, false);
    verify(securityRepository, times(0)).setArmingStatus(any());
  }


  @Test
  @DisplayName("If the camera image contains a cat while the system is armed-home, put the system into alarm status.")
  void ifArmedHomeAndImageContainsACat_setToAlarm(){
    when(securityService.getArmingStatus()).thenReturn(ARMED_HOME);
    when(imageService.imageContainsCat(image, 50.0f)).thenReturn(true);
    securityService.processImage(image);
    verify(securityRepository, times(1)).setAlarmStatus(ALARM);
  }

  @Test
  @DisplayName("If the camera image does not contain a cat, change the status to no alarm as long as the sensors are not active.")
  void ifImageNotContainsACatAndSensorsAreNotActive_setToNoAlarm(){
    when(imageService.imageContainsCat(image, 50.0f)).thenReturn(false);
    securityService.processImage(image);
    verify(securityRepository,times(1)).setAlarmStatus(NO_ALARM);
  }

  @Test
  @DisplayName("If the system is disarmed, set the status to no alarm.")
  void ifDisarmed_setToNoAlarm(){
    securityService.setArmingStatus(DISARMED);
    verify(securityRepository).setAlarmStatus(NO_ALARM);
  }

  @Test
  @DisplayName("If the system is armed, reset all sensors to inactive.")
  void ifArmed_setAllSensorsToInactive(){
    securityService.setArmingStatus(ARMED_HOME);
    verify(securityRepository,atLeast(1)).updateSensor(sensor);

  }

  @Test
  @DisplayName("If the system is armed-home while the camera shows a cat, set the alarm status to alarm.")
  void ifArmed_homeAndImageContainsACCat_setToAlarm(){

  }

}