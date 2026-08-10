-- casting_cards 테이블에 "오늘의 캐스팅 결과" 화면 전용 필드 추가
-- scene_phrase: 오늘의 장면 (문구 스타일, 예: '작은 위로의 순간')
-- comment_phrase: 오늘의 코멘트 (문구 스타일, 예: '오늘의 나는 누군가에게 조용한 힘')
-- 기존 highlight/one_line_comment는 "오늘의 결과" 화면용으로 그대로 유지 (2026-08-05)
ALTER TABLE casting_cards
    ADD COLUMN scene_phrase VARCHAR(100) NULL AFTER one_line_comment,
    ADD COLUMN comment_phrase VARCHAR(100) NULL AFTER scene_phrase;