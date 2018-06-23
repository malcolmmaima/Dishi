package malcolmmaima.dishi.Model;

public class MyCartDetails {

    public String name;
    public String price;
    public String description;
    public String provider;
    public String providerNumber;
    public String customerNumber;
    public String imageURL;
    public String status;
    public String key; //When deleting menu items from firebase, this key value will help delete individual items from the 'mymenu' node

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

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }

    public void setProviderNumber(String providerNumber) {
        this.providerNumber = providerNumber;
    }

    public String getProviderNumber() {
        return providerNumber;
    }
}
