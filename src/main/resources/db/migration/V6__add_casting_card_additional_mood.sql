-- casting_cards 테이블에 AI가 추가로 감지한 감정을 저장할 컬럼 추가
-- 사용자가 직접 선택한 mood(daily_records 소유)와는 별개로,
-- AI가 하루 기록 본문에서 판단한 추가 감정을 담음 (2026-08-05)
ALTER TABLE casting_cards
    ADD COLUMN additional_mood JSON NULL AFTER comment_phrase;