-- casting_cards 테이블에 캐릭터 소개 문구 컬럼 추가
-- 달력 화면 등에서 "이 하루를 살아낸 사람이 어떤 사람인지" 소개하는 문장을 담음
-- 아직 실제 화면 연결 여부는 미확정 (담당자 확인 필요), 미리 준비해두는 용도 (2026-08-06)
ALTER TABLE casting_cards
    ADD COLUMN character_phrase VARCHAR(150) NULL AFTER additional_mood;