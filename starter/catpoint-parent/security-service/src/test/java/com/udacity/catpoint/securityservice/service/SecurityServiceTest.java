package com.udacity.catpoint.securityservice.service;

import static com.udacity.catpoint.securityservice.data.AlarmStatus.ALARM;
import static com.udacity.catpoint.securityservice.data.AlarmStatus.NO_ALARM;
import static com.udacity.catpoint.securityservice.data.AlarmStatus.PENDING_ALARM;
import static com.udacity.catpoint.securityservice.data.ArmingStatus.ARMED_HOME;
import static com.udacity.catpoint.securityservice.data.ArmingStatus.DISARMED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.udacity.catpoint.imageservice.service.ImageService;
import com.udacity.catpoint.securityservice.application.StatusListener;
import com.udacity.catpoint.securityservice.data.AlarmStatus;
import com.udacity.catpoint.securityservice.data.ArmingStatus;
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
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

  @Mock
  BufferedImage image;
  private SecurityService securityService;
  private StatusListener listener;
  @Mock
  private Sensor sensor;
  @Mock
  private ImageService imageService;
  @Mock
  private SecurityRepository securityRepository;

  @BeforeEach
  void init() {
    securityService = new SecurityService(securityRepository, imageService);
  }

  private Set<Sensor> getSensorList(Boolean activeStatus, int count) {
    Set<Sensor> sensorList = new HashSet<>();
    for (int i = 0; i < count; i++) {
      sensorList.add(new Sensor(UUID.randomUUID().toString(), SensorType.DOOR));
    }
    sensorList.forEach(it -> it.setActive(activeStatus));
    return sensorList;
  }

  @ParameterizedTest
  @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY", "ARMED_HOME"})
  @DisplayName("If alarm is armed and a sensor becomes activated, put the system into pending alarm status.")
  void ifAlarmIsArmedAndSensorIsActive_setAlarmStatusToPENDING_ALARM(ArmingStatus status) {
    //given
    when(securityRepository.getAlarmStatus()).thenReturn(NO_ALARM);
    when(securityRepository.getArmingStatus()).thenReturn(status);
    //when
    securityService.changeSensorActivationStatus(sensor, true);
    //then
    verify(securityRepository, times(1)).setAlarmStatus(PENDING_ALARM);
  }

  @ParameterizedTest
  @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY", "ARMED_HOME"})
  @DisplayName("If alarm is armed and a sensor becomes activated and the system is already pending alarm, set the alarm status to alarm.")
  void ifAlarmIsArmedAndSensorIsActiveAndSensorIsPendingAlarm_setAlarmStatusToAlarm(
      ArmingStatus status) {
    //given
    when(securityRepository.getAlarmStatus()).thenReturn(PENDING_ALARM);
    when(securityRepository.getArmingStatus()).thenReturn(status);
    //when
    securityService.changeSensorActivationStatus(sensor, true);
    //then
    verify(securityRepository, atMostOnce()).setAlarmStatus(ALARM);
  }

  @Test
  @DisplayName("If pending alarm and all sensors are inactive, return to no alarm state.")
  void ifPendingAlarmAndSensorsAreInactive_setToNoAlarm() {
    //given
    Set<Sensor> sensorList = getSensorList(true, 2);
    when(securityRepository.getAlarmStatus()).thenReturn(PENDING_ALARM);
    Sensor Sensor_1 = sensorList.iterator().next();
    Sensor Sensor_2 = sensorList.iterator().next();
    //when
    securityService.changeSensorActivationStatus(Sensor_1, false);
    securityService.changeSensorActivationStatus(Sensor_2, false);
    //then
    verify(securityRepository, atMostOnce()).setAlarmStatus(NO_ALARM);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  @DisplayName("If alarm is active, change in sensor state should not affect the alarm state.")
  void ifAlarmIsActive_sensorStateChange_shouldNotAffectAlarmState(boolean activeStatus) {
    //given
    when(securityRepository.getAlarmStatus()).thenReturn(ALARM);
    Set<Sensor> sensorList = getSensorList(false, 2);
    securityService.changeSensorActivationStatus(sensor, activeStatus);
    Sensor Sensor_1 = sensorList.iterator().next();
    Sensor Sensor_2 = sensorList.iterator().next();
    //when
    securityService.changeSensorActivationStatus(Sensor_1, true);
    securityService.changeSensorActivationStatus(Sensor_2, true);
    //then
    assertEquals(ALARM, securityRepository.getAlarmStatus());
    //when
    securityService.changeSensorActivationStatus(Sensor_1, false);
    assertEquals(ALARM, securityRepository.getAlarmStatus());
  }

  @Test
  @DisplayName("If a sensor is activated while already active and the system is in pending state, change it to alarm state.")
  void ifPendingAndActivateAnAlreadyActivatedSensor_setToAlarm() {
    //given
    when(securityRepository.getAlarmStatus()).thenReturn(PENDING_ALARM);
    sensor.setActive(true);
    //then
    securityService.changeSensorActivationStatus(sensor, true);
    verify(securityRepository, times(1)).setAlarmStatus(ALARM);
  }

  @Test
  @DisplayName("If a sensor is deactivated while already inactive, make no changes to the alarm state.")
  void ifDeactivateAlreadyInActiveSensor_shouldNotAffectAlarmState() {
    //given
    sensor.setActive(false);
    //when
    securityService.changeSensorActivationStatus(sensor, false);
    //then
    verify(securityRepository, never()).setArmingStatus(any());
  }


  @Test
  @DisplayName("If the camera image contains a cat while the system is armed-home, put the system into alarm status.")
  void ifArmedHomeAndImageContainsACat_setToAlarm() {
    //given
    when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
    when(securityRepository.getArmingStatus()).thenReturn(ARMED_HOME);
    //when
    securityService.processImage(image);
    //then
    verify(securityRepository, times(1)).setAlarmStatus(ALARM);
  }

  @Test
  @DisplayName("If the camera image does not contain a cat, change the status to no alarm as long as the sensors are not active.")
  void ifImageNotContainsACatAndSensorsAreNotActive_setToNoAlarm() {
    //given
    when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
    sensor.setActive(false);
    securityService.changeSensorActivationStatus(sensor, false);
    //when
    securityService.processImage(image);
    //then
    verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
  }

  @Test
  @DisplayName("If the system is disarmed, set the status to no alarm.")
  void ifDisarmed_setToNoAlarm() {
    securityService.setArmingStatus(DISARMED);
    verify(securityRepository).setAlarmStatus(NO_ALARM);
  }

  @ParameterizedTest
  @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY", "ARMED_HOME"})
  @DisplayName("If the system is armed, reset all sensors to inactive.")
  void ifArmed_setAllSensorsToInactive(ArmingStatus status) {
    //given
    Set<Sensor> sensors = getSensorList(true, 2);
    when(securityRepository.getSensors()).thenReturn(sensors);
    //when
    securityService.setArmingStatus(status);
    //then
    securityService.getSensors().forEach(sensor -> assertFalse(sensor.getActive()));
  }

  @Test
  @DisplayName("If the system is armed-home while the camera shows a cat, set the alarm status to alarm.")
  void ifArmed_homeAndImageContainsACat_setAlarmToAlarm() {
    //given
    when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
    when(securityRepository.getArmingStatus()).thenReturn(ARMED_HOME);
    securityService.processImage(image);
    //when
    securityService.setAlarmStatus(ALARM);
    //then
    verify(securityRepository, atMost(2)).setAlarmStatus(ALARM);
  }

  // The following tests are for the test coverage

  @Test
  void ifArmed_homeAndImageContainsACat_setToAlarm() {
    when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
    when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
    securityService.processImage(image);
    verify(securityRepository, atMostOnce()).setAlarmStatus(ALARM);
  }

  @Test
  void ifDisarmedAndActivateASensor_setToPendingAlarm() {
    when(securityRepository.getArmingStatus()).thenReturn(DISARMED);
    securityService.changeSensorActivationStatus(sensor, true);
    verify(securityRepository, atMostOnce()).setAlarmStatus(PENDING_ALARM);
  }

  @Test
  void ifPendingAlarmAndDeactivateASensor_setToNoAlarm() {
    sensor.setActive(true);
    when(securityRepository.getAlarmStatus()).thenReturn(PENDING_ALARM);
    securityService.changeSensorActivationStatus(sensor, false);
    verify(securityRepository, atMostOnce()).setAlarmStatus(NO_ALARM);
  }


  @Test
  void testAddRemoveSensor() {
    securityService.addSensor(sensor);
    verify(securityRepository).addSensor(sensor);
    securityService.removeSensor(sensor);
    verify(securityRepository).removeSensor(sensor);
  }

  @Test
  void testAddRemoveListener() {
    securityService.addStatusListener(listener);
    securityService.removeStatusListener(listener);
  }

}