/* 
 * DIY Solar Weather Station - Hack Club Blueprint 2026
 * Setup: Arduino Nano + RS485 Anemometers + DHT22 
 * Radio: Custom 433.92MHz SAW (R433A) Transmitter
 * By: z0b1
 */

#include <SoftwareSerial.h>
#include <ModbusMaster.h>
#include "DHT.h"

// --- HW PIN MAPPING ---
#define DHT_PIN 4         // DHT22 data line
#define DHT_TYPE DHT22
#define RS485_RX 8        // MAX485 Receive (RO)
#define RS485_TX 9        // MAX485 Transmit (DI)
#define RS485_CONTROL 2   // DE/RE Direction pin
#define RF_TX_PIN 3       // Data pin for our homemade 433MHz stage
#define STATUS_LED 10     // Blinks during transmission

// --- MODBUS CONFIG ---
// Note: Check your sensor manuals. Usually, default IDs are 1 and 2.
#define SPEED_ID 1        
#define DIRECTION_ID 2    

DHT dht(DHT_PIN, DHT_TYPE);
SoftwareSerial rs485Serial(RS485_RX, RS485_TX);
ModbusMaster modbus;

// Variables to hold our field data
float windSpeed = 0.0;
int windHeading = 0;
float airTemp = 0.0;
float airHum = 0.0;

// Helper: Toggles the MAX485 chip between "Listen" and "Talk" modes
void toggleRS485(bool transmit) {
  digitalWrite(RS485_CONTROL, transmit ? HIGH : LOW);
  delay(5); // Small buffer for the hardware to switch over
}

void setup() {
  Serial.begin(9600);      // For PC debugging
  rs485Serial.begin(9600); // Standard speed for most RS485 wind sensors
  
  dht.begin();
  
  pinMode(RS485_CONTROL, OUTPUT);
  pinMode(RF_TX_PIN, OUTPUT);
  pinMode(STATUS_LED, OUTPUT);
  
  toggleRS485(false); // Start in listening mode
  
  // Tie the Modbus library to our toggle function
  modbus.preTransmission([]() { toggleRS485(true); });
  modbus.postTransmission([]() { toggleRS485(false); });
}

void loop() {
  // 1. Grab local temp and humidity
  airTemp = dht.readTemperature();
  airHum = dht.readHumidity();

  // 2. Poll the RS485 Anemometer (Speed)
  modbus.begin(SPEED_ID, rs485Serial);
  if (modbus.readHoldingRegisters(0x0000, 1) == modbus.ku8MBSuccess) {
    // Most sensors send (Value * 10). Example: 125 = 12.5 m/s
    windSpeed = modbus.getResponseBuffer(0) / 10.0;
  }

  // 3. Poll the RS485 Wind Vane (Direction)
  modbus.begin(DIRECTION_ID, rs485Serial);
  if (modbus.readHoldingRegisters(0x0000, 1) == modbus.ku8MBSuccess) {
    windHeading = modbus.getResponseBuffer(0);
  }

  // 4. Fire the custom RF Transmitter
  sendRadioPacket();

  // Wait 5 seconds so we don't spam the 433MHz band (and to save battery)
  delay(5000); 
}

// Function to handle the OOK (On-Off Keying) for the R433A stage
void sendRadioPacket() {
  digitalWrite(STATUS_LED, HIGH);
  
  // Packet structure: "S:speed,D:direction,T:temp,H:hum"
  String payload = "S:" + String(windSpeed, 1) + 
                   ",D:" + String(windHeading) + 
                   ",T:" + String(airTemp, 1) + 
                   ",H:" + String(airHum, 0) + ";";

  // Manual bit-banging to ensure the SAW resonator has time to "wake up"
  for (int i = 0; i < payload.length(); i++) {
    char c = payload[i];
    for (int bit = 0; bit < 8; bit++) {
      digitalWrite(RF_TX_PIN, HIGH);
      if (bitRead(c, bit)) {
        delayMicroseconds(600); // Pulse for a logic 1
      } else {
        delayMicroseconds(250); // Pulse for a logic 0
      }
      digitalWrite(RF_TX_PIN, LOW);
      delayMicroseconds(400); // Gap between bits
    }
  }
  
  digitalWrite(STATUS_LED, LOW);
}
