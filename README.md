# weather-station
I'm presenting my project of a Field Weather Station containing anemometers and wind direction sensors, also ambient temp. and humidity sensors with a UHF transmitter in the field and an at home reciever(rtl-sdr). This also includes an RPi 4 (4GB RAM) that is recieving satellite images and displaying them in a mobile phone app.
# Why a weather station-Short Story
I live in the countryside and my family has large fruit plantations. By the microclimate of my area strong frosts in the spring time are common, that quite often hurts the fruits. We solve this issue by turning on our watering systems(the ground water keeps around 12 degrees Celsius at all times), but also by using air heaters to warm the area. So instead of waking up every 30 minutes to check the temps and having to go outside this idea was born. Main goal is that the weather station sends me phone notifications to a custom website/app. The weather station is located atleast 500m away from the house so the trick was getting reliable power, which I solved using a solar panel and a Lead Acid battery.
# How it functions 
Using an Arduino Nano I read the data from the sensors and send it with my UHF transmitter using ASK modulation(to be changed to FSK if too much interferences). At home using a reciever and an RPi 4 Model B (4GB), RTL SDR V4 and a homemade quadrifillar helix antenna to recieve satellite images and a Nagoya antenna for the UHF signals. Compare it and display on the website/app using the RPi.
# The BOM

