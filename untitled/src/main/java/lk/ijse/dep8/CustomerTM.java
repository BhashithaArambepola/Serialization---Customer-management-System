package lk.ijse.dep8;

import java.io.Serializable;
import java.util.Arrays;

public class CustomerTM implements Serializable {


    private String id;
    private String name;
    private String address;
    private byte[] image;

    public CustomerTM() {
    }

    public CustomerTM(String id, String name, String address, byte[] image) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }


    @Override
    public String toString() {
        return "CustomerTM{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", bytes='" + Arrays.toString(image) + '\'' +
                '}';
    }

}
