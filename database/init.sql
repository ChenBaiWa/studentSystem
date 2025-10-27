1课本-- 学生管理系统数据库初始化脚本

-- 创建用户表
CREATE TABLE sys_user (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    user_role TINYINT NOT NULL COMMENT '用户角色 1:老师, 2:学生',
    phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    password VARCHAR(50) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '系统用户表';

-- 创建学生表
CREATE TABLE student (
    id BIGINT PRIMARY KEY COMMENT '学生ID',
    name VARCHAR(50) NOT NULL COMMENT '学生姓名',
    gender VARCHAR(10) NOT NULL COMMENT '性别',
    age INT NOT NULL COMMENT '年龄',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    address VARCHAR(200) COMMENT '地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '学生表';

-- 创建年级表
CREATE TABLE grade (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '年级ID',
    name VARCHAR(50) NOT NULL COMMENT '年级名称',
    creator_id INT NOT NULL COMMENT '创建人ID',
    creator_name VARCHAR(50) NOT NULL COMMENT '创建人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_grade_name_creator (name, creator_id)
) COMMENT '年级表';

-- 创建课本表
CREATE TABLE textbook (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '课本ID',
    name VARCHAR(100) NOT NULL COMMENT '课本名称',
    grade_id BIGINT NOT NULL COMMENT '年级ID',
    grade_name VARCHAR(50) NOT NULL COMMENT '年级名称',
    creator_id INT NOT NULL COMMENT '创建人ID',
    creator_name VARCHAR(50) NOT NULL COMMENT '创建人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_textbook_name_grade_creator (name, grade_id, creator_id),
    FOREIGN KEY (grade_id) REFERENCES grade(id) ON DELETE CASCADE
) COMMENT '课本表';

-- 创建章节表
CREATE TABLE chapter (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '章节ID',
    name VARCHAR(100) NOT NULL COMMENT '章节名称',
    textbook_id BIGINT NOT NULL COMMENT '课本ID',
    textbook_name VARCHAR(100) NOT NULL COMMENT '课本名称',
    creator_id INT NOT NULL COMMENT '创建人ID',
    creator_name VARCHAR(50) NOT NULL COMMENT '创建人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (textbook_id) REFERENCES textbook(id) ON DELETE CASCADE
) COMMENT '章节表';

-- 插入初始用户数据
INSERT INTO sys_user (user_role, phone, password, real_name) VALUES
(1, '13800000001', 'e10adc3949ba59abbe56e057f20f883e', '张老师'),  -- 密码: 123456
(2, '13800000002', 'e10adc3949ba59abbe56e057f20f883e', '李学生'); -- 密码: 123456

-- 插入初始年级数据
INSERT INTO grade (name, creator_id, creator_name) VALUES
('初一', 1, '张老师'),
('初二', 1, '张老师'),
('初三', 1, '张老师');

-- 插入初始课本数据
INSERT INTO textbook (name, grade_id, grade_name, creator_id, creator_name) VALUES
('人教版数学七年级上册', 1, '初一', 1, '张老师'),
('人教版语文七年级上册', 1, '初一', 1, '张老师');

-- 插入初始章节数据
INSERT INTO chapter (name, textbook_id, textbook_name, creator_id, creator_name) VALUES
('第一章 有理数', 1, '人教版数学七年级上册', 1, '张老师'),
('第二章 整式的加减', 1, '人教版数学七年级上册', 1, '张老师');

-- 创建班级表
CREATE TABLE class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '班级ID',
    name VARCHAR(50) NOT NULL COMMENT '班级名称',
    class_code VARCHAR(6) NOT NULL UNIQUE COMMENT '班级号(6位数字)',
    verification_code VARCHAR(4) NOT NULL COMMENT '验证码(4位数字)',
    grade_id BIGINT NOT NULL COMMENT '年级ID',
    grade_name VARCHAR(50) NOT NULL COMMENT '年级名称',
    creator_id INT NOT NULL COMMENT '创建人ID',
    creator_name VARCHAR(50) NOT NULL COMMENT '创建人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_class_name_creator (name, creator_id),
    FOREIGN KEY (grade_id) REFERENCES grade(id) ON DELETE CASCADE
) COMMENT '班级表';

-- 创建学生-班级关联表
CREATE TABLE student_class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    class_id BIGINT NOT NULL COMMENT '班级ID',
    join_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    UNIQUE KEY uk_student_class (student_id, class_id),
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES class(id) ON DELETE CASCADE
) COMMENT '学生-班级关联表';

