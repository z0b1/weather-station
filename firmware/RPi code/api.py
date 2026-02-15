from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from fastapi.responses import FileResponse
import pandas as pd
import os
import json
from typing import List, Optional
from datetime import datetime, timedelta

app = FastAPI(title="Weather Station API")

# Allow CORS for the frontend
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

CSV_FILE = "weather_data.csv"
RECORDINGS_DIR = "recordings"

# Ensure recordings directory exists
if not os.path.exists(RECORDINGS_DIR):
    os.makedirs(RECORDINGS_DIR)

# Mount recordings as static files to serve images to the app
app.mount("/view-recordings", StaticFiles(directory=RECORDINGS_DIR), name="recordings")

def get_data():
    if not os.path.exists(CSV_FILE):
        return None
    try:
        df = pd.read_csv(CSV_FILE)
        return df
    except Exception as e:
        print(f"Error reading CSV: {e}")
        return None

@app.get("/weather/latest")
async def get_latest_weather():
    df = get_data()
    if df is None or df.empty:
        raise HTTPException(status_code=404, detail="No weather data available")
    
    latest = df.iloc[-1].to_dict()
    return latest

@app.get("/weather/history")
async def get_weather_history(hours: int = 24):
    df = get_data()
    if df is None or df.empty:
        return []
    
    df['Timestamp'] = pd.to_datetime(df['Timestamp'])
    cutoff = datetime.now() - timedelta(hours=hours)
    history = df[df['Timestamp'] >= cutoff]
    
    return history.to_dict(orient="records")

@app.get("/satellites")
async def list_recordings():
    recordings = []
    if os.path.exists(RECORDINGS_DIR):
        for entry in os.scandir(RECORDINGS_DIR):
            if entry.is_dir():
                # Look for image files in the recording directory
                images = [f.name for f in os.scandir(entry.path) if f.name.lower().endswith(('.png', '.jpg', '.jpeg'))]
                recordings.append({
                    "name": entry.name,
                    "timestamp": datetime.fromtimestamp(entry.stat().st_mtime).isoformat(),
                    "images": images,
                    "path": entry.name
                })
    
    return sorted(recordings, key=lambda x: x['timestamp'], reverse=True)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
