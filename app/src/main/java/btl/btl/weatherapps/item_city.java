package btl.btl.weatherapps;

public class item_city {
    private String name_city, image;

    public String getName_city() {
        return name_city;
    }

    public void setName_city(String name_city) {
        this.name_city = name_city;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public item_city(String name_city, String image) {
        this.name_city = name_city;
        this.image = image;
    }

    public item_city() {
    }
}
