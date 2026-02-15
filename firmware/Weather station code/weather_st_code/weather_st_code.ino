/*
 * DIY Solar Weather Station - Hack Club Blueprint 2026
 * Setup: Arduino Nano + RS485 Anemometers + DHT22
 * Radio: FS1000A 433.92MHz Transmitter
 * By: z0b1
 */

#include "DHT.h"
#include <ModbusMaster.h>
#include <RH_ASK.h>
#include <SPI.h>
#include <SoftwareSerial.h>

#define DHT_PIN 4 // DHT22 data line
#define DHT_TYPE DHT22
#define RS485_RX 8      // MAX485 Receive (RO)
#define RS485_TX 9      // MAX485 Transmit (DI)
#define RS485_CONTROL 2 // DE/RE Direction pin
#define RF_TX_PIN 3     // FS1000A Data pin
#define STATUS_LED 10   // Blinks during transmission

#define SPEED_ID 1
#define DIRECTION_ID 2

DHT dht(DHT_PIN, DHT_TYPE);
SoftwareSerial rs485Serial(RS485_RX, RS485_TX);
ModbusMaster modbus;
RH_ASK driver(2000, 0, RF_TX_PIN,
              0); // 2000 bps, RX on 0 (unused), TX on RF_TX_PIN

float windSpeed = 0.0;
int windHeading = 0;
float airTemp = 0.0;
float airHum = 0.0;

// RS485 mode
void toggleRS485(bool transmit) {
  digitalWrite(RS485_CONTROL, transmit ? HIGH : LOW);
  delay(5);
}

void setup() {
  Serial.begin(9600);
  rs485Serial.begin(9600);
  dht.begin();

  if (!driver.init()) {
    Serial.println("RadioHead init failed");
  }

  pinMode(RS485_CONTROL, OUTPUT);
  pinMode(STATUS_LED, OUTPUT);

  toggleRS485(false);
  modbus.preTransmission([]() { toggleRS485(true); });
  modbus.postTransmission([]() { toggleRS485(false); });
}

void loop() {
  // read dht22
  airTemp = dht.readTemperature();
  airHum = dht.readHumidity();

  // read speed
  modbus.begin(SPEED_ID, rs485Serial);
  if (modbus.readHoldingRegisters(0x0000, 1) == modbus.ku8MBSuccess) {
    windSpeed = modbus.getResponseBuffer(0) / 10.0;
  }

  // read direction
  modbus.begin(DIRECTION_ID, rs485Serial);
  if (modbus.readHoldingRegisters(0x0000, 1) == modbus.ku8MBSuccess) {
    windHeading = modbus.getResponseBuffer(0);
  }

  // send data
  sendRadioPacket();
  delay(5000);
}

// radio transmitter
void sendRadioPacket() {
  digitalWrite(STATUS_LED, HIGH);

  // build payload
  String payload = "S:" + String(windSpeed, 1) + ",D:" + String(windHeading) +
                   ",T:" + String(airTemp, 1) + ",H:" + String(airHum, 0) + ";";

  const char *msg = payload.c_str();
  driver.send((uint8_t *)msg, strlen(msg));
  driver.waitPacketSent();

  digitalWrite(STATUS_LED, LOW);
}
