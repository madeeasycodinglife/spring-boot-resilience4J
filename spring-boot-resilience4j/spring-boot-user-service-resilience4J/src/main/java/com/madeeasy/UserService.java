package com.madeeasy;

import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private int counter = 0;

    @Retry(name = "myRetry")
    public void retryMethod() {
        counter++;
        System.out.println("Retry method called. Counter: " + counter);
        if (counter < 3) {
            throw new RuntimeException("Retry exception");
        }
    }
}

