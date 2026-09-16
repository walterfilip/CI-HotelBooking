package org.example.pensionat.customer.client;

import org.example.pensionat.customer.model.CreateCustomerRequest;
import org.example.pensionat.customer.model.CustomerResponse;
import org.example.pensionat.customer.model.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class CustomerIntegrationTest {

    private final RestTemplate restTemplate = new RestTemplate();

    //201 = Created
    @Test
    void registeringCustomerShouldReturn201(){
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Jens",
                "Jensson",
                "jens.test@fakemail.se",
                "0767661111",
                "hej"
        );

        CustomerResponse createdCustomer = null;

        try {
            ResponseEntity<CustomerResponse> response = restTemplate.postForEntity("http://localhost:8081/api/customers", request, CustomerResponse.class);

            assertEquals(201, response.getStatusCode().value());

            createdCustomer = response.getBody();

            assertNotNull(createdCustomer);
            assertTrue(createdCustomer.id() > 0);
            assertEquals("Jens", createdCustomer.firstName());
            assertEquals("Jensson", createdCustomer.lastName());
            assertEquals("jens.test@fakemail.se", createdCustomer.email());
            assertEquals("0767661111", createdCustomer.phoneNumber()
            );
        } finally {
            //Deletar bara customern från databasen efter testet, egentligen inte en riktig del av testet (går att bryta ut till ett separat test sen om man vill).
            if (createdCustomer != null) {
                restTemplate.delete("http://localhost:8081/api/customers/{id}", createdCustomer.id());
            }
        }
    }

    //400 = Bad Request
    @Test
    void registeringCustomerWithIncorrectEmailShouldReturn400() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Gunnar",
                "Gunnarsson",
                "paj-epost",
                "0767661111",
                "hej"
        );

        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.class,
                () -> restTemplate.postForEntity("http://localhost:8081/api/customers", request, CustomerResponse.class));

        assertEquals(400, exception.getStatusCode().value());
    }

    //401 = Unauthorized
    @Test
    void loginWithIncorrectCredentialsShouldReturn401() {
        LoginRequest request = new LoginRequest("finns-inte@testmail.se", "fel-lösen");

        //får den här Unauthorized exceptionen från logiken i CustomerService
        HttpClientErrorException.Unauthorized exception = assertThrows(
                HttpClientErrorException.Unauthorized.class,
                () -> restTemplate.postForEntity("http://localhost:8081/api/customers/login", request, CustomerResponse.class));

        assertEquals(401, exception.getStatusCode().value());
    }
}
