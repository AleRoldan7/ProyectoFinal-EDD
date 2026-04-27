package clases;
import Enum.Estado;
public class Productos implements Comparable<Productos> {

    private String name;
    private String barCode;
    private String category;
    private String expiryDate;
    private String brand;
    private Double price;

    private int stock;

    private Estado status;

    private int branchId;

    public Productos() {
    }

    public Productos(String name) {
        this.name = name;
    }

    public Productos(String name, String barCode, String category, String expiryDate, String brand, Double price,
                     int stock, Estado status, int branchId) {
        this.name = name;
        this.barCode = barCode;
        this.category = category;
        this.expiryDate = expiryDate;
        this.brand = brand;
        this.price = price;
        this.stock = stock;
        this.status = Estado.DISPONIBLE;
        this.branchId = branchId;
    }


    public Productos(int branchId, String name, String barCode, String category, String expiryDate, String brand,
                     Double price, int stock) {
        this.branchId = branchId;
        this.name = name;
        this.barCode = barCode;
        this.category = category;
        this.expiryDate = expiryDate;
        this.brand = brand;
        this.price = price;
        this.stock = stock;
    }

    @Override
    public int compareTo(Productos productos) {
        return this.name.compareToIgnoreCase(productos.name);
    }

    @Override
    public String toString() {
        return "Productos{" +
                "name='" + name + '\'' +
                ", barCode='" + barCode + '\'' +
                ", category='" + category + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", brand='" + brand + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", status=" + status +
                ", branchId=" + branchId +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Estado getStatus() {
        return status;
    }

    public void setStatus(Estado status) {
        this.status = status;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }
}


