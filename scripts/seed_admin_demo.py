#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
admin 演示数据种子（幂等）。
为当前为空的 admin 相关核心表灌入真实演示数据，使管理台列表与仪表盘不再是空/全 mock。
连接：127.0.0.1:3306 / magao_platform / root:root123456（凭据来自 ~/.my.cnf）。
对每张表先 SELECT COUNT(*)，仅当为 0 时插入，可重复执行不重复。
"""
import pymysql
from datetime import datetime

HOST, PORT, USER, PASS, DB = "127.0.0.1", 3306, "root", "root123456", "magao_platform"
NOW = datetime.now().strftime("%Y-%m-%d %H:%M:%S")


def conn():
    return pymysql.connect(host=HOST, port=PORT, user=USER, password=PASS,
                            database=DB, charset="utf8mb4", autocommit=True)


def seed_only(cur, table, cols, rows):
    cur.execute(f"SELECT COUNT(*) FROM `{table}`")
    n = cur.fetchone()[0]
    if n and n > 0:
        print(f"  skip {table} (已有 {n} 行)")
        return
    placeholders = ", ".join(["%s"] * len(cols))
    col_sql = ", ".join(f"`{c}`" for c in cols)
    sql = f"INSERT INTO `{table}` ({col_sql}) VALUES ({placeholders})"
    cur.executemany(sql, rows)
    print(f"  insert {table}: {len(rows)} 行")


def main():
    c = conn()
    cur = c.cursor()
    print("== admin 演示数据种子 ==")

    # 分类（news / course / practice）
    seed_only(cur, "cms_category",
              ["name", "type", "parent_id", "sort_order", "status"],
              [("编程课程", "course", 0, 1, 1),
               ("平台公告", "news", 0, 2, 1),
               ("Scratch练习", "practice", 0, 3, 1)])

    # 课程
    seed_only(cur, "course_info",
              ["title", "subtitle", "teacher_name", "price", "original_price",
               "status", "category_id", "total_lessons", "student_count", "rating", "description"],
              [("Python基础入门", "零基础学Python", "张老师", 199.00, 299.00, 1, 1, 24, 1256, 4.8, "面向青少年的Python编程入门课程。"),
               ("C++算法竞赛", "信息学奥赛冲刺", "李老师", 299.00, 399.00, 1, 1, 36, 856, 4.9, "系统讲解C++算法与数据结构竞赛。"),
               ("GoC图形编程", "趣味图形化编程", "赵老师", 99.00, 159.00, 1, 1, 18, 680, 4.7, "用GoC绘制图形，培养编程兴趣。")])

    # 班级（teacher_id / course_id 为业务关联，无外键约束）
    seed_only(cur, "teach_class",
              ["class_name", "description", "teacher_id", "course_id", "max_student", "current_student", "status"],
              [("Python春季班", "2026春季Python入门班", 2, 1, 50, 32, 1),
               ("C++竞赛冲刺班", "信息学奥赛冲刺班", 3, 2, 30, 28, 1)])

    # 赛事（竞赛信息）
    seed_only(cur, "competition_info",
              ["title", "description", "start_time", "end_time", "status", "max_participants", "creator_id"],
              [("2026青少年编程创意大赛", "面向中小学生的编程创意作品征集。", "2026-03-01 09:00:00", "2026-05-31 18:00:00", 1, 500, 1),
               ("Scratch趣味挑战赛", "用Scratch创作趣味动画与游戏。", "2026-04-01 09:00:00", "2026-06-30 18:00:00", 1, 300, 1)])

    # Banner
    seed_only(cur, "cms_banner",
              ["title", "image_url", "link_url", "position", "sort_order", "status"],
              [("暑期编程训练营", "https://picsum.photos/seed/magao-b1/1200/400", "/course", "home", 1, 1),
               ("新年特惠课程", "https://picsum.photos/seed/magao-b2/1200/400", "/course", "home", 2, 1)])

    # 合作伙伴
    seed_only(cur, "cms_partner",
              ["name", "logo", "website", "description", "sort_order", "status"],
              [("中国青少年编程教育协会", "https://picsum.photos/seed/p1/200/200", "https://example.org", "致力于青少年编程教育推广。", 1, 1),
               ("示范科技大学计算机学院", "https://picsum.photos/seed/p2/200/200", "https://example.edu", "高校合作单位。", 2, 1)])

    # 资讯
    seed_only(cur, "cms_news",
              ["title", "summary", "content", "cover_image", "category_id", "author", "view_count", "sort_order", "is_pinned", "status", "publish_time"],
              [("平台新版上线公告", "magao平台全新版本正式上线。", "<p>我们很高兴地宣布，magao平台全新版本正式上线，带来更流畅的学习体验。</p>", "https://picsum.photos/seed/n1/800/400", 2, "运营团队", 1024, 1, 1, 1, NOW),
               ("2026赛季竞赛报名开启", "新赛季各类编程竞赛报名通道已开启。", "<p>2026赛季竞赛报名现已开启，欢迎同学们踊跃参加。</p>", "https://picsum.photos/seed/n2/800/400", 2, "竞赛组委会", 860, 2, 0, 1, NOW)])

    # 系统公告
    seed_only(cur, "sys_notice",
              ["title", "content", "notice_type", "status"],
              [("系统维护通知", "平台将于本周末进行例行维护，期间部分功能可能短暂不可用。", 2, 1),
               ("新功能上线提醒", "练习中心新增错题本功能，欢迎体验。", 1, 1)])

    # 汇总
    for t in ["cms_category", "course_info", "teach_class", "competition_info",
              "cms_banner", "cms_partner", "cms_news", "sys_notice"]:
        cur.execute(f"SELECT COUNT(*) FROM `{t}`")
        print(f"  {t}: {cur.fetchone()[0]} 行")
    cur.close()
    c.close()
    print("== 完成 ==")


if __name__ == "__main__":
    main()
