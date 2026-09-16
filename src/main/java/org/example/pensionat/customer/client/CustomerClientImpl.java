package org.example.pensionat.customer.client;

import org.springframework.beans.factory.annotation.Value;
import org.example.pensionat.customer.model.UpdateCustomerRequest;
import org.example.pensionat.customer.model.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CustomerClientImpl implements CustomerClient {

    private final String BASE_URL;
    private final RestTemplate restTemplate;

    public CustomerClientImpl(@Value("${services.customer.base-url}")
                              String customerServiceUrl) {
        this.restTemplate = new RestTemplate();
        this.BASE_URL = customerServiceUrl + "/api/customers";
    }

    @Override
    public CustomerResponse login(LoginRequest request) {
        return restTemplate.postForObject(BASE_URL + "/login", request, CustomerResponse.class);
    }

    @Override
    public CustomerResponse getCustomer(long customerId) {
        return restTemplate.getForObject(BASE_URL + "/{id}", CustomerResponse.class, customerId);
    }

    @Override
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        return restTemplate.postForObject(BASE_URL, request, CustomerResponse.class);
    }

    @Override
    public CustomerResponse updateCustomer(long customerId, UpdateCustomerRequest request) {
        restTemplate.put(BASE_URL + "/{id}", request, customerId);

        return getCustomer(customerId);
    }

    @Override
    public void deleteCustomer(long customerId) {restTemplate.delete(BASE_URL + "/{id}", customerId);
    }
}
