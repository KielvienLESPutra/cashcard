package example.cashcard;

import java.net.URI;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {

	private CashCardRepository cashCardRepository;

	public CashCardController(CashCardRepository cashCardRepository) {
		this.cashCardRepository = cashCardRepository;
	}

	@GetMapping("/{requestedId}")
	public ResponseEntity<CashCard> findById(@PathVariable Long requestedId) {
//		if (requestedId.equals(99L)) {
//			CashCard cashCard = new CashCard(99L, 123.45);
//			return ResponseEntity.ok(cashCard);
//		} else {
//			return ResponseEntity.notFound().build();
//		}
		Optional<CashCard> cashCardOptional = cashCardRepository.findById(requestedId);
		if (cashCardOptional.isPresent()) {
			return ResponseEntity.ok(cashCardOptional.get());
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping
	public ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb) {
		CashCard saveCashCard = cashCardRepository.save(newCashCardRequest);
		URI locationOfNewCashCard = ucb.path("cashcards/{id}").buildAndExpand(saveCashCard.id()).toUri();

//		return ResponseEntity.created(URI.create("/what/should/go/here?")).build();
		return ResponseEntity.created(locationOfNewCashCard).build();
	}
}
