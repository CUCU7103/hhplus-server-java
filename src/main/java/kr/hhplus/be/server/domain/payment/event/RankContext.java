package kr.hhplus.be.server.domain.payment.event;

import java.time.LocalDate;

public record RankContext(String concertName, String concertDate) {

	public RankContext(String concertName, String concertDate) {
		this.concertName = concertName;
		this.concertDate = concertDate;
	}

	public static RankContext of(String concertName, LocalDate concertDate) {
		String changedDate = String.valueOf(concertDate);
		return new RankContext(concertName, changedDate);
	}
}
