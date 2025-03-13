package org.example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.net.InetSocketAddress;
import java.net.BindException;
import java.util.HashMap;
import java.util.Map;
import java.net.URLDecoder;
import org.json.JSONObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WeatherApp {
    // Updated API key provided by you
    private static final String API_KEY = "0aa3d3d08d8d40d591b93025251303";
    private static final String API_URL = "http://api.weatherapi.com/v1/current.json";
    private static final String WEBAPP_PATH = "webapp";
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(DEFAULT_PORT), 0);
            server.createContext("/weather", new WeatherHandler());
            server.createContext("/", new StaticFileHandler());
            server.setExecutor(null);
            server.start();

            System.out.println("Server started successfully on port " + DEFAULT_PORT);
            System.out.println("Current working directory: " + Paths.get(".").toAbsolutePath());
            System.out.println("Open http://localhost:" + DEFAULT_PORT + " in your browser");

            Thread.currentThread().join();
        } catch (BindException e) {
            System.err.println("Failed to bind to port " + DEFAULT_PORT + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (InterruptedException e) {
            System.err.println("Server interrupted: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    static class WeatherHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Handle CORS preflight requests
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                System.out.println("Received query: " + query);

                Map<String, String> params = parseQuery(query);
                String city = params.get("city");
                System.out.println("City parameter: " + city);

                if (city == null || city.trim().isEmpty()) {
                    System.out.println("Error: City parameter is missing");
                    redirectToIndex(exchange);
                    return;
                }

                try {
                    String weatherData = getWeatherData(city);
                    System.out.println("Weather API Response: " + weatherData);

                    JSONObject json = new JSONObject(weatherData);
                    JSONObject location = json.getJSONObject("location");
                    JSONObject current = json.getJSONObject("current");

                    // WeatherAPI returns local time directly in the "location" object (format: "yyyy-MM-dd HH:mm")
                    String localtime = location.getString("localtime");
                    DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime localDateTime = LocalDateTime.parse(localtime, parser);

                    // Format the date and time
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                    System.out.println("Formatted Date: " + dateFormatter.format(localDateTime));
                    System.out.println("Formatted Time: " + timeFormatter.format(localDateTime));

                    // Extract weather details in metric units
                    double temperature = current.getDouble("temp_c");
                    double feelsLike = current.getDouble("feelslike_c");
                    String weatherDescription = current.getJSONObject("condition").getString("text");
                    double visibility = current.getDouble("vis_km");
                    double windSpeed = current.getDouble("wind_kph"); // Already in km/h
                    int cloudCover = current.getInt("cloud");

                    // Generate the HTML response by replacing placeholders in result.html
                    String html = Files.readString(Paths.get(WEBAPP_PATH, "result.html"))
                        .replace("${city}", location.getString("name"))
                        .replace("${temperature}", String.format("%.0f", temperature))
                        .replace("${date}", dateFormatter.format(localDateTime))
                        .replace("${currentTime}", timeFormatter.format(localDateTime))
                        .replace("${weatherCondition}", capitalize(weatherDescription))
                        .replace("${feelsLike}", String.format("%.0f", feelsLike))
                        .replace("${visibility}", String.format("%.1f", visibility))
                        .replace("${windSpeed}", String.format("%.1f", windSpeed))
                        .replace("${cloudCover}", String.valueOf(cloudCover));

                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    byte[] responseBytes = html.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBytes);
                    }
                } catch (IOException e) {
                    System.out.println("Error fetching weather data: " + e.getMessage());
                    e.printStackTrace();
                    redirectToIndex(exchange);
                } catch (Exception e) {
                    System.out.println("Unexpected error: " + e.getMessage());
                    e.printStackTrace();
                    redirectToIndex(exchange);
                }
            } else {
                redirectToIndex(exchange);
            }
        }

        private void redirectToIndex(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Location", "/");
            exchange.sendResponseHeaders(302, -1);
        }

        private Map<String, String> parseQuery(String query) {
            Map<String, String> params = new HashMap<>();
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=", 2);
                    if (pair.length > 1) {
                        try {
                            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8.toString());
                            String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8.toString());
                            params.put(key, value);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return params;
        }

        private String capitalize(String str) {
            if (str == null || str.isEmpty()) return str;
            return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
        }
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            path = path.equals("/") ? "/index.html" : path;
            Path filePath = Paths.get(WEBAPP_PATH, path.startsWith("/") ? path.substring(1) : path);
            System.out.println("Attempting to serve file: " + filePath.toString());
            System.out.println("File exists: " + Files.exists(filePath));
            System.out.println("Absolute path: " + filePath.toAbsolutePath());

            if (!Files.exists(filePath)) {
                String response = "404 (Not Found)\nFile: " + filePath.toString();
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
                return;
            }

            String contentType = getContentType(path);
            exchange.getResponseHeaders().add("Content-Type", contentType);
            exchange.sendResponseHeaders(200, Files.size(filePath));

            try (OutputStream os = exchange.getResponseBody();
                 InputStream is = Files.newInputStream(filePath)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            }
        }

        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=UTF-8";
            if (path.endsWith(".css")) return "text/css; charset=UTF-8";
            if (path.endsWith(".js")) return "text/javascript; charset=UTF-8";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            return "application/octet-stream";
        }
    }

    // Fetch weather data from WeatherAPI
    private static String getWeatherData(String city) throws IOException {
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.toString());
        String urlString = String.format("%s?key=%s&q=%s", API_URL, API_KEY, encodedCity);
        System.out.println("Requesting URL: " + urlString);

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } catch (IOException e) {
            System.err.println("Failed to fetch weather data for city " + city + ": " + e.getMessage());
            throw e;
        } finally {
            connection.disconnect();
        }
    }
}
