ALTER TABLE ai_analysis_logs
    ADD CONSTRAINT uk_ai_analysis_logs_daily_record UNIQUE (daily_record_id);