| Reference | Qty | Value | Price | Link | Info | Datasheet |
|---|---|---|---|---|---|---|
| A1 | 1 | Arduino Nano | € 3.20 | [Link](https://www.aliexpress.com/item/1005009848194286.html) | | [Link](https://content.arduino.cc/assets/NANOEveryV3.0_sch.pdf) |
| AE1 | 1 | Antenna Shielded SMA | € 2.30 | [Link](https://www.aliexpress.com/item/1005008813991595.html) | | ~ |
| AE2 | 2 | Nagoya NA-771 | € 7.47 | [Link](https://www.aliexpress.com/item/1005008660133810.html?spm=a2g0o.productlist.main.1.54332Rrn2RrnbS&algo_pvid=6b1cd6b7-e759-4554-baba-a451a18d1cca&algo_exp_id=6b1cd6b7-e759-4554-baba-a451a18d1cca-0&pdp_ext_f=%7B%22order%22%3A%22663%22%2C%22spu_best_type%22%3A%22order%22%2C%22eval%22%3A%221%22%2C%22orig_sl_item_id%22%3A%221005008660133810%22%2C%22orig_item_id%22%3A%221005005862057777%22%2C%22fromPage%22%3A%22search%22%7D&pdp_npi=6%40dis%21EUR%219.50%214.75%21%21%2175.96%2137.98%21%4021BVTrB47erypG3tevi1U9Fv6BbNUBEiuiX%2112000046135982236%21sea%21SRB%210%21ABX%211%210%21n_tag%3A-29910%3Bd%3A3d40ec8a%3Bm03_new_user%3A-29895&curPageLogUid=peroKELiN18w&utparam-url=scene%3Asearch%7Cquery_from%3A%7Cx_object_id%3A1005008660133810%7C_p_origin_prod%3A1005005862057777) | High Gain Handheld Antenna | ~ |
| BT1 | 1 | 12V 7Ah Lead Acid | € 15.00 | [Link](https://borikplus.rs/proizvodi/olovne-baterije/) | Alternative capacity | ~ |
| C1 | 1 | 470uF | € 0.00 | [Link](https://www.aliexpress.com/item/1005008244407175.html) | I already have | ~ |
| C2 | 1 | 100uF | € 0.00 | [Link](https://www.aliexpress.com/item/1005005661484224.html) | I already have | ~ |
| C3 | 1 | 2pF | - | | | ~ |
| C4 | 1 | 2.2pF | - | | | ~ |
| C5 | 1 | 10pF | - | | | ~ |
| C6 | 1 | 470pF | - | | | ~ |
| D1 | 1 | LED_R | € 0.00 | [Link](https://www.aliexpress.com/item/1005010315277808.html) | I already have | ~ |
| J1, J2 | 1 | RS485 ANEMOMETER | € 26.53 | [Link](https://www.aliexpress.com/item/1005007059408469.html?spm=a2g0o.productlist.main.2.4a033928CMF5XY&algo_pvid=716ca457-5aab-4316-b2cb-04b983bb9b31&algo_exp_id=716ca457-5aab-4316-b2cb-04b983bb9b31-1&pdp_ext_f=%7B%22order%22%3A%2222%22%2C%22eval%22%3A%221%22%25) | RS485 type | ~ |
| J4 | 1 | RS485 WIND DIRECTION | € 23.82 | [Link](https://www.aliexpress.com/item/1005007059408469.html?spm=a2g0o.productlist.main.2.4a033928CMF5XY&algo_pvid=716ca457-5aab-4316-b2cb-04b983bb9b31&algo_exp_id=716ca457-5aab-4316-b2cb-04b983bb9b31-1&pdp_ext_f=%7B%22order%22%3A%2222%22%2C%22eval%22%3A%221%22%25) | RS485 type | ~ |
| U4 | 1 | SDLA12TA | € 1.75 | [Link](https://www.aliexpress.com/item/1005006641063540.html?spm=a2g0o.productlist.main.2.27095885CkBPLU&algo_pvid=e4be5066-8354-41d3-922b-3ee448c878b3&algo_exp_id=e4be5066-8354-41d3-922b-3ee448c878b3-1&pdp_ext_f=%7B%22order%22%3A%2225%22%2C%22eval%22%3A%221%22%25) | | [Link](https://easyeda.com/component/451d05428ff8431fb9c594829dfd0fbf) |
| R1 | 1 | 2k1? | € 0.00 | [Link](https://www.aliexpress.com/item/1005010688244614.html?spm=a2g0o.productlist.main.2.7d8fG8tEG8tEk2&algo_pvid=e197ad8d-f3f6-47ee-a63e-3a9a69ddded3&algo_exp_id=e197ad8d-f3f6-47ee-a63e-3a9a69ddded3-1&pdp_ext_f=%7B%22order%22%3A%225%22%2C%22eval%22%3A%221%22%252) | I already have | ~ |
| R2 | 1 | 220? | - | | | ~ |
| R3, R6 | 2 | 10K? | - | | | ~ |
| R5 | 1 | 47K? | - | | | ~ |
| R7 | 1 | 120? | - | | | ~ |
| SC1 | 1 | Solar cell | € 48.76 | [Link](https://www.xunzel.com/store/en/producto/solarpower-15w-12v/) | 15W 12V | ~ |
| U1 | 1 | DC-DC Buck Converter (LM2596) | € 2.00 | [Link](https://www.aliexpress.com/item/1005011900000000.html) | Voltage Regulator | ~ |
| U2 | 1 | MAX485E | € 4.39 | [Link](https://www.aliexpress.com/item/1005008248138246.html?spm=a2g0o.productlist.main.3.50414ee34ANwSC&algo_pvid=91f3ea3b-89b0-416b-b821-3a5b0b836404&algo_exp_id=91f3ea3b-89b0-416b-b821-3a5b0b836404-2&pdp_ext_f=%7B%22order%22%3A%224614%22%2C%22spu_best_type%22%3A%22order%22%2C%22eval%22%3A%221%22%2C%22orig_sl_item_id%22%3A%221005008248138246%22%2C%22orig_item_id%22%3A%221005008332469260%22%2C%22fromPage%22%3A%22search%22%7D&pdp_npi=6%40dis%21EUR%218.78%214.39%21%21%2170.18%2135.09%21%40211b615317711213649861629e601f%2112000044371383487%21sea%21SRB%210%21ABX%211%210%21n_tag%3A-29910%3Bd%3A3d40ec8a%3Bm03_new_user%3A-29895&curPageLogUid=iT1tcIOUDtud&utparam-url=scene%3Asearch%7Cquery_from%3A%7Cx_object_id%3A1005008248138246%7C_p_origin_prod%3A1005008332469260) | RS485 Translate | [Link](https://datasheets.maximintegrated.com/en/ds/MAX1487E-MAX491E.pdf) |
| U3 | 1 | DHT22 | € 3.01 | [Link](https://www.aliexpress.com/item/1005012000000000.html) | Temp & Humidity sensor | [Link](http://akizukidenshi.com/download/ds/aosong/AM2302.pdf) |
| RF1 | 1 | 433MHz RF Kit | € 3.88 | [Link](https://www.aliexpress.com/item/1005005853468512.html) | Transmitter + Receiver | |
| U5 | 2 | DS18B20 | € 4.20 | [Link](https://www.aliexpress.com/item/1005010363252763.html?spm=a2g0o.productlist.main.3.29524241FUBE8k&algo_pvid=3deaecd0-814a-478f-b2ff-2f3b8ef2c44d&algo_exp_id=3deaecd0-814a-478f-b2ff-2f3b8ef2c44d-2&pdp_ext_f=%7B%22order%22%3A%2274%22%2C%22eval%22%3A%221%22%2C%22fromPage%22%3A%22search%22%7D&pdp_npi=6%40dis%21EUR%219.80%210.85%21%21%2178.32%216.80%21%40211b61a417711198131738996e5d08%2112000052189971493%21sea%21SRB%210%21ABX%211%210%21n_tag%3A-29910%3Bd%3A3d40ec8a%3Bm03_new_user%3A-29895%3BpisId%3A5000000198944678&curPageLogUid=zeX5XbhmfOld&utparam-url=scene%3Asearch%7Cquery_from%3A%7Cx_object_id%3A1005010363252763%7C_p_origin_prod%3A) | Waterproof Temp Sensor | [Link](https://datasheets.maximintegrated.com/en/ds/DS18B20.pdf) |
| U6 | 1 | Raspberry Pi 4 Model B (4GB) | € 64.00 | [Link](https://www.aliexpress.com/item/1005012300000000.html) | Receiver Host | ~ |
| U7 | 1 | RTL-SDR Blog V4 | € 30.78 | [Link](https://www.rtl-sdr.com/buy-rtl-sdr-dvb-t-dongles/) | Official Receiver | ~ |
| AC1 | 1 | Micro-Type Diplexer Duplex Filter | € 13.66 | [Link](https://www.aliexpress.com/item/1005005862057777.html?spm=a2g0o.productlist.main.4.54332Rrn2RrnbS&algo_pvid=6b1cd6b7-e759-4554-baba-a451a18d1cca&algo_exp_id=6b1cd6b7-e759-4554-baba-a451a18d1cca-4&pdp_ext_f=%7B%22order%22%3A%2272%22%2C%22eval%22%3A%221%22%25) | 10W V/U 2m 70cm | ~ |
| PCB | 1 | JLCPCB PCB Manufacturing | € 19.61 | [Link](https://jlcpcb.com/) | $20.75 | ~ |

**Grand Total**: € 273.63 ($ 290.05)  
**Grand Total (Including shipping/VAT)**: € 343.01 ($ 363.59)

# What might be added 
Using local LLM's to recognize cloud and other patterns to predict weather more accurately.

#NAPRED ZVEZDO