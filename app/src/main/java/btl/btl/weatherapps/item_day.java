package btl.btl.weatherapps;

public class item_day {
    private String day, min_tem, max_tem;
    int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getMin_tem() {
        return min_tem;
    }

    public void setMin_tem(String min_tem) {
        this.min_tem = min_tem;
    }

    public String getMax_tem() {
        return max_tem;
    }

    public void setMax_tem(String max_tem) {
        this.max_tem = max_tem;
    }


    public item_day(String day, String min_tem, String max_tem, int type) {
        this.day = day;
        this.min_tem = min_tem;
        this.max_tem = max_tem;
        this.type = type;
    }
}
