-- ============================================================
-- 中医考试系统 基础种子数据（角色/权限/院系/班级/演示账号）
-- 运行顺序：schema.sql -> seed_basic.sql -> seed_questions.sql
-- ============================================================

-- 角色
INSERT INTO sys_role(id, code, name, description) VALUES
(1, 'STUDENT',  '学生', '参加考试、练习、查看成绩与错题本'),
(2, 'TEACHER',  '教师', '管理题库、组卷、发布考试、阅卷、查看班级成绩'),
(3, 'ADMIN',    '管理员', '用户/角色/班级管理、系统配置、全局统计')
ON CONFLICT (code) DO NOTHING;

-- 权限
INSERT INTO sys_permission(id, code, name, type) VALUES
(1, 'question:manage', '题库管理', 'API'),
(2, 'paper:manage',    '组卷管理', 'API'),
(3, 'exam:manage',     '考试管理', 'API'),
(4, 'exam:join',       '参加考试', 'API'),
(5, 'grading:manage',  '阅卷管理', 'API'),
(6, 'stats:view',      '统计查看', 'API'),
(7, 'user:manage',     '用户管理', 'API')
ON CONFLICT (code) DO NOTHING;

-- 角色-权限
INSERT INTO sys_role_permission(role_id, permission_id) VALUES
(1,4),
(2,1),(2,2),(2,3),(2,5),(2,6),
(3,1),(3,2),(3,3),(3,5),(3,6),(3,7)
ON CONFLICT DO NOTHING;

-- 院系
INSERT INTO department(id, name) VALUES
(1, '中医学系'),(2, '针灸推拿系'),(3, '中药学系'),(4, '中西医结合系')
ON CONFLICT (id) DO NOTHING;

-- 班级
INSERT INTO clazz(id, name, department_id, grade) VALUES
(1, '中医学2023级1班', 1, '2023'),
(2, '中医学2023级2班', 1, '2023'),
(3, '针灸推拿2023级1班', 2, '2023'),
(4, '中药学2023级1班', 3, '2023'),
(5, '中医学2024级1班', 1, '2024')
ON CONFLICT (id) DO NOTHING;

-- 演示账号 (密码均为 123456，BCrypt)
INSERT INTO sys_user(id, username, password, real_name, clazz_id, role_code, status) VALUES
(1, 'admin', '$2a$10$QVsCa6Qw3Vlv.epYb/lI1.V5GmeHl6qfhD3xJcvRBVoquA/wCc/Xu', '系统管理员', NULL, 'ADMIN', 1),
(2, 'teacher', '$2a$10$QVsCa6Qw3Vlv.epYb/lI1.V5GmeHl6qfhD3xJcvRBVoquA/wCc/Xu', '张老师', NULL, 'TEACHER', 1),
(3, 'student1', '$2a$10$QVsCa6Qw3Vlv.epYb/lI1.V5GmeHl6qfhD3xJcvRBVoquA/wCc/Xu', '李小华', 1, 'STUDENT', 1),
(4, 'student2', '$2a$10$QVsCa6Qw3Vlv.epYb/lI1.V5GmeHl6qfhD3xJcvRBVoquA/wCc/Xu', '王小明', 1, 'STUDENT', 1),
(5, 'student3', '$2a$10$QVsCa6Qw3Vlv.epYb/lI1.V5GmeHl6qfhD3xJcvRBVoquA/wCc/Xu', '赵敏', 2, 'STUDENT', 1)
ON CONFLICT (id) DO NOTHING;

-- 用户-角色
INSERT INTO sys_user_role(user_id, role_id) VALUES
(1,3),(2,2),(3,1),(4,1),(5,1)
ON CONFLICT DO NOTHING;
