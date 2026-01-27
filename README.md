# weather-station
I'm presenting my project of a Field Weather Station containing anemometers and wind direction sensors, also ambient temp. and humidity sensors with a homemade UHF transmitter in the field and an at home reciever(also homemade). This also includes an RPi3 B+ that is recieving satellite images and displaying them in a mobile phone app.
# Why a weather station-Short Story
I live in the countryside and my family has large fruit plantations. By the microclimate of my area strong frosts in the spring time are common, that quite often hurts the fruits. We solve this issue by turning on our watering systems(the ground water keeps around 12 degrees Celsius at all times), but also by using air heaters to warm the area. So instead of waking up every 30 minutes to check the temps and having to go outside this idea was born. Main goal is that the weather station sends me phone notifications to a custom website/app. The weather station is located atleast 500m away from the house so the trick was getting reliable power. Also I'm a licensed ham radio operator with a US and Serbian licence thats why I decided to make my own transmitter and recievers at home.
# How it functions 
Using an Arduino Nano I read the data from the sensors and send it with my UHF transmitter using OOK modulation(to be changed to FSK if too much interferences). At home using a reciever and an RPi 3 B+, RTL SDR V4 and a homemade dipole to recieve NOAA images and process the sensor data. Compare it and display on the website/app using the RPi.

# What might be added 
Using LLM's to recognize cloud and other patterns to predict weather more accurately.
