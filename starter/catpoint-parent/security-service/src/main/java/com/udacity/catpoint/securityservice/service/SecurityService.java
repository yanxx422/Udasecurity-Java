package com.udacity.catpoint.securityservice.service;

import com.udacity.catpoint.imageservice.service.ImageService;
import com.udacity.catpoint.securityservice.application.StatusListener;
import com.udacity.catpoint.securityservice.data.AlarmStatus;
import com.udacity.catpoint.securityservice.data.ArmingStatus;
import com.udacity.catpoint.securityservice.data.SecurityRepository;
import com.udacity.catpoint.securityservice.data.Sensor;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Service that receives information about changes to the security system. Responsible for
 * forwarding updates to the repository and making any decisions about changing the system state.
 * <p>
 * This is the class that should contain most of the business logic for our system, and it is the
 * class you will be writing unit tests for.
 */
public class SecurityService {


  private final ImageService imageService;
  private final SecurityRepository securityRepository;
  private final Set<StatusListener> statusListeners = new HashSet<>();
  private Boolean catDetected = false;

  public SecurityService(SecurityRepository securityRepository, ImageService imageService) {
    this.securityRepository = securityRepository;
    this.imageService = imageService;
  }

  private boolean getSensorActivity() {
    return getSensors().stream().noneMatch(Sensor::getActive);
  }

  /**
   * Internal method that handles alarm status changes based on whether the camera currently shows a
   * cat.
   *
   * @param cat True if a cat is detected, otherwise false.
   */
  private void catDetected(Boolean cat) {
    catDetected = cat;
    if(cat && getArmingStatus() == ArmingStatus.ARMED_HOME) {
      setAlarmStatus(AlarmStatus.ALARM);
    } else {
      setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    statusListeners.forEach(sl -> sl.catDetected(cat));
  }

  /**
   * Register the StatusListener for alarm system updates from within the service.SecurityService.
   *
   * @param statusListener
   */
  public void addStatusListener(StatusListener statusListener) {
    statusListeners.add(statusListener);
  }

  public void removeStatusListener(StatusListener statusListener) {
    statusListeners.remove(statusListener);
  }

  /**
   * Internal method for updating the alarm status when a sensor has been activated.
   */
  private void handleSensorActivated() {
    if(securityRepository.getArmingStatus() == ArmingStatus.DISARMED) {
      return; //no problem if the system is disarmed
    }
    switch(securityRepository.getAlarmStatus()) {
      case NO_ALARM -> setAlarmStatus(AlarmStatus.PENDING_ALARM);
      case PENDING_ALARM -> setAlarmStatus(AlarmStatus.ALARM);
    }
  }

  /**
   * Internal method for updating the alarm status when a sensor has been deactivated
   */
  protected void handleSensorDeactivated() {
    switch (securityRepository.getAlarmStatus()) {
      case PENDING_ALARM -> setAlarmStatus(AlarmStatus.NO_ALARM);
    }
  }

  /**
   * Change the activation status for the specified sensor and update alarm status if necessary.
   *
   * @param sensor
   * @param active
   */
  public void changeSensorActivationStatus(Sensor sensor, Boolean active) {
    if(securityRepository.getAlarmStatus() != AlarmStatus.ALARM) {
      if (active) {
        handleSensorActivated();
      }
      else{
        if(sensor.getActive()){
          handleSensorDeactivated();
        }
      }
    }
    sensor.setActive(active);
    securityRepository.updateSensor(sensor);
  }


  /**
   * Send an image to the service.SecurityService for processing. The securityService will use its
   * provided ImageService to analyze the image for cats and update the alarm status accordingly.
   *
   * @param currentCameraImage
   */
  public void processImage(BufferedImage currentCameraImage) {
    catDetected(imageService.imageContainsCat(currentCameraImage, 50.0f));
  }

  public AlarmStatus getAlarmStatus() {
    return securityRepository.getAlarmStatus();
  }

  /**
   * Change the alarm status of the system and notify all listeners.
   *
   * @param status
   */
  public void setAlarmStatus(AlarmStatus status) {
    securityRepository.setAlarmStatus(status);
    statusListeners.forEach(sl -> sl.notify(status));
  }

  public Set<Sensor> getSensors() {
    return securityRepository.getSensors();
  }

  public void addSensor(Sensor sensor) {
    securityRepository.addSensor(sensor);
  }

  public void removeSensor(Sensor sensor) {
    securityRepository.removeSensor(sensor);
  }

  public ArmingStatus getArmingStatus() {
    return securityRepository.getArmingStatus();
  }

  /**
   * Sets the current arming status for the system. Changing the arming status may update both the
   * alarm status.
   *
   * @param armingStatus
   */
  public void setArmingStatus(ArmingStatus armingStatus) {
    if (armingStatus == ArmingStatus.DISARMED) {
      setAlarmStatus(AlarmStatus.NO_ALARM);
    } else {
      if (catDetected && armingStatus == ArmingStatus.ARMED_HOME) {
        setAlarmStatus(AlarmStatus.ALARM);
      }
      ConcurrentSkipListSet<Sensor> sensors = new ConcurrentSkipListSet<>(getSensors());
      sensors.forEach(s -> changeSensorActivationStatus(s, false));
    }
    securityRepository.setArmingStatus(armingStatus);
    statusListeners.forEach(sl -> sl.sensorStatusChanged());
  }
}