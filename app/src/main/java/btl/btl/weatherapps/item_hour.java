package btl.btl.weatherapps;

public class item_hour {
    private String time, temperature, type_weather;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String gettype_weather() {
        return type_weather;
    }

    public void setImage(String type_weather) {
        this.type_weather = type_weather;
    }

    public item_hour(String time, String temperature, String type_weather) {
        this.time = time;
        this.temperature = temperature;
        this.type_weather = type_weather;
    }
}
