-- ============================================================
-- 学生自主练习功能 建表迁移
-- 允许 exam_record / score 的 exam_id 为空（随机组卷练习不关联正式考试）
-- 执行：schema.sql 之后；幂等，可重复执行
-- ============================================================

ALTER TABLE exam_record DROP CONSTRAINT IF EXISTS exam_record_exam_id_fkey;
ALTER TABLE exam_record ALTER COLUMN exam_id DROP NOT NULL;

ALTER TABLE score DROP CONSTRAINT IF EXISTS score_exam_id_fkey;
ALTER TABLE score ALTER COLUMN exam_id DROP NOT NULL;
