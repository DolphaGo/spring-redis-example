package dolphago.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.ContextConfiguration;

import dolphago.redis.config.AppConfig;

@ContextConfiguration(classes = AppConfig.class) // AppConfig에서 정의한 Bean을 사용함
@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void redisConnectionTest() {
        final String key = "a";
        final String data = "1";

        final ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, data);

        final String result = valueOperations.get(key);
        assertThat(data).isEqualTo(result);
    }

    @Test
    void redisGetKeyABC() {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();

        // redis-cli로 직접 (key, value) = (abc, 123) 을 넣었음

        String result = valueOperations.get("abc");
        System.out.println(result); // null 반환 ?!
        // => redisTemplate로 통신할 때 byte로 통신하기 때문에 valueOperations.get("abc")에 값이 없는 것은 당연함
    }

    @Test
    void redisObjectTest() {
        User user = new User();
        user.setId("user1");
        user.setPw("pw");

        ValueOperations<String, User> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(user.getId(), user);

        User result = valueOperations.get(user.getId());
        System.out.println(user);
        /**
         * Cannot serialize; nested exception is org.springframework.core.serializer.support.SerializationFailedException: Failed to serialize object using DefaultSerializer; nested exception is java.lang.IllegalArgumentException: DefaultSerializer requires a Serializable payload but received an object of type [dolphago.redis.User]
         * org.springframework.data.redis.serializer.SerializationException
         *
         * -> User Class에 Serialize 추가
         */
    }

    @DisplayName("레디스 집합 연산")
    @Test
    void redisSetOperationTest() {
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        String setKey = "setKey";
        String str1 = "str1";
        String str2 = "str2";
        String str3 = "str3";

        List<String> list = new LinkedList<>();

        list.add(str1);
        list.add(str2);
        list.add(str3);

        setOperations.add(setKey, list.get(0));
        setOperations.add(setKey, list.get(1));
        setOperations.add(setKey, list.get(2));
        // setOperations.add(setKey, list.get(0), list.get(1), list.get(2)); 도 가능하다.

        assertThat(checkAllInside(setKey, setOperations, list)).isTrue();
    }

    private boolean checkAllInside(String setKey, SetOperations<String, String> setOperations, List<String> list) {
        boolean flag = true;
        for (String o : list) {
            System.out.println(o);
            if (!setOperations.isMember(setKey, o)) {
                flag = false;
            }
        }
        return flag;
    }

    @Test
    void redisSetOperationSortedsetTest() {
        String setKey = "setKey";
        String str1 = "str1";
        String str2 = "str2";
        String str3 = "str3";

        redisTemplate.delete(setKey);
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();

        List<String> list = new LinkedList<>();

        list.add(str3);
        list.add(str2);
        list.add(str1);

        setOperations.add(setKey, list.get(0), list.get(1), list.get(2));

        for (String s : setOperations.members(setKey)) {
            System.out.println(s);
        }
    }

    @DisplayName("Sorted Set을 사용하기 위해서는 ZSetOperation을 이용해야 한다.")
    @Test
    void redisSetOperationSortedSetTest() {
        String key = "users";

        if (redisTemplate.hasKey(key)) {
            redisTemplate.delete(key);
        }

        ZSetOperations<String, User> zSetOperations = redisTemplate.opsForZSet();
        List<User> list = new LinkedList<>();
        for (int i = 0; i < 1000; i++) {
            User user = new User("user-" + i, String.valueOf(i));
            list.add(user);
            zSetOperations.add(key, user, i); // score가 곧 순서를 의미한다.
        }
        Set<User> set = zSetOperations.range(key, 0, 1000);
        System.out.println("set = " + set);

        assertThat(list.size()).isEqualTo(set.size());
    }
}
