package yedi.examples.greeter;

public class WeatherForecast implements Forecast{

    private String windSpeed;
    private String temperature;
    private String description;

    public WeatherForecast(String temperature, String windSpeed) {
        this.windSpeed = windSpeed;
        this.temperature = temperature;
        this.description = "Sunny all day long.";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getDescription() {
        return description;
    }
}
