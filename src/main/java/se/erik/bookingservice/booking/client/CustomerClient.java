package se.erik.bookingservice.booking.client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import se.erik.bookingservice.dto.CustomerDto;
import se.erik.bookingservice.error.NotFoundException;
import se.erik.bookingservice.error.ServiceUnavailableException;

@Service
public class CustomerClient {

    private static final Logger log = LoggerFactory.getLogger(CustomerClient.class);

    private final RestTemplate restTemplate;
    private final String customerServiceUrl;

    public CustomerClient(RestTemplate restTemplate,
                          @Value("${customer.service.base-url}") String customerServiceUrl) {
        this.restTemplate = restTemplate;
        this.customerServiceUrl = customerServiceUrl;
    }

    public CustomerDto getCustomerById(Long customerId, String authorizationHeader) {

        log.info("Fetching customer from CustomerService");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<CustomerDto> response = restTemplate.exchange(
                    customerServiceUrl + "/customers/" + customerId,
                    HttpMethod.GET,
                    entity,
                    CustomerDto.class
            );

            log.info("Customer fetched successfully from CustomerService");
            return response.getBody();

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Customer was not found in CustomerService");
            throw new NotFoundException("Customer with id " +customerId + " not found");
        } catch (RestClientException e) {
            log.error("CustomerService unavailable", e);
            throw new ServiceUnavailableException("Customer service unavailable");
        }
    }
}

