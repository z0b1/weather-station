import subprocess
import json
import csv
import os
import datetime
import time
import signal
import requests

from tracker import SatelliteTracker

CSV_FILE = "weather_data.csv"
SDR_FREQUENCY = "433.92M"
NTFY_TOPIC = "weatherstat"
FROST_THRESHOLD = 2.0

SAT_CONFIG = {
    "meteor-m2 3": {"freq": "137.100M", "pipeline": "meteor_m2_lrpt"},
    "meteor-m2 4": {"freq": "137.900M", "pipeline": "meteor_m2_lrpt"}
}

DECODER_COMMAND = [
    "rtl_433",
    "-f", SDR_FREQUENCY,
    "-R", "110",
    "-F", "json"
]


class WeatherSDR:

    def __init__(self):
        self.process = None
        self.is_satellite_recording = False
        self.tracker = SatelliteTracker()

    def start_receiver(self):
        print(f"[*] starting receiver on {SDR_FREQUENCY}")
        self.process = subprocess.Popen(
            DECODER_COMMAND,
            stdout=subprocess.PIPE,
            stderr=subprocess.DEVNULL,
            universal_newlines=True
        )
        os.set_blocking(self.process.stdout.fileno(), False)

    def stop_receiver(self):
        if self.process:
            print("[*] stopping receiver")
            self.process.terminate()
            self.process.wait()
            self.process = None

    def record_satellite(self, sat_info):
        sat_name = sat_info["name"].lower()

        config = None
        for key in SAT_CONFIG:
            if key in sat_name:
                config = SAT_CONFIG[key]
                break

        if not config:
            config = {"freq": "137.100M", "pipeline": "meteor_m2_lrpt"}

        now_str = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
        output_dir = os.path.join(
            "recordings",
            f"{sat_name.replace(' ', '_')}_{now_str}"
        )
        os.makedirs(output_dir, exist_ok=True)

        cmd = [
            "satdump", "live", config["pipeline"], output_dir,
            "--source", "rtlsdr",
            "--samplerate", "1024000",
            "--frequency", config["freq"].replace("M", "000000"),
            "--gain", "38",
            "--timeout", str(int(sat_info.get("duration", 600)))  # Defaults to 600 seconds if missing
        ]

        print(f"[!] recording {sat_name.upper()}")

        try:
            subprocess.run(cmd, timeout=1800)
            print(f"[+] done. saved to {output_dir}")
        except Exception as e:
            print(f"[!] error: {e}")

    def hex_to_string(self, hex_str):
        try:
            bytes_data = bytes.fromhex(hex_str)
            return bytes_data.decode("ascii", errors="ignore")
        except Exception:
            return None

    def save_data(self, packet):
        now = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        packet = packet.split(";")[0] + ";"
        print(f"[{now}] {packet}")

        try:
            parts = packet.replace(";", "").split(",")
            data_map = {}

            for p in parts:
                if ":" in p:
                    k, v = p.split(":")
                    data_map[k] = v

            row = [
                now,
                data_map.get("S", "0.0"),
                data_map.get("D", "0"),
                data_map.get("T", "0.0"),
                data_map.get("H", "0"),
                data_map.get("ST", "0.0"),
                data_map.get("SU", "0.0")
            ]

            file_exists = os.path.isfile(CSV_FILE)

            with open(CSV_FILE, "a", newline="") as f:
                writer = csv.writer(f)
                if not file_exists:
                    writer.writerow(
                        ["Timestamp", "Speed", "Heading", "Temp", "Hum", "SoilTemp", "SurfTemp"]
                    )
                writer.writerow(row)

            return row

        except Exception as e:
            print(f"[!] Save error: {e}")
            return None

    def send_notification(self, message):
        print(f"[*] Sending notification: {message}")
        try:
            requests.post(
                f"https://ntfy.sh/{NTFY_TOPIC}",
                data=message,
                headers={
                    "Title": "Weather Station Alert",
                    "Priority": "high",
                    "Tags": "warning,snowflake"
                },
                timeout=5
            )
        except Exception as e:
            print(f"[!] Notification failed: {e}")

    def check_alerts(self, row):
        try:
            temp = float(row[3])
            if temp < FROST_THRESHOLD:
                self.send_notification(
                    f"Frost Warning! Temperature is {temp}°C"
                )
        except (ValueError, IndexError):
            pass

    def run(self):
        self.start_receiver()

        try:
            while True:
                active_pass = self.tracker.is_pass_active(min_elevation=5)

                if active_pass and not self.is_satellite_recording:
                    self.stop_receiver()
                    self.is_satellite_recording = True
                    self.record_satellite(active_pass)
                    self.is_satellite_recording = False
                    self.start_receiver()

                if self.process:
                    try:
                        line = self.process.stdout.readline()
                    except (IOError, TypeError):
                        line = None

                    if line:
                        try:
                            data = json.loads(line)

                            if "payload" in data:
                                raw_hex = data["payload"]
                                payload = self.hex_to_string(raw_hex)

                                if payload and "S:" in payload:
                                    row = self.save_data(payload)
                                    if row:
                                        self.check_alerts(row)

                        except json.JSONDecodeError:
                            continue

                time.sleep(0.1)

        except KeyboardInterrupt:
            self.stop_receiver()


if __name__ == "__main__":
    receiver = WeatherSDR()
    receiver.run()
    #NAPRED ZVEZDO 
