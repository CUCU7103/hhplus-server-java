package kr.hhplus.be.server.global.support.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public record SearchRankListenerContext(String concertName, String concertDate) {

	public SearchRankListenerContext(String concertName, String concertDate) {
		this.concertName = concertName;
		this.concertDate = concertDate;
	}

	public String objectToString() throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(new SearchRankListenerContext(concertName, concertDate));
	}
}
