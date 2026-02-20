-- call_logs tablosu JPA tarafından otomatik oluşturulur (ddl-auto=update).
-- Bu dosya ilave index veya başlangıç verisi için kullanılabilir.

-- Arayan numaraya göre hızlı arama için index
CREATE INDEX IF NOT EXISTS idx_caller_number ON call_logs(caller_number);
CREATE INDEX IF NOT EXISTS idx_call_time ON call_logs(call_time);
