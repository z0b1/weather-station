from skyfield.api import Topos, load, EarthSatellite
from datetime import datetime, timedelta
import requests
import os

# SET YOUR LOCATION
# because i'm using a QFH antenna, accuracy here is key 
# to catch the signal the moment it clears the horizon!
LATITUDE = 44.7165 # latitude
LONGITUDE = 19.6257 # longitude
ELEVATION = 113 # meters above sea level

# TLE Source
TLE_URL = "https://celestrak.org/NORAD/elements/weather.txt"
TLE_FILE = os.path.join(os.path.dirname(__file__), "weather.tle")

class SatelliteTracker:
    def __init__(self, lat=LATITUDE, lon=LONGITUDE, alt=ELEVATION):
        self.ts = load.timescale()
        self.observer = Topos(latitude_degrees=lat, longitude_degrees=lon, elevation_m=alt)
        self.satellites = {}
        self.update_tles()

    def update_tles(self):
        """Downloads fresh TLE data if older than 24h."""
        needs_update = True
        if os.path.exists(TLE_FILE):
            file_age = datetime.now() - datetime.fromtimestamp(os.path.getmtime(TLE_FILE))
            if file_age.days < 1:
                needs_update = False
        
        if needs_update:
            try:
                print("[*] Downloading fresh TLE data for Meteor M2...")
                r = requests.get(TLE_URL, timeout=10)
                with open(TLE_FILE, 'wb') as f:
                    f.write(r.content)
            except Exception as e:
                print(f"[!] Warning: Failed to update TLEs: {e}")
        
        # Load satellites from file. 
        # Celestrak weather.txt includes Meteor M2-3, M2-4
        try:
            sats = load.tle_file(TLE_FILE)
            targets = ["METEOR-M2 3", "METEOR-M2 4"]
            self.satellites = {s.name: s for s in sats if any(t in s.name for t in targets)}
        except Exception as e:
            print(f"[!] Error loading TLE file: {e}")

    def is_pass_active(self, min_elevation=5):
        """
        Checks if any target satellite is currently above the horizon.
        With a QFH antenna, we can start as low as 5 degrees!
        """
        t = self.ts.now()
        
        for name, sat in self.satellites.items():
            difference = sat - self.observer
            topocentric = difference.at(t)
            alt, az, distance = topocentric.altaz()
            
            if alt.degrees > min_elevation:
                return {
                    "name": name,
                    "elevation": alt.degrees,
                    "azimuth": az.degrees
                }
        
        return None

    def get_next_pass(self):
        """Finds the next upcoming pass among all tracked satellites."""
        t0 = self.ts.now()
        t1 = self.ts.utc(t0.utc_datetime() + timedelta(days=1))
        
        next_passes = []
        
        for name, sat in self.satellites.items():
            times, events = sat.find_events(self.observer, t0, t1, altitude_degrees=10)
            if len(times) >= 3:
                next_passes.append({
                    "name": name,
                    "aos": times[0].utc_datetime(),
                    "max": times[1].utc_datetime(),
                    "los": times[2].utc_datetime()
                })
        
        if not next_passes:
            return None
            
        # sort by aos time
        next_passes.sort(key=lambda x: x['aos'])
        return next_passes[0]

if __name__ == "__main__":
    tracker = SatelliteTracker()
    active = tracker.is_pass_active()
    if active:
        print(f"[!] {active['name']} is active! Elevation: {active['elevation']:.1f}\u00b0")
    else:
        nxt = tracker.get_next_pass()
        if nxt:
            print(f"[*] Next pass: {nxt['name']} at {nxt['aos'].strftime('%H:%M:%S')} UTC")
