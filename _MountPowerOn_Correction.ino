  // MPon: Turn mount power on
  if (userCmd == MPon) {
    if (EmergencyOverride) {
      pushMessage("OVERRIDE: Turning on mount power");
      myDigitalWrite(mountPowerOut, HIGH);
    } else {
      if (sensorInput(mountPowerIn)) {
        pushMessage("ERROR: Mount is already on!");
      } else {
        if (!sensorInput(roofOpen)) {    //CORRECTED made NOT sensorInput(roofOpen)
          pushMessage("ERROR: Cannot turn on mount: roof is not open");
        } else {
          pushMessage("INFO: Turning on mount power");
          myDigitalWrite(mountPowerOut, HIGH);
        }
      }
    }
    userCmd = None;
  }
