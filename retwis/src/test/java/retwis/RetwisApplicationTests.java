package retwis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import retwis.util.MD5Utils;

@SpringBootTest
class RetwisApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void test() {
//        System.out.println(MD5Utils.convertMD5("tom"));
    }

    public static void main(String[] args) {
        System.out.println("123asd_....456".matches("[A-Za-z0-9_.]{6,16}"));
    }

}
