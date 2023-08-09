package example.cashcard;

import java.net.URI;
import java.security.Principal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
	public ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
//		if (requestedId.equals(99L)) {
//			CashCard cashCard = new CashCard(99L, 123.45);
//			return ResponseEntity.ok(cashCard);
//		} else {
//			return ResponseEntity.notFound().build();
//		}

//		Optional<CashCard> cashCardOptional = cashCardRepository.findById(requestedId);
		Optional<CashCard> cashCardOptional = Optional
				.ofNullable(cashCardRepository.findByIdAndOwner(requestedId, principal.getName()));
		if (cashCardOptional.isPresent()) {
			return ResponseEntity.ok(cashCardOptional.get());
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping
	public ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb,
			Principal principal) {
//		CashCard saveCashCard = cashCardRepository.save(newCashCardRequest);
		CashCard cashCardWithOwner = new CashCard(null, newCashCardRequest.amount(), principal.getName());
		CashCard saveCashCard = cashCardRepository.save(cashCardWithOwner);
		URI locationOfNewCashCard = ucb.path("cashcards/{id}").buildAndExpand(saveCashCard.id()).toUri();

//		return ResponseEntity.created(URI.create("/what/should/go/here?")).build();
		return ResponseEntity.created(locationOfNewCashCard).build();
	}

//	@GetMapping
//	public ResponseEntity<Iterable<CashCard>> findAll() {
//		return ResponseEntity.ok(cashCardRepository.findAll());
//	}

	@GetMapping
	public ResponseEntity<Iterable<CashCard>> findAll(Pageable pageable, Principal principal) {// return
		// ResponseEntity.ok(cashCardRepository.findAll());
		Page<CashCard> page = cashCardRepository.findByOwner(principal.getName(),
				PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
						pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))));

		return ResponseEntity.ok(page.getContent());

	}
}
