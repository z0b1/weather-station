import subprocess
import json
import csv
import os
import datetime
import time
import signal
try:
    import RPi.GPIO as GPIO
except ImportError:
    # dummy class for non rpi testing
    class GPIO:
        BCM = BOARD = OUT = HIGH = LOW = 0
        @staticmethod
        def setmode(a): pass
        @staticmethod
        def setup(a, b): pass
        @staticmethod
        def output(a, b): pass
        @staticmethod
        def cleanup(): pass

from tracker import SatelliteTracker

# config
CSV_FILE = "weather_data.csv"
SDR_FREQUENCY = "433.92M"
ANTENNA_SWITCH_PIN = 17  # GPIO pin for antenna mode relay

# Satellite freq & pipeline map
SAT_CONFIG = {
    "meteor-m2 3": {"freq": "137.100M", "pipeline": "meteor_m2_lrpt"},
    "meteor-m2 4": {"freq": "137.900M", "pipeline": "meteor_m2_lrpt"}
}

DECODER_COMMAND = [
    "rtl_433",
    "-f", SDR_FREQUENCY,
    "-M", "json",
    "-X", "n=WeatherStation,m=OOK_PWM,s=250,l=600,g=400,r=2000,bits>=40",
    "-F", "json"
]

class WeatherSDR:
    def __init__(self):
        self.process = None
        self.is_satellite_recording = False
        self.tracker = SatelliteTracker()
    
    # hardware setup
    GPIO.setmode(GPIO.BCM)
    GPIO.setup(ANTENNA_SWITCH_PIN, GPIO.OUT)
    self.set_antenna_mode('weather')

    def set_antenna_mode(self, mode):
        """Toggles antenna between UHF (Weather) and VHF (Sat)."""
        if mode == 'sat':
            print("[*] Switching antenna to SAT MODE (VHF/QFH)...")
            GPIO.output(ANTENNA_SWITCH_PIN, GPIO.HIGH)
        else:
            print("[*] Switching antenna to WEATHER MODE (UHF/Nagoya)...")
            GPIO.output(ANTENNA_SWITCH_PIN, GPIO.LOW)
        time.sleep(1)
        
        def start_receiver(self):
            print(f"[*] Starting Weather Station Receiver on {SDR_FREQUENCY}...")
            self.process = subprocess.Popen(
                DECODER_COMMAND,
                stdout=subprocess.PIPE,
                stderr=subprocess.DEVNULL,
                universal_newlines=True
            )

        def stop_receiver(self):
            if self.process:
                print("[*] Stopping Weather Receiver to free up SDR...")
                self.process.terminate()
                self.process.wait()
                self.process = None

        def record_satellite(self, sat_info):
            """Triggers SatDump to record and decode a satellite pass."""
            self.set_antenna_mode('sat')
            
            sat_name = sat_info['name'].lower()
            config = None
            for key in SAT_CONFIG:
                if key in sat_name:
                    config = SAT_CONFIG[key]
                    break
            
            if not config:
                config = {"freq": "137.100M", "pipeline": "meteor_m2_lrpt"}

            now_str = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
            output_dir = os.path.join("recordings", f"{sat_name.replace(' ', '_')}_{now_str}")
            os.makedirs(output_dir, exist_ok=True)
            # SatDump CLI commands
            cmd = [
                "satdump", config["pipeline"], "iqn",
                "--source", "rtl_sdr",
                "--samplerate", "1.024e6",
                "--frequency", config["freq"],
                "--gain", "45",
                "--path", output_dir,
                "--timeout", "60"
            ]

            print(f"[!] RECORDING {sat_name.upper()} at {config['freq']}...")
            try:
                subprocess.run(cmd, timeout=1800)
                print(f"[+] Pass complete. Files in: {output_dir}")
            except Exception as e:
                print(f"[!] SatDump error: {e}")
            finally:
                self.set_antenna_mode('weather')

        def hex_to_string(self, hex_str):
            try:
                bytes_data = bytes.fromhex(hex_str)
                return bytes_data.decode('ascii', errors='ignore')
            except:
                return None

        def save_data(self, packet):
            now = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            packet = packet.split(';')[0] + ';'
            print(f"[{now}] Sensor Data: {packet}")
            
            try:
                parts = packet.replace(';', '').split(',')
                row = [now] + [p.split(':')[-1] for p in parts]
                
                file_exists = os.path.isfile(CSV_FILE)
                with open(CSV_FILE, 'a', newline='') as f:
                    writer = csv.writer(f)
                    if not file_exists:
                        writer.writerow(["Timestamp", "Speed", "Heading", "Temp", "Hum"])
                    writer.writerow(row)
            except:
                pass

        def run(self):
            self.start_receiver()
            
            try:
                while True:
                    # check for sat passes
                    active_pass = self.tracker.is_pass_active(min_elevation=5)
                    
                    if active_pass and not self.is_satellite_recording:
                        self.stop_receiver()
                        self.is_satellite_recording = True
                        self.record_satellite(active_pass)
                        self.is_satellite_recording = False
                        self.start_receiver()

                    # read weather data
                    if self.process:
                        line = self.process.stdout.readline()
                        if line:
                            try:
                                data = json.loads(line)
                                if "codes" in data:
                                    raw_hex = data["codes"][0]
                                    payload = self.hex_to_string(raw_hex)
                                    if payload and "S:" in payload:
                                        self.save_data(payload)
                            except json.JSONDecodeError:
                                continue

                    time.sleep(0.1)
            except KeyboardInterrupt:
                self.stop_receiver()
                GPIO.cleanup()

                def save_data(self, packet):
                    """Appends weather data to CSV."""
                    now = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
                    packet = packet.split(';')[0] + ';'
                    print(f"[{now}] Sensor Data: {packet}")
                    
                    try:
                        parts = packet.replace(';', '').split(',')
                        row = [now] + [p.split(':')[-1] for p in parts]
                        
                        file_exists = os.path.isfile(CSV_FILE)
                        with open(CSV_FILE, 'a', newline='') as f:
                            writer = csv.writer(f)
                            if not file_exists:
                                writer.writerow(["Timestamp", "Speed", "Heading", "Temp", "Hum"])
                            writer.writerow(row)
                    except:
                        pass
                    if __name__ == "__main__":
                        receiver = WeatherSDR()
                        receiver.run()