package dolphago.redis.redisrepository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PersonRepositoryTest {

    @Autowired
    private PersonRepository repository;

    @Test
    void basicCrudOperations() {
        final Address address = new Address("korea", "seoul");
        final Person person = new Person(null, "DolphaGo", "adam", address); // id 의 현재값이 Null 이면 RedisHash 가 Key 와 함께 keyspace:id 값으로 지정

        final Person savedPerson = repository.save(person);

        final Optional<Person> findPerson = repository.findById(savedPerson.getId());

        assertThat(findPerson.isPresent()).isTrue();
        assertThat(findPerson.get().getFirstname()).isEqualTo(person.getFirstname());

    }
}