-- 创建作业表
CREATE TABLE assignment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '作业ID',
    title VARCHAR(100) NOT NULL COMMENT '作业标题',
    class_id BIGINT NOT NULL COMMENT '班级ID',
    class_name VARCHAR(50) NOT NULL COMMENT '班级名称',
    chapter_id BIGINT NOT NULL COMMENT '章节ID',
    chapter_name VARCHAR(100) NOT NULL COMMENT '章节名称',
    content TEXT NOT NULL COMMENT '作业内容',
    total_score INT NOT NULL COMMENT '总分',
    deadline DATETIME NOT NULL COMMENT '截止时间',
    creator_id INT NOT NULL COMMENT '创建人ID(老师ID)',
    creator_name VARCHAR(50) NOT NULL COMMENT '创建人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (class_id) REFERENCES class(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapter(id) ON DELETE CASCADE
) COMMENT '作业表';

-- 创建学生作业提交表
CREATE TABLE student_assignment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '学生作业ID',
    assignment_id BIGINT NOT NULL COMMENT '作业ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    student_name VARCHAR(50) NOT NULL COMMENT '学生姓名',
    answer TEXT COMMENT '学生答案',
    score INT COMMENT '得分',
    feedback TEXT COMMENT '批注',
    status VARCHAR(20) NOT NULL DEFAULT 'submitted' COMMENT '状态: submitted(已提交) / graded(已批改)',
    submit_time DATETIME COMMENT '提交时间',
    grade_time DATETIME COMMENT '批改时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_assignment_student (assignment_id, student_id),
    FOREIGN KEY (assignment_id) REFERENCES assignment(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE
) COMMENT '学生作业提交表';

-- 创建习题集表
CREATE TABLE exercise_set (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '习题集ID',
    name VARCHAR(100) NOT NULL COMMENT '习题集名称',
    subject VARCHAR(50) NOT NULL COMMENT '学科',
    creator_id BIGINT NOT NULL COMMENT '创建人ID(老师ID)',
    creator_name VARCHAR(50) NOT NULL COMMENT '创建人姓名',
    status VARCHAR(20) NOT NULL DEFAULT 'editing' COMMENT '状态: editing(编辑中) / published(已发布)',
    question_count INT NOT NULL DEFAULT 0 COMMENT '题目数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_creator_id (creator_id)
) COMMENT '习题集表';

-- 创建题目表
CREATE TABLE question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    exercise_set_id BIGINT NOT NULL COMMENT '习题集ID',
    chapter_id BIGINT COMMENT '章节ID',
    chapter_name VARCHAR(100) COMMENT '章节名称',
    type VARCHAR(20) NOT NULL COMMENT '题型: choice(选择题) / fill(填空题) / subjective(主观题)',
    content TEXT NOT NULL COMMENT '题干内容',
    options TEXT COMMENT '选项（JSON格式存储，适用于选择题）',
    answer TEXT COMMENT '标准答案',
    score INT NOT NULL DEFAULT 5 COMMENT '分值',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_exercise_set_id (exercise_set_id),
    INDEX idx_chapter_id (chapter_id),
    FOREIGN KEY (exercise_set_id) REFERENCES exercise_set(id) ON DELETE CASCADE
) COMMENT '题目表';

-- 创建学生习题答题记录表
CREATE TABLE student_exercise_answer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '答题记录ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    answer TEXT COMMENT '学生答案',
    answer_type TINYINT NOT NULL DEFAULT 1 COMMENT '答案类型: 1=文本, 2=图片',
    image_urls TEXT COMMENT '图片URL列表（JSON格式存储）',
    score INT COMMENT '得分',
    remark TEXT COMMENT '批注',
    correct_status TINYINT NOT NULL DEFAULT 0 COMMENT '是否正确: 0=未批改, 1=正确, 2=错误',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_student_question (student_id, question_id),
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE
) COMMENT '学生习题答题记录表';
