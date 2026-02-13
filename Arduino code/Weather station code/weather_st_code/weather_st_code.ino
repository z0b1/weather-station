/*
 * Solar Weather Station
 * Nano + FS1000A
 */

#include "DHT.h"
#include <ModbusMaster.h>
#include <SoftwareSerial.h>

// pins
#define DHT_PIN 4
#define DHT_TYPE DHT22
#define RS485_RX 8
#define RS485_TX 9
#define RS485_CONTROL 2
#define RF_TX_PIN 3
#define STATUS_LED 10

// modbus ids
#define SPEED_ID 1
#define DIRECTION_ID 2

DHT dht(DHT_PIN, DHT_TYPE);
SoftwareSerial rs485Serial(RS485_RX, RS485_TX);
ModbusMaster modbus;

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

  pinMode(RS485_CONTROL, OUTPUT);
  pinMode(RF_TX_PIN, OUTPUT);
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

  // bit-bang ook
  for (int i = 0; i < payload.length(); i++) {
    char c = payload[i];
    for (int bit = 0; bit < 8; bit++) {
      digitalWrite(RF_TX_PIN, HIGH);
      if (bitRead(c, bit)) {
        delayMicroseconds(600); // 1
      } else {
        delayMicroseconds(250); // 0
      }
      digitalWrite(RF_TX_PIN, LOW);
      delayMicroseconds(400); // gap
    }
  }

  digitalWrite(STATUS_LED, LOW);
}
