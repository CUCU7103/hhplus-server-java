package kr.hhplus.be.server.global.support.page;

import java.util.List;

import lombok.Getter;

@Getter
public class PageResult<T> {
	private final List<T> content;
	private final int page;
	private final int size;
	private final int totalPages;
	private final long totalElements;

	/**
	 * @param fullList 전체 데이터 리스트
	 * @param page 요청 페이지 (1부터 시작)
	 * @param size 페이지 크기
	 */
	public PageResult(List<T> fullList, int page, int size) {
		// 전체 요소 수
		this.totalElements = fullList.size();
		// 전체 페이지 수 계산 (올림 연산)
		this.totalPages = (int)Math.ceil((double)totalElements / size);
		this.page = page;
		this.size = size;

		// 현재 페이지 데이터 계산
		this.content = PaginationUtils.getPage(fullList, page, size);
	}

	/**
	 * 다음 페이지가 있는지 여부를 반환합니다.
	 *
	 * @return 다음 페이지 존재 여부
	 */
	public boolean hasNext() {
		return page < totalPages;
	}

	/**
	 * 이전 페이지가 있는지 여부를 반환합니다.
	 *
	 * @return 이전 페이지 존재 여부
	 */
	public boolean hasPrevious() {
		return page > 1;
	}

	/**
	 * 현재 페이지가 첫 페이지인지 여부를 반환합니다.
	 *
	 * @return 첫 페이지 여부
	 */
	public boolean isFirst() {
		return page == 1;
	}

	/**
	 * 현재 페이지가 마지막 페이지인지 여부를 반환합니다.
	 *
	 * @return 마지막 페이지 여부
	 */
	public boolean isLast() {
		return page >= totalPages;
	}

}
