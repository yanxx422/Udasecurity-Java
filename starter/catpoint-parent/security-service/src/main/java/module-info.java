module com.udacity.catpoint.securityservice{
  requires com.udacity.catpoint.imageservice;
  requires java.desktop;
  requires miglayout;
  requires com.google.common;
  requires com.google.gson;
  requires java.prefs;
  requires junit;
  opens com.udacity.catpoint.securityservice.data to com.google.gson;
}