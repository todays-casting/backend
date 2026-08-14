package com.todayscasting.domain.casting.support;

import java.util.List;
import java.util.Map;

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

    // "오피스·학원물", "누아르·비극"처럼 중간에 특수문자(·)가 낀 장르는 AI 응답이나
    // 사용자 표기에서 "오피스", "누아르"처럼 축약되어 나올 수 있어 별칭으로 매핑한다.
    // (CodeRabbit 리뷰 반영: "오피스 성장물"에서 첫 토큰 "오피스"가 정식 후보와
    // 불일치해 보조 장르 "성장"으로 잘못 폴백되던 문제 수정)
    private static final Map<String, String> MAIN_GENRE_ALIASES = Map.of(
            "오피스", "오피스·학원물",
            "학원물", "오피스·학원물",
            "누아르", "누아르·비극",
            "비극", "누아르·비극"
    );

    private MainGenreExtractor() {
    }

    // genre 값에서 메인 장르를 추출한다.
    // 1) 공백 기준 첫 토큰이 20개 후보 중 하나로 시작하면 그 후보를 반환
    // 2) 첫 토큰이 별칭(예: "오피스")과 일치하면 정식 장르명으로 변환해 반환
    // 3) 위 두 경우 모두 실패하면 null 반환 — 보조 장르를 메인으로 오인하지 않도록
    //    genre 전체 문자열에 대한 contains() 폴백은 의도적으로 사용하지 않는다.
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

        for (Map.Entry<String, String> alias : MAIN_GENRE_ALIASES.entrySet()) {
            if (firstToken.equals(alias.getKey()) || firstToken.startsWith(alias.getKey())) {
                return alias.getValue();
            }
        }

        return null;
    }
}