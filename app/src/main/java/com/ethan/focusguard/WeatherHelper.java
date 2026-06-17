package com.ethan.focusguard;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.util.Locale;

public final class WeatherHelper {
    public interface Callback {
        void onDone(boolean success);
    }

    private static final long WEATHER_MAX_AGE_MS = 30L * 60L * 1000L;

    private WeatherHelper() {}

    public static boolean isStale(Context context) {
        long updated = FocusManager.getWeatherUpdated(context);
        return updated == 0L || System.currentTimeMillis() - updated > WEATHER_MAX_AGE_MS;
    }

    public static void refresh(Context context, Callback callback) {
        Context app = context.getApplicationContext();
        new Thread(() -> {
            boolean success = false;
            try {
                String city = FocusManager.getWeatherCity(app);
                WeatherLocation location = lookupLocation(city);
                if (location == null) throw new IllegalStateException("Location not found");
                WeatherResult result = lookupWeather(location);
                if (result == null) throw new IllegalStateException("Weather not found");
                String summary = location.name + "  •  " + Math.round(result.temperature) + "°C  •  " + weatherCode(result.code);
                FocusManager.setWeatherSummary(app, summary);
                success = true;
            } catch (Exception e) {
                FocusManager.setWeatherSummary(app, "Weather unavailable  •  check city/internet");
            }
            if (callback != null) callback.onDone(success);
        }).start();
    }

    private static WeatherLocation lookupLocation(String city) throws Exception {
        String encoded = URLEncoder.encode(city == null ? "London" : city, "UTF-8");
        String url = "https://geocoding-api.open-meteo.com/v1/search?name=" + encoded + "&count=1&language=en&format=json";
        JSONObject root = new JSONObject(get(url));
        JSONArray results = root.optJSONArray("results");
        if (results == null || results.length() == 0) return null;
        JSONObject first = results.getJSONObject(0);
        String name = first.optString("name", city);
        String country = first.optString("country_code", "");
        double lat = first.getDouble("latitude");
        double lon = first.getDouble("longitude");
        return new WeatherLocation(name + (country.isEmpty() ? "" : ", " + country), lat, lon);
    }

    private static WeatherResult lookupWeather(WeatherLocation location) throws Exception {
        String url = String.format(Locale.US,
                "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current_weather=true&timezone=auto&temperature_unit=celsius",
                location.latitude,
                location.longitude);
        JSONObject root = new JSONObject(get(url));
        JSONObject current = root.optJSONObject("current_weather");
        if (current == null) return null;
        return new WeatherResult(current.getDouble("temperature"), current.optInt("weathercode", -1));
    }

    private static String get(String urlText) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlText);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(9000);
            connection.setReadTimeout(9000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "FocusGuard/1.0");
            int code = connection.getResponseCode();
            InputStream stream = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            if (code < 200 || code >= 300) throw new IllegalStateException("HTTP " + code);
            return sb.toString();
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String weatherCode(int code) {
        switch (code) {
            case 0: return "Clear";
            case 1:
            case 2:
            case 3: return "Cloudy";
            case 45:
            case 48: return "Fog";
            case 51:
            case 53:
            case 55:
            case 56:
            case 57: return "Drizzle";
            case 61:
            case 63:
            case 65:
            case 66:
            case 67:
            case 80:
            case 81:
            case 82: return "Rain";
            case 71:
            case 73:
            case 75:
            case 77:
            case 85:
            case 86: return "Snow";
            case 95:
            case 96:
            case 99: return "Storm";
            default: return "Weather";
        }
    }

    private static final class WeatherLocation {
        final String name;
        final double latitude;
        final double longitude;

        WeatherLocation(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    private static final class WeatherResult {
        final double temperature;
        final int code;

        WeatherResult(double temperature, int code) {
            this.temperature = temperature;
            this.code = code;
        }
    }
}
