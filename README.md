# weather-station
I'm presenting my project of a Field Weather Station containing anemometers and wind direction sensors, also ambient temp. and humidity sensors with a UHF transmitter in the field and an at home reciever(rtl-sdr). This also includes an RPi3 B+ that is recieving satellite images and displaying them in a mobile phone app.
# Why a weather station-Short Story
I live in the countryside and my family has large fruit plantations. By the microclimate of my area strong frosts in the spring time are common, that quite often hurts the fruits. We solve this issue by turning on our watering systems(the ground water keeps around 12 degrees Celsius at all times), but also by using air heaters to warm the area. So instead of waking up every 30 minutes to check the temps and having to go outside this idea was born. Main goal is that the weather station sends me phone notifications to a custom website/app. The weather station is located atleast 500m away from the house so the trick was getting reliable power, which I solved using a solar panel and a Lead Acid battery.
# How it functions 
Using an Arduino Nano I read the data from the sensors and send it with my UHF transmitter using ASK modulation(to be changed to FSK if too much interferences). At home using a reciever and an RPi 3 B+, RTL SDR V4 and a homemade quadrifillar helix antenna to recieve satellite images and a Nagoya antenna for the UHF signals. Compare it and display on the website/app using the RPi.
# The BOM

| Reference | Qty | Value | Price | Link | Info | Datasheet |
|---|---|---|---|---|---|---|
| A1 | 1 | Arduino Nano | € 0.85 | [Link](https://www.aliexpress.com/item/1005009848194286.html) | | [Link](https://content.arduino.cc/assets/NANOEveryV3.0_sch.pdf) |
| AE1 | 1 | Antenna Shielded SMA | € 0.85 | [Link](https://www.aliexpress.com/item/1005008813991595.html) | | ~ |
| BT1 | 1 | 12VDC Lead Acid | € 27.59 | [Link](https://borikplus.rs/proizvodi/olovne-baterije/ms-12-12-olovna-vrla-baterija-12v-12ah/) | | ~ |
| C1 | 1 | 470uF | € 0.85 | [Link](https://www.aliexpress.com/item/1005008244407175.html) | | ~ |
| C2 | 1 | 100uF | € 1.22 | [Link](https://www.aliexpress.com/item/1005005661484224.html) | | ~ |
| C3 | 1 | 2pF | - | | | ~ |
| C4 | 1 | 2.2pF | - | | | ~ |
| C5 | 1 | 10pF | - | | | ~ |
| C6 | 1 | 470pF | - | | | ~ |
| D1 | 1 | LED_R | € 0.85 | [Link](https://www.aliexpress.com/item/1005010315277808.html) | | ~ |
| J1, J2 | 1 | RS485 ANEMOMETER | € 20.71 | [Link](https://www.aliexpress.com/item/1005007059408469.html) | RS485 type | ~ |
| J4 | 1 | RS485 WIND DIRECTION | € 18.44 | [Link](https://www.aliexpress.com/item/1005007059408469.html) | RS485 type | ~ |
| U4 | 1 | SDLA12TA | € 1.75 | [Link](https://www.aliexpress.com/item/1005006641063540.html) | | [Link](https://easyeda.com/component/451d05428ff8431fb9c594829dfd0fbf) |
| L1 | 1 | 22nH | € 2.10 | [Link](https://www.aliexpress.com/item/1005006169693076.html) | | ~ |
| Q1 | 1 | 2C3357 | € 0.85 | [Link](https://www.aliexpress.com/item/32844342626.html) | | [Link](http://www.b-kainka.de/Daten/Transistor/BC108.pdf) |
| Q2 | 1 | BC547 | € 1.01 | [Link](https://www.aliexpress.com/item/1005009457727959.html) | | [Link](https://www.onsemi.com/pub/Collateral/BC550-D.pdf) |
| R1 | 1 | 2k1? | € 12.22 | [Link](https://www.aliexpress.com/item/1005010688244614.html) | | ~ |
| R2 | 1 | 220? | - | | | ~ |
| R3, R6 | 2 | 10K? | - | | | ~ |
| R5 | 1 | 47K? | - | | | ~ |
| R7 | 1 | 120? | - | | | ~ |
| SC1 | 1 | Solar cell | € 48.76 | [Link](https://www.xunzel.com/store/en/producto/solarpower-15w-12v/) | 15W 12V | ~ |
| U1 | 1 | PTN78000H_EUS-5 | € 16.15 | [Link](https://www.ti.com/product/PTN78000H/part-details/PTN78000HAZ) | VRM | [Link](https://www.ti.com/lit/ds/symlink/ptn78000w.pdf) |
| U2 | 1 | MAX485E | € 0.14 | [Link](https://www.weylan-d.com/goods/detail/249405/) | RS485 Translate | [Link](https://datasheets.maximintegrated.com/en/ds/MAX1487E-MAX491E.pdf) |
| U3 | 1 | DHT22 | € 10.13 | [Link](https://malina314.com/proizvod/dht22-temperature-humidity-sensor-senzor-temperatura-vlaznost/) | Temp & Humidity sensor | [Link](http://akizukidenshi.com/download/ds/aosong/AM2302.pdf) |
| Y1 | 1 | R344A | € 1.74 | [Link](https://www.aliexpress.com/item/1005003063517015.html) | SAW | |

**Grand Total**: € 166.21 ($ 197.53)  
**Grand Total (Including shipping/VAT)**: € 209.49 ($ 248.97)

NOTE: Prices marked € 0.85 may be subject to change due to welcome deals.

# What might be added 
Using local LLM's to recognize cloud and other patterns to predict weather more accurately.
