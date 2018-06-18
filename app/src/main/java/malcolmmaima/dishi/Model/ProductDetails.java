package malcolmmaima.dishi.Model;

public class ProductDetails {

    public String name;
    public String price;
    public String description;
    public String imageURL;
    public  String key;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageURL(String imageURL){
        this.imageURL = imageURL;
    }

    public String getImageURL() {
        return imageURL;
    }


}