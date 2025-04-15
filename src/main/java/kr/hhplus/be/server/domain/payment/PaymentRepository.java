package kr.hhplus.be.server.domain.payment;

import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository {
	Payment save(Payment payment);
}
