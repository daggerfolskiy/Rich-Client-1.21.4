package fun.rich.utils.features.autobuy;

public class BuyRequest {
    public String itemName;
    public int price;

    public BuyRequest(String itemName, int price) {
        this.itemName = itemName;
        this.price = price;
    }
}