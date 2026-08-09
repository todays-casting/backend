-- casting_cards 테이블의 title 컬럼을 NOT NULL에서 nullable로 변경
-- 사유: 확정된 UI(오늘의 캐스팅 결과, 히스토리 조회)에 title이 노출되지 않아
--       더 이상 AI 분석 시 title 값을 생성/저장하지 않기로 결정 (2026-08-04)
ALTER TABLE casting_cards MODIFY COLUMN title VARCHAR(100) NULL;