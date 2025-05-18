package kr.hhplus.be.server.global.support.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class SearchRankEvent extends ApplicationEvent {
	private long scheduleId;

	public SearchRankEvent(Object source, long scheduleId) {
		super(source);
		this.scheduleId = scheduleId;
	}

}
