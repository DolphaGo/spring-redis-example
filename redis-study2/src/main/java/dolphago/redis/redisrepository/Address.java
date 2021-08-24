package dolphago.redis.redisrepository;

import lombok.Data;

@Data
public class Address {
    private String country;
    private String city;

    public Address(String country, String city) {
        this.country = country;
        this.city = city;
    }
}
