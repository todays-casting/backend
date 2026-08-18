package com.todayscasting.domain.casting.support;

import com.todayscasting.domain.user.entity.User;

import java.util.LinkedHashMap;
import java.util.Map;

// genre(메인+보조 장르 조합) 문자열과 성별을 받아 캐스팅 카드 배경 이미지 S3 key를 반환한다. (이슈 #89)
//
// 매칭 우선순위 (팀 논의 결과, 2026-08-14):
// 1) genre 전체 문자열과 정확히 일치하는 "조합 전용 이미지"가 있으면 그것을 사용
//    (예: "로맨스 코미디" 전용 이미지가 있으면 그대로 사용)
// 2) 정확히 일치하는 조합이 없으면, genre에서 메인 장르를 추출해
//    같은 메인 장르를 가진 다른 조합 이미지 중 하나로 대체
//    (예: "오피스 로맨스"는 전용 이미지가 없어도, 메인 장르가 같은 "로맨스 코미디" 이미지를 대신 사용)
// 3) 그 메인 장르로도 못 찾으면 최종 기본 이미지로 폴백
//
// 실제 이미지가 아직 다 준비되지 않아, 지금은 확정된 key 규칙만 관리한다.
// 이미지가 준비되는 대로 COMBO_IMAGES에 값만 추가/교체하면 된다.
public class CastingImageResolver {

    private static final String DIRECTORY = "casting-images";
    private static final String DEFAULT_FEMALE_KEY = DIRECTORY + "/default-female.png";
    private static final String DEFAULT_MALE_KEY = DIRECTORY + "/default-male.png";

    // 조합 이름(genre 전체 문자열과 정확히 일치해야 함) -> 이미지 정보
    // 자주 나오는 조합을 추가로 만들 때마다 이 목록에 register() 한 줄만 추가하면 된다.
    private static final Map<String, ComboImage> COMBO_IMAGES = new LinkedHashMap<>();

    static {
        register("로맨스 코미디", "로맨스", "romantic-comedy-female.png", "romantic-comedy-male.png");
        register("성장 드라마", "성장", "growth-drama-female.png", "growth-drama-male.png");
        register("미스터리 스릴러", "미스터리", "mystery-thriller-female.png", "mystery-thriller-male.png");
        register("힐링 일상", "힐링", "healing-female.png", "healing-male.png");
        register("판타지 모험", "판타지", "fantasy-female.png", "fantasy-male.png");
        register("모험 여행", "모험", "journey-female.png", "journey-male.png");
        // TODO: 자주 나오는 조합을 추가로 만들 때마다 여기에 register() 추가 (이슈 #89)
    }

    private static void register(String comboGenre, String mainGenre, String femaleKey, String maleKey) {
        COMBO_IMAGES.put(comboGenre, new ComboImage(mainGenre, toKey(femaleKey), toKey(maleKey)));
    }

    private CastingImageResolver() {
    }

    public static String resolveImageKey(String genre, User.Gender gender) {
        boolean isMale = gender == User.Gender.MALE;

        // 1) genre 전체 문자열과 정확히 일치하는 조합 전용 이미지가 있으면 그대로 사용
        ComboImage exactMatch = COMBO_IMAGES.get(genre);
        if (exactMatch != null) {
            return isMale ? exactMatch.maleKey() : exactMatch.femaleKey();
        }

        // 2) 정확히 일치하는 조합이 없으면, 메인 장르가 같은 다른 조합 이미지로 대체
        String mainGenre = MainGenreExtractor.extract(genre);
        if (mainGenre != null) {
            for (ComboImage combo : COMBO_IMAGES.values()) {
                if (combo.mainGenre().equals(mainGenre)) {
                    return isMale ? combo.maleKey() : combo.femaleKey();
                }
            }
        }

        // 3) 그마저도 없으면 최종 기본 이미지
        return isMale ? DEFAULT_MALE_KEY : DEFAULT_FEMALE_KEY;
    }

    private static String toKey(String filename) {
        return DIRECTORY + "/" + filename;
    }

    private record ComboImage(String mainGenre, String femaleKey, String maleKey) {
    }
}
