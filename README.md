# weather-station
I'm presenting my project of a Field Weather Station containing anemometers and wind direction sensors, also ambient temp. and humidity sensors with a UHF transmitter in the field and an at home reciever(rtl-sdr). This also includes an RPi3 B+ that is recieving satellite images and displaying them in a mobile phone app.
# Why a weather station-Short Story
I live in the countryside and my family has large fruit plantations. By the microclimate of my area strong frosts in the spring time are common, that quite often hurts the fruits. We solve this issue by turning on our watering systems(the ground water keeps around 12 degrees Celsius at all times), but also by using air heaters to warm the area. So instead of waking up every 30 minutes to check the temps and having to go outside this idea was born. Main goal is that the weather station sends me phone notifications to a custom website/app. The weather station is located atleast 500m away from the house so the trick was getting reliable power, which I solved using a solar panel and a Lead Acid battery.
# How it functions 
Using an Arduino Nano I read the data from the sensors and send it with my UHF transmitter using ASK modulation(to be changed to FSK if too much interferences). At home using a reciever and an RPi 3 B+, RTL SDR V4 and a homemade quadrifillar helix antenna to recieve satellite images and a Nagoya antenna for the UHF signals. Compare it and display on the website/app using the RPi.
# The BOM

# What might be added 
Using local LLM's to recognize cloud and other patterns to predict weather more accurately.
