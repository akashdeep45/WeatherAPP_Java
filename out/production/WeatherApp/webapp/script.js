document.addEventListener('DOMContentLoaded', function() {
    const weatherForm = document.getElementById('weatherForm');
    const cityInput = document.getElementById('city');
    const errorMsg = document.getElementById('errorMsg');
    const weatherIcon = document.getElementById('weather-icon');

    // Show weather data if it's available from JSP
    const cityName = document.getElementById('cityName');
    if (cityName && cityName.textContent.trim() !== '${cityName}') {
        weatherResult.style.display = 'block';
    }

    // Form validation
    weatherForm.addEventListener('submit', function(event) {
        event.preventDefault(); // Prevent default form submission

        if (cityInput.value.trim() === '') {
            errorMsg.style.display = 'block'; // Show error message
        } else {
            errorMsg.style.display = 'none'; // Hide error message
            this.submit(); // Submit the form
        }
    });

    // Handle Enter key press
    cityInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            if (!cityInput.value.trim()) {
                e.preventDefault();
                errorMsg.style.display = 'block';
            } else {
                errorMsg.style.display = 'none';
            }
        }
    });

    // Update weather icon based on condition
    const weatherCondition = document.querySelector('.weather-info p:nth-child(3)').textContent;
    if (weatherCondition) {
        const condition = weatherCondition.toLowerCase();
        if (condition.includes('clear')) {
            weatherIcon.src = 'https://cdn-icons-png.flaticon.com/512/6974/6974833.png';
        } else if (condition.includes('cloud')) {
            weatherIcon.src = 'https://cdn-icons-png.flaticon.com/512/1146/1146869.png';
        } else if (condition.includes('rain')) {
            weatherIcon.src = 'https://cdn-icons-png.flaticon.com/512/3351/3351979.png';
        } else if (condition.includes('thunder')) {
            weatherIcon.src = 'https://cdn-icons-png.flaticon.com/512/3351/3351979.png';
        }
    }
});