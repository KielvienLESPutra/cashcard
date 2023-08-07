package example.cashcard;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

//@SpringBootTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CashcardApplicationTests {

	private static Logger log = LoggerFactory.getLogger(CashcardApplicationTests.class);
	
//	@Autowired
//    TestRestTemplate restTemplate;
//
//	@Test
//    void shouldReturnACashCardWhenDataIsSaved() {
//        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/100", String.class);
//
////        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
////        
////        DocumentContext documentContext = JsonPath.parse(response.getBody());
////        Number id = documentContext.read("$.id");
////        assertThat(id).isNotNull();
//        
////        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
//  		assertThat(response.getBody()).isBlank();
//    }
//	
//	@Test
//	void contextLoads() {
//	}
	
	@Autowired
    TestRestTemplate restTemplate;

    @Test
    void shouldReturnACashCardWhenDataIsSaved() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/99", String.class);
        log.info("data response :" + response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(99);

        Double amount = documentContext.read("$.amount");
        assertThat(amount).isEqualTo(123.45);
    }

    @Test
    void shouldNotReturnACashCardWithAnUnknownId() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/1000", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }
    
    @Test
    void shouldCreateNewCashCard() {
    	CashCard newCashCard = new CashCard(null, 250.00);
    	ResponseEntity<Void> createResponse  = restTemplate.postForEntity("/cashcards", newCashCard, void.class);
//    	assertThat(createResponse .getStatusCode()).isEqualTo(HttpStatus.OK);
    	assertThat(createResponse .getStatusCode()).isEqualTo(HttpStatus.CREATED);
    	
    	URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
    	ResponseEntity<String> getResponse = restTemplate.getForEntity(locationOfNewCashCard, String.class);
    	assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    	
    	DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
    	Number id = documentContext.read("$.id");
    	Double amount = documentContext.read("$.amount");
    	
    	assertThat(id).isNotNull();
    	assertThat(amount).isEqualTo(250.00);
    }

}
