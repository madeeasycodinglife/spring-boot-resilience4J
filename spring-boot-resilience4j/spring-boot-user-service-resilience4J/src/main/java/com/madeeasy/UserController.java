package com.madeeasy;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@RestController
public class UserController {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private UserService userService;

    @Value("${resilience4j.retry.configs.customRetryConfig.maxAttempts}")
    private Integer maxAttempts;

    @GetMapping("/users")
    @CircuitBreaker(name = "myCircuitBreaker", fallbackMethod = "fallbackMethodCalledWhenDepartmentServiceIsDown")
    @Bulkhead(name = "myBulkhead")
    @RateLimiter(name = "myRateLimiter")
    @TimeLimiter(name = "myTimeLimiter")
    public ResponseEntity<String> getUserWithDepartmentService() {
        System.out.println("getDepartmentService() call starts here");
        String fromDepartmentService = restTemplate.getForObject("http://localhost:8082/departments", String.class);
        String response = "Hi " + fromDepartmentService;
        return new ResponseEntity<String>(response, HttpStatus.OK);
    }

    @GetMapping("/retry")
    public String retryMethod() {
        try {
            userService.retryMethod();
            return "Retry method executed successfully";
        } catch (Exception e) {
            return "Exception occurred: " + e.getMessage();
        }
    }

    @Retry(name = "myRetry")
    @GetMapping("/user/retry")
    public ResponseEntity<String> callDepartmentService() {
        /**
         * to see the @Retry() method's working then stop department service http://localhost:8082 so that it will keep trying
         * calling this "http://localhost:8082/departments" url 3 times that is configured in yml file maxAttempts = 3 per 2 seconds
         * i.e. waitDuration: 2s i.e. each time you refresh the page it will trying to call 3 times to "http://localhost:8082/departments"
         */
        String url = "http://localhost:8082/departments"; // Replace with your actual URL
        ResponseEntity<String> response;

        try {
            response = restTemplate.getForEntity(url, String.class);
            // If the response is successful, return it
            if (response.getStatusCode().is2xxSuccessful()) {
                return response;
            }
        } catch (Exception e) {
            // Retry for all exceptions
            System.out.println("Exception occurred: " + e.getMessage());
        }
        throw new RuntimeException("Failed to retrieve department service");
    }

    @GetMapping("/department")
    @TimeLimiter(name = "myTimeLimiter", fallbackMethod = "fallbackMethodCalledWhenDepartmentServiceIsDown")
    public CompletionStage<String> getDepartmentService() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                /**
                 * this 3s is made to verify the @TimeLimiter so that it can not complete the operation with in 1ms
                 * and fallbackMethod will be invoked
                 */
                Thread.sleep(3000); // Simulating a long-running operation
                String url = "http://localhost:8082/departments"; // Replace with your actual URL
                ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    return responseEntity.getBody();
                } else {
                    throw new RuntimeException("Failed to retrieve department service");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted");
            }
        });
    }


    public ResponseEntity<String> fallbackMethodCalledWhenDepartmentServiceIsDown(Exception e) {
        System.out.println("fall back method is called");
        return new ResponseEntity<String>("Fallback response method name getUserWithDepartmentService()", HttpStatus.OK);
    }

    public CompletionStage<String> fallbackMethodCalledWhenDepartmentServiceIsDown(Throwable ex) {
        return CompletableFuture.completedFuture("Fallback response method name getDepartmentService()");
    }
}

