package example.cashcard;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

//@SpringBootTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
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
//		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/99", String.class);
		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1", "abc123").getForEntity("/cashcards/99",
				String.class);
//		log.info("data response saved :" + response);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		Number id = documentContext.read("$.id");
		assertThat(id).isEqualTo(99);

		Double amount = documentContext.read("$.amount");
		assertThat(amount).isEqualTo(123.45);
	}

	@Test
	void shouldNotReturnACashCardWithAnUnknownId() {
		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1", "abc123").getForEntity("/cashcards/1000",
				String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isBlank();
	}

	@Test
//    @Disabled
	@DirtiesContext
	void shouldCreateNewCashCard() {
//		CashCard newCashCard = new CashCard(null, 250.00, "sarah1");
		CashCard newCashCard = new CashCard(null, 250.00, null);
		ResponseEntity<Void> createResponse = restTemplate.withBasicAuth("sarah1", "abc123").postForEntity("/cashcards",
				newCashCard, void.class);
//    	assertThat(createResponse .getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate.withBasicAuth("sarah1", "abc123")
				.getForEntity(locationOfNewCashCard, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");

		assertThat(id).isNotNull();
		assertThat(amount).isEqualTo(250.00);
	}

	@Test
	@DirtiesContext
	void shouldUpdateAnExistingCashCard() {
		CashCard cashCardUpdate = new CashCard(null, 19.99, null);
		HttpEntity<CashCard> request = new HttpEntity<CashCard>(cashCardUpdate);
		ResponseEntity<Void> response = restTemplate.withBasicAuth("sarah1", "abc123").exchange("/cashcards/99",
				HttpMethod.PUT, request, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> getResponse = restTemplate.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards/99", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		assertThat(id).isEqualTo(99);
		assertThat(amount).isEqualTo(19.99);
	}

	@Test
	void shouldNotpdateACashCardThatDoesotExist() {
		CashCard unknownCard = new CashCard(null, 19.99, null);
		HttpEntity<CashCard> request = new HttpEntity<CashCard>(unknownCard);
		ResponseEntity<Void> response = restTemplate.withBasicAuth("sarah1", "abc123").exchange("/cashcards/99999",
				HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldRejectUsersWhoAreNotCardOwners() {
		ResponseEntity<String> response = restTemplate.withBasicAuth("hank-owns-no-cards", "qrs456")
				.getForEntity("/cashcards/99", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void shouldNotAllowAccessToCashCArdsTheyDoNotOwn() {
		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1", "abc123").getForEntity("/cashcards/102",
				String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotReturnACashCardWhenUsingBadCredentials() {
		ResponseEntity<String> response = restTemplate.withBasicAuth("BAD-USER", "abc123").getForEntity("/cashcards/99",
				String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		response = restTemplate.withBasicAuth("sarah1", "BAD-PASSWORD").getForEntity("/cashcards/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {
		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1", "abc123").getForEntity("/cashcards",
				String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

//		log.info("return should page sorted no param :" + response);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(3);

		JSONArray amount = documentContext.read("$..amount");
		assertThat(amount).containsExactly(1.00, 123.45, 150.00);
	}

	@Test
	void shouldReturnASortedPageOfCashCards() {
		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

//		log.info("return should page sorted :" + response);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray read = documentContext.read("$[*]");
		assertThat(read.size()).isEqualTo(1);

		double amount = documentContext.read("$[0].amount");
		assertThat(amount).isEqualTo(150.00);
	}

	@Test
	void shouldReturnAPageOfCashCards() {
		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards?page=0&size=1", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

//		log.info("return should page :" + response);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(1);
	}

	@Test
	void shouldReturnAllCashCardsWhenListIsRequested() {
		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1", "abc123").getForEntity("/cashcards",
				String.class);
//		log.info("response find all : " + response);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int cashCardCount = documentContext.read("$.length()");
		assertThat(cashCardCount).isEqualTo(3);

		JSONArray ids = documentContext.read("$..id");
		assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);

		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.00, 150.00);
	}

}
