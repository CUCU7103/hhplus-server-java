package kr.hhplus.be.server.global.support.event;

public record SearchRankListenerContext(String concertName, String concertDate) {

	public SearchRankListenerContext(String concertName, String concertDate) {
		this.concertName = concertName;
		this.concertDate = concertDate;
	}

}
