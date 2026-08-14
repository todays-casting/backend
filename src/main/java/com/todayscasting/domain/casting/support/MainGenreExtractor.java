package com.todayscasting.domain.casting.support;

import java.util.List;

// genre 문자열(예: "오피스 성장물")에서 메인 장르를 추출한다.
// AI가 "메인 장르 + 보조 장르" 순서로 조합하도록 프롬프트가 설계되어 있어,
// 공백으로 나눈 첫 토큰을 메인 장르 후보로 보고 20개 장르 후보 목록과 대조한다.
public class MainGenreExtractor {

    // 20개 메인 장르 후보 풀 (AI 분석 프롬프트의 장르 후보와 동일)
    public static final List<String> MAIN_GENRE_LIST = List.of(
            "로맨스", "멜로", "코미디", "드라마", "액션", "스릴러", "미스터리", "공포",
            "판타지", "SF", "모험", "성장", "청춘", "일상", "힐링", "오피스·학원물",
            "스포츠", "재난", "서바이벌", "누아르·비극"
    );

    private MainGenreExtractor() {
    }

    // genre 값에서 메인 장르를 추출한다.
    // 1) 공백 기준 첫 토큰이 20개 후보 중 하나로 시작하면 그 후보를 반환
    // 2) 못 찾으면 genre 전체 문자열에 포함된 후보 중 첫 번째로 매칭되는 것을 폴백으로 사용
    // 3) 그마저도 없으면 null 반환 (예: genre="-"인 미작성 케이스)
    public static String extract(String genre) {
        if (genre == null || genre.isBlank()) {
            return null;
        }
        String firstToken = genre.trim().split(" ")[0];
        for (String candidate : MAIN_GENRE_LIST) {
            if (firstToken.equals(candidate) || firstToken.startsWith(candidate)) {
                return candidate;
            }
        }
        for (String candidate : MAIN_GENRE_LIST) {
            if (genre.contains(candidate)) {
                return candidate;
            }
        }
        return null;
    }
}