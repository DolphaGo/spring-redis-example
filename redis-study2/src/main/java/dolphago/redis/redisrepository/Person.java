package dolphago.redis.redisrepository;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Data;

@RedisHash("people")
@Data
public class Person {

    @Id
    private String id;
    private String firstname;
    private String lastname;
    private Address address;

    public Person(String id, String firstname, String lastname, Address address) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.address = address;
    }
}
