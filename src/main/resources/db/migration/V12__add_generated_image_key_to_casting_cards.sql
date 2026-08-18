ALTER TABLE casting_cards
    ADD COLUMN generated_image_key VARCHAR(255) NULL COMMENT 'AI가 실시간 생성한 이미지의 S3 key (이슈 #93)';