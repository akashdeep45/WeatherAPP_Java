package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SimpleWeatherApp {
    private static final String API_KEY = "YOUR_API_KEY"; // Replace with your OpenWeatherMap API key
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";
    
    public static void main(String[] args) {
        // Create the main window
        JFrame frame = new JFrame("Simple Weather App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout(10, 10));
        
        // Create components
        JPanel topPanel = new JPanel(new FlowLayout());
        JTextField cityField = new JTextField(20);
        JButton searchButton = new JButton("Get Weather");
        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        
        // Add components
        topPanel.add(new JLabel("Enter city: "));
        topPanel.add(cityField);
        topPanel.add(searchButton);
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        
        // Add button action
        searchButton.addActionListener(e -> {
            String city = cityField.getText().trim();
            if (!city.isEmpty()) {
                try {
                    String weatherData = getWeatherData(city);
                    resultArea.setText("Weather for " + city + ":\n\n" + weatherData);
                } catch (Exception ex) {
                    resultArea.setText("Error: " + ex.getMessage());
                }
            }
        });
        
        // Show the window
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private static String getWeatherData(String city) throws Exception {
        String urlString = String.format("%s?q=%s&appid=%s&units=metric", API_URL, city, API_KEY);
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            // Simple parsing of the response
            String jsonStr = response.toString();
            StringBuilder result = new StringBuilder();
            
            // Extract temperature
            int tempIndex = jsonStr.indexOf("\"temp\":");
            if (tempIndex != -1) {
                int start = tempIndex + 7;
                int end = jsonStr.indexOf(",", start);
                String temp = jsonStr.substring(start, end);
                result.append("Temperature: ").append(temp).append("°C\n");
            }
            
            // Extract humidity
            int humIndex = jsonStr.indexOf("\"humidity\":");
            if (humIndex != -1) {
                int start = humIndex + 10;
                int end = jsonStr.indexOf(",", start);
                String humidity = jsonStr.substring(start, end);
                result.append("Humidity: ").append(humidity).append("%\n");
            }
            
            // Extract weather description
            int descIndex = jsonStr.indexOf("\"description\":\"");
            if (descIndex != -1) {
                int start = descIndex + 14;
                int end = jsonStr.indexOf("\"", start);
                String desc = jsonStr.substring(start, end);
                result.append("Description: ").append(desc);
            }
            
            return result.toString();
        } finally {
            conn.disconnect();
        }
    }
} 