function getWeather() {
    const cityInput = document.getElementById('cityInput');
    const weatherInfo = document.getElementById('weatherInfo');
    const error = document.getElementById('error');
    
    const city = cityInput.value.trim();
    
    if (!city) {
        showError('Please enter a city name');
        return;
    }
    
    // Show loading state
    weatherInfo.innerHTML = '<p>Loading weather information...</p>';
    error.style.display = 'none';
    
    // Make request to our servlet
    fetch(`weather?city=${encodeURIComponent(city)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('City not found');
            }
            return response.json();
        })
        .then(data => {
            // Create weather info HTML
            const weatherHtml = `
                <div class="weather-detail">
                    <i class="fas fa-thermometer-half"></i>
                    <span>Temperature: ${Math.round(data.main.temp)}°C</span>
                </div>
                <div class="weather-detail">
                    <i class="fas fa-tint"></i>
                    <span>Humidity: ${data.main.humidity}%</span>
                </div>
                <div class="weather-detail">
                    <i class="fas fa-cloud"></i>
                    <span>Weather: ${data.weather[0].description}</span>
                </div>
                <div class="weather-detail">
                    <i class="fas fa-wind"></i>
                    <span>Wind Speed: ${data.wind.speed} m/s</span>
                </div>
            `;
            
            weatherInfo.innerHTML = weatherHtml;
        })
        .catch(err => {
            showError(err.message);
        });
}

function showError(message) {
    const error = document.getElementById('error');
    error.textContent = message;
    error.style.display = 'block';
    document.getElementById('weatherInfo').innerHTML = '';
}

// Allow Enter key to trigger search
document.getElementById('cityInput').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        getWeather();
    }
}); 