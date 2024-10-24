package com.billion.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class HelloWorldTest {
    @Test
    public void test() {
        System.out.println("hello world");
    }
}
