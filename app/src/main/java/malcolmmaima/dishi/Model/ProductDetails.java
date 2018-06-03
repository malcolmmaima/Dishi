package malcolmmaima.dishi.Model;

public class ProductDetails {

    public String name;
    public String price;
    public String description;
    public String product_image;

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

    public String getProduct_image(){
        return product_image;
    }

    public void setProduct_image(String product_image) {
        this.product_image = product_image;
    }
}