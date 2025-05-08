package kr.hhplus.be.server.global.support.page;

import java.util.Collections;
import java.util.List;

public class PaginationUtils {
	/**
	 * 리스트를 페이지네이션하여 요청된 페이지의 데이터만 반환합니다.
	 *
	 * @param fullList 전체 데이터 리스트
	 * @param page 요청 페이지 (1부터 시작)
	 * @param size 페이지 크기
	 * @return 해당 페이지의 데이터 리스트
	 * @param <T> 리스트 요소 타입
	 */
	public static <T> List<T> getPage(List<T> fullList, int page, int size) {
		// 페이지 번호는 사용자가 1부터 인식하지만, 리스트 인덱스는 0부터 시작하므로 변환합니다.
		// 예: 1페이지 요청 시 (1-1)*size = 0번째 인덱스부터 시작
		int start = (page - 1) * size;
		// 유효하지 않은 시작 인덱스인 경우 빈 리스트 반환
		if (start >= fullList.size()) {
			return Collections.emptyList();
		}
		// 끝 인덱스 계산 (리스트 크기를 초과하지 않도록)
		int end = Math.min(start + size, fullList.size());

		// 해당 범위의 하위 리스트 반환
		return fullList.subList(start, end);
	}

	/**
	 * 페이지 결과 객체를 생성합니다.
	 *
	 * @param fullList 전체 데이터 리스트
	 * @param page 요청 페이지 (1부터 시작)
	 * @param size 페이지 크기
	 * @return 페이지 결과 객체
	 * @param <T> 리스트 요소 타입
	 */
	public static <T> PageResult<T> createPageResult(List<T> fullList, int page, int size) {
		return new PageResult<>(fullList, page, size);
	}
}

