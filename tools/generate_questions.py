# -*- coding: utf-8 -*-
"""
中医考试题库生成器
从事实数据表（中药/方剂/腧穴/脉象/内科病证 + 手写题库）程序化生成 10000+ 道试题，
输出 backend/src/main/resources/sql/seed_questions.sql 及 stats 报告。
"""
import random
import sys

from tcm_data import HERBS, FANGJIS, POINTS, PULSES, INTERNAL, BASIC_THEORY, DIAGNOSIS
from tcm_hand_data import TERMS, SHORT, ESSAY, CASES

random.seed(20260824)

OPTION_KEYS = list("ABCDEFGH")

# 学科 id
SUBJECTS = {
    "中医基础理论": 1,
    "中医诊断学": 2,
    "中药学": 3,
    "方剂学": 4,
    "针灸学": 5,
    "中医内科学": 6,
}

# 各学科知识点（chapter -> id）
KP = {
    1: {"阴阳学说": 101, "五行学说": 102, "藏象": 103, "气血津液": 104, "经络": 105,
        "病因": 106, "病机": 107, "防治原则": 108},
    2: {"望诊": 201, "闻诊": 202, "问诊": 203, "切诊": 204, "八纲辨证": 205,
        "脏腑辨证": 206, "六经辨证": 207, "卫气营血辨证": 208, "舌诊": 209},
    3: {"解表药": 301, "清热药": 302, "泻下药": 303, "祛风湿药": 304, "化湿药": 305,
        "利水渗湿药": 306, "温里药": 307, "理气药": 308, "消食药": 309, "驱虫药": 310,
        "止血药": 311, "活血化瘀药": 312, "化痰止咳平喘药": 313, "安神药": 314,
        "平肝息风药": 315, "开窍药": 316, "补虚药": 317, "收涩药": 318,
        "攻毒杀虫止痒药": 319, "总论": 320},
    4: {"解表剂": 401, "泻下剂": 402, "和解剂": 403, "清热剂": 404, "温里剂": 405,
        "补益剂": 406, "固涩剂": 407, "安神剂": 408, "开窍剂": 409, "理气剂": 410,
        "理血剂": 411, "治风剂": 412, "治燥剂": 413, "祛湿剂": 414, "祛痰剂": 415,
        "消食剂": 416, "驱虫剂": 417, "痈疡剂": 418, "总论": 419},
    5: {"手太阴肺经": 501, "手阳明大肠经": 502, "足阳明胃经": 503, "足太阴脾经": 504,
        "手少阴心经": 505, "手太阳小肠经": 506, "足太阳膀胱经": 507, "足少阴肾经": 508,
        "手厥阴心包经": 509, "手少阳三焦经": 510, "足少阳胆经": 511, "足厥阴肝经": 512,
        "督脉": 513, "任脉": 514, "经外奇穴": 515, "经络": 516},
    6: {"肺系病证": 601, "心系病证": 602, "脾胃病证": 603, "肝胆病证": 604,
        "肾系病证": 605, "气血津液病证": 606, "肢体经络病证": 607, "总论": 608},
}


class Question:
    def __init__(self, qtype, subject, kp, stem, answer, analysis="",
                 options=None, diff=2, cog="记忆", score=1.0):
        self.qtype = qtype          # A / X / 判断 / 填空 / B / 名解 / 简答 / 论述 / 病案
        self.subject = subject
        self.kp = kp
        self.stem = stem
        self.answer = answer        # 字母串 or 对/错 or 文本
        self.analysis = analysis
        self.options = options or []  # [('A','..'),('B','..')]
        self.diff = diff
        self.cog = cog
        self.score = score

    def to_sql(self, qid):
        out = []
        sid = SUBJECTS[self.subject]
        kpid = KP[sid].get(self.kp, list(KP[sid].values())[0])
        diff = self.diff
        cog = self.cog
        out.append(
            "INSERT INTO question(id, subject_id, knowledge_point_id, qtype, difficulty, "
            "cognitive_level, stem, score, status, created_at, updated_at) VALUES "
            f"({qid},{sid},{kpid},'{self.qtype}',{diff},'{cog}','{esc(self.stem)}',"
            f"{self.score},1,now(),now());"
        )
        for k, txt in self.options:
            is_correct = 1 if k in [self.answer] or (len(self.answer) > 1 and k in self.answer) else 0
            out.append(
                f"INSERT INTO question_option(question_id, option_key, option_text, is_correct) "
                f"VALUES ({qid},'{k}','{esc(txt)}',{is_correct});"
            )
        if self.qtype in ("填空", "名解", "简答", "论述", "病案", "B"):
            out.append(
                f"INSERT INTO question_answer(question_id, answer_type, answer_text) "
                f"VALUES ({qid},'{self.qtype}','{esc(self.answer)}');"
            )
        if self.analysis:
            out.append(
                f"INSERT INTO question_analysis(question_id, analysis_text) "
                f"VALUES ({qid},'{esc(self.analysis)}');"
            )
        out.append(
            f"INSERT INTO question_knowledge(question_id, knowledge_point_id) "
            f"VALUES ({qid},{kpid});"
        )
        return "\n".join(out)


def esc(s):
    return s.replace("'", "''")


# ---------------- 干扰项工具 ----------------
def distractor(pool, correct, n):
    others = [x for x in pool if x != correct]
    random.shuffle(others)
    return others[:n]


def shuffle_opts(correct, dists, n_opts=4):
    """构造 (key,text) 选项列表，correct 在前，返回打乱后列表与正确 key"""
    pool = [correct] + dists[: n_opts - 1]
    random.shuffle(pool)
    opts = [(OPTION_KEYS[i], pool[i]) for i in range(len(pool))]
    ans_key = [k for k, t in opts if t == correct][0]
    return opts, ans_key


# 建立素材池
herb_xingwei_pool = list({h[2] for h in HERBS})
herb_guijing_pool = list({h[3] for h in HERBS})
herb_gongxiao_pool = list({h[4] for h in HERBS})
herb_zhuzhi_pool = list({h[5] for h in HERBS})
herb_cat_pool = list({h[1] for h in HERBS})
herb_name_pool = [h[0] for h in HERBS]

fj_gongxiao_pool = list({f[3] for f in FANGJIS})
fj_zhuzhi_pool = list({f[4] for f in FANGJIS})
fj_junyao_pool = list({f[5] for f in FANGJIS})
fj_cat_pool = list({f[1] for f in FANGJIS})
fj_name_pool = [f[0] for f in FANGJIS]
fj_drug_pool = []
for f in FANGJIS:
    fj_drug_pool += [d.strip() for d in f[2].replace("，", ",").split(",")]
fj_drug_pool = list({d for d in fj_drug_pool if d})

pt_channel_pool = list({p[1] for p in POINTS})
pt_dingwei_pool = list({p[2] for p in POINTS})
pt_zhuzhi_pool = list({p[3] for p in POINTS})
pt_texing_pool = list({p[4] for p in POINTS})
pt_name_pool = [p[0] for p in POINTS]

pulse_tedian_pool = list({p[1] for p in PULSES})
pulse_zhubing_pool = list({p[2] for p in PULSES})
pulse_name_pool = [p[0] for p in PULSES]

int_daibiaofang_pool = list({i[4] for i in INTERNAL})
int_bingji_pool = list({i[2] for i in INTERNAL})
int_zhuzheng_pool = list({i[1] for i in INTERNAL})
int_bing_pool = [i[0] for i in INTERNAL]


QUESTIONS = []


def add(q):
    QUESTIONS.append(q)


# ---------------- 中药学 ----------------
def gen_herb(h):
    name, cat, xingwei, guijing, gongxiao, zhuzhi = h
    # 1 性味单选
    opts, key = shuffle_opts(xingwei, distractor(herb_xingwei_pool, xingwei, 5), 4)
    add(Question("A", "中药学", cat, f"中药【{name}】的性味是？", key, f"{name}性味{xingwei}。",
                 opts, 2, "记忆"))
    # 2 归经单选
    opts, key = shuffle_opts(guijing, distractor(herb_guijing_pool, guijing, 5), 4)
    add(Question("A", "中药学", cat, f"中药【{name}】的归经是？", key, f"{name}归{guijing}。",
                 opts, 2, "记忆"))
    # 3 功效单选
    opts, key = shuffle_opts(gongxiao, distractor(herb_gongxiao_pool, gongxiao, 5), 4)
    add(Question("A", "中药学", cat, f"中药【{name}】的功效是？", key, f"{name}功效：{gongxiao}。",
                 opts, 3, "理解"))
    # 4 功效反选
    opts, key = shuffle_opts(name, distractor(herb_name_pool, name, 5), 4)
    add(Question("A", "中药学", cat, f"下列哪味中药具有【{gongxiao}】的功效？", key,
                 f"{name}功效为{gongxiao}。", opts, 3, "应用"))
    # 5 主治单选
    opts, key = shuffle_opts(zhuzhi, distractor(herb_zhuzhi_pool, zhuzhi, 5), 4)
    add(Question("A", "中药学", cat, f"中药【{name}】可用于治疗？", key, f"{name}主治：{zhuzhi}。",
                 opts, 3, "应用"))
    # 6 判断 功效对
    add(Question("判断", "中药学", cat, f"中药【{name}】具有【{gongxiao}】的功效。", "对",
                 f"{name}功效为{gongxiao}。", [("A", "对"), ("B", "错")], 2, "记忆"))
    # 7 判断 归经错
    w = distractor(herb_guijing_pool, guijing, 1)[0]
    add(Question("判断", "中药学", cat, f"中药【{name}】的归经是【{w}】。", "错",
                 f"{name}归{guijing}，而非{w}。", [("A", "对"), ("B", "错")], 3, "理解"))
    # 8 填空 分类
    add(Question("填空", "中药学", cat, f"中药【{name}】属于____类药物。", cat,
                 f"{name}属于{cat}。", [], 2, "记忆"))
    # 9 多选 分类
    same = [h2[0] for h2 in HERBS if h2[1] == cat and h2[0] != name]
    random.shuffle(same)
    others = [h2[0] for h2 in HERBS if h2[1] != cat]
    random.shuffle(others)
    corr = same[:4]
    dists = others[:2]
    pool = corr + dists
    random.shuffle(pool)
    opts = [(OPTION_KEYS[i], t) for i, t in zip(range(6), pool)]
    ans_key = "".join([k for k, t in opts if t in corr])
    add(Question("X", "中药学", cat, f"下列属于【{cat}】的中药有（　）", ans_key,
                 f"{cat}包括{cat}类常用药：{name}等；本题正确项为：{'、'.join(corr)}。",
                 opts, 3, "记忆"))
    # 10 分类单选
    opts, key = shuffle_opts(cat, distractor(herb_cat_pool, cat, 5), 4)
    add(Question("A", "中药学", cat, f"中药【{name}】属于下列哪一类药？", key, f"{name}属于{cat}。",
                 opts, 2, "记忆"))
    # 11 判断 主治对
    add(Question("判断", "中药学", cat, f"中药【{name}】可用于治疗【{zhuzhi}】。", "对",
                 f"{name}主治{zhuzhi}。", [("A", "对"), ("B", "错")], 2, "应用"))
    # 12 填空 反功效
    add(Question("填空", "中药学", cat, f"具有【{gongxiao}】功效的常用中药是____。", name,
                 f"{name}具有{gongxiao}之功效。", [], 3, "应用"))
    # 13 判断 性味对
    add(Question("判断", "中药学", cat, f"中药【{name}】的性味是【{xingwei}】。", "对",
                 f"{name}性味{xingwei}。", [("A", "对"), ("B", "错")], 2, "记忆"))
    # 14 判断 分类对
    add(Question("判断", "中药学", cat, f"中药【{name}】属于【{cat}】。", "对",
                 f"{name}属于{cat}。", [("A", "对"), ("B", "错")], 2, "记忆"))
    # 15 填空 归经
    add(Question("填空", "中药学", cat, f"中药【{name}】归____经。", guijing,
                 f"{name}归{guijing}。", [], 2, "记忆"))
    # 16 反归经
    opts, key = shuffle_opts(name, distractor(herb_name_pool, name, 5), 4)
    add(Question("A", "中药学", cat, f"下列哪味中药归【{guijing}】经？", key,
                 f"{name}归{guijing}。", opts, 3, "理解"))
    # 17 主治反选
    opts, key = shuffle_opts(name, distractor(herb_name_pool, name, 5), 4)
    add(Question("A", "中药学", cat, f"下列哪味中药可治疗【{zhuzhi}】？", key,
                 f"{name}主治：{zhuzhi}。", opts, 4, "应用"))
    # 18 判断 主治错
    w = distractor(herb_zhuzhi_pool, zhuzhi, 1)[0]
    add(Question("判断", "中药学", cat, f"中药【{name}】可用于治疗【{w}】。", "错",
                 f"{name}主治{zhuzhi}，而非{w}。", [("A", "对"), ("B", "错")], 3, "理解"))


for h in HERBS:
    gen_herb(h)

# ---------------- 方剂学 ----------------
def gen_fangji(f):
    name, cat, zucheng, gongxiao, zhuzhi, junyao = f
    # 1 功效
    opts, key = shuffle_opts(gongxiao, distractor(fj_gongxiao_pool, gongxiao, 5), 4)
    add(Question("A", "方剂学", cat, f"方剂【{name}】的功效是？", key, f"{name}功效：{gongxiao}。",
                 opts, 3, "理解"))
    # 2 主治
    opts, key = shuffle_opts(zhuzhi, distractor(fj_zhuzhi_pool, zhuzhi, 5), 4)
    add(Question("A", "方剂学", cat, f"方剂【{name}】的主治是？", key, f"{name}主治：{zhuzhi}。",
                 opts, 3, "理解"))
    # 3 反主治
    opts, key = shuffle_opts(name, distractor(fj_name_pool, name, 5), 4)
    add(Question("A", "方剂学", cat, f"下列何方的主治是【{zhuzhi}】？", key,
                 f"{name}主治{zhuzhi}。", opts, 4, "应用"))
    # 4 君药
    opts, key = shuffle_opts(junyao, distractor(fj_junyao_pool, junyao, 5), 4)
    add(Question("A", "方剂学", cat, f"方剂【{name}】的君药是？", key,
                 f"{name}以{junyao}为君药。", opts, 3, "理解"))
    # 5 判断 组成
    drugs = [d.strip() for d in zucheng.replace("，", ",").split(",") if d.strip()]
    drug1 = drugs[0] if drugs else junyao
    add(Question("判断", "方剂学", cat, f"方剂【{name}】的组成中含有药物【{drug1}】。", "对",
                 f"{name}组成为：{zucheng}。", [("A", "对"), ("B", "错")], 3, "记忆"))
    # 6 判断 功效错
    w = distractor(fj_gongxiao_pool, gongxiao, 1)[0]
    add(Question("判断", "方剂学", cat, f"方剂【{name}】的功效是【{w}】。", "错",
                 f"{name}功效为{gongxiao}，而非{w}。", [("A", "对"), ("B", "错")], 3, "理解"))
    # 7 填空 君药
    add(Question("填空", "方剂学", cat, f"方剂【{name}】的君药是____。", junyao,
                 f"{name}以{junyao}为君药。", [], 3, "记忆"))
    # 8 多选 分类
    same = [f2[0] for f2 in FANGJIS if f2[1] == cat and f2[0] != name]
    random.shuffle(same)
    others = [f2[0] for f2 in FANGJIS if f2[1] != cat]
    random.shuffle(others)
    corr = same[:4]
    dists = others[:2]
    pool = corr + dists
    random.shuffle(pool)
    opts = [(OPTION_KEYS[i], t) for i, t in zip(range(6), pool)]
    ans_key = "".join([k for k, t in opts if t in corr])
    add(Question("X", "方剂学", cat, f"下列属于【{cat}】的方剂有（　）", ans_key,
                 f"{cat}常用方包括：{'、'.join(corr)}。", opts, 3, "记忆"))
    # 9 反功效
    opts, key = shuffle_opts(name, distractor(fj_name_pool, name, 5), 4)
    add(Question("A", "方剂学", cat, f"下列何方具有【{gongxiao}】的功效？", key,
                 f"{name}功效为{gongxiao}。", opts, 4, "应用"))
    # 10 不属于组成
    not_in = distractor(fj_drug_pool, drug1, 5) + distractor(herb_name_pool, drug1, 5)
    # 确保干扰项不在组成中
    not_in = [d for d in not_in if d not in drugs]
    random.shuffle(not_in)
    wrong = not_in[0]
    opts, key = shuffle_opts(wrong, drugs[:3], 4)
    add(Question("A", "方剂学", cat, f"下列哪味药物不属于方剂【{name}】的组成？", key,
                 f"{name}组成为：{zucheng}。", opts, 4, "理解"))
    # 11 判断 君药对
    add(Question("判断", "方剂学", cat, f"方剂【{name}】以【{junyao}】为君药。", "对",
                 f"{name}以{junyao}为君药。", [("A", "对"), ("B", "错")], 3, "记忆"))
    # 12 反君药
    opts, key = shuffle_opts(name, distractor(fj_name_pool, name, 5), 4)
    add(Question("A", "方剂学", cat, f"下列何方以【{junyao}】为君药？", key,
                 f"{name}以{junyao}为君药。", opts, 4, "应用"))
    # 13 填空 组成
    drug2 = drugs[1] if len(drugs) > 1 else drug1
    add(Question("填空", "方剂学", cat, f"方剂【{name}】的组成中含有药物____。", drug2,
                 f"{name}组成为：{zucheng}。", [], 3, "记忆"))
    # 14 组成填空（缺方名）
    add(Question("填空", "方剂学", cat, f"组成中含有【{zucheng}】的方剂是____。", name,
                 f"{name}组成为：{zucheng}。", [], 4, "应用"))
    # 15 判断 主治对
    add(Question("判断", "方剂学", cat, f"方剂【{name}】的主治是【{zhuzhi}】。", "对",
                 f"{name}主治：{zhuzhi}。", [("A", "对"), ("B", "错")], 3, "理解"))
    # 16 反组成（哪味在组成中）
    opts, key = shuffle_opts(drug1, distractor(fj_drug_pool, drug1, 5)[:3], 4)
    add(Question("A", "方剂学", cat, f"下列哪味药物属于方剂【{name}】的组成？", key,
                 f"{name}组成为：{zucheng}。", opts, 4, "理解"))


for f in FANGJIS:
    gen_fangji(f)

# ---------------- 针灸学 ----------------
def gen_point(p):
    name, channel, dingwei, zhuzhi, teshuxue = p
    # 1 归经
    opts, key = shuffle_opts(channel, distractor(pt_channel_pool, channel, 5), 4)
    add(Question("A", "针灸学", channel, f"腧穴【{name}】属于下列哪条经脉？", key,
                 f"{name}属{channel}。", opts, 2, "记忆"))
    # 2 定位
    opts, key = shuffle_opts(dingwei, distractor(pt_dingwei_pool, dingwei, 5), 4)
    add(Question("A", "针灸学", channel, f"腧穴【{name}】的定位是？", key,
                 f"{name}定位：{dingwei}。", opts, 3, "记忆"))
    # 3 特定穴
    opts, key = shuffle_opts(teshuxue, distractor(pt_texing_pool, teshuxue, 5), 4)
    add(Question("A", "针灸学", channel, f"腧穴【{name}】是下列哪种特定穴？", key,
                 f"{name}为{teshuxue}。", opts, 3, "理解"))
    # 4 判断 归经对
    add(Question("判断", "针灸学", channel, f"腧穴【{name}】属于{channel}。", "对",
                 f"{name}属{channel}。", [("A", "对"), ("B", "错")], 2, "记忆"))
    # 5 填空 归经
    add(Question("填空", "针灸学", channel, f"腧穴【{name}】归____经。", channel,
                 f"{name}属{channel}。", [], 2, "记忆"))
    # 6 多选 同经
    same = [p2[0] for p2 in POINTS if p2[1] == channel and p2[0] != name]
    random.shuffle(same)
    others = [p2[0] for p2 in POINTS if p2[1] != channel]
    random.shuffle(others)
    corr = same[:4]
    dists = others[:2]
    pool = corr + dists
    random.shuffle(pool)
    opts = [(OPTION_KEYS[i], t) for i, t in zip(range(6), pool)]
    ans_key = "".join([k for k, t in opts if t in corr])
    add(Question("X", "针灸学", channel, f"下列腧穴中属于【{channel}】的有（　）", ans_key,
                 f"{channel}腧穴包括：{'、'.join(corr)}。", opts, 3, "记忆"))
    # 7 主治
    opts, key = shuffle_opts(zhuzhi, distractor(pt_zhuzhi_pool, zhuzhi, 5), 4)
    add(Question("A", "针灸学", channel, f"腧穴【{name}】的主治是？", key,
                 f"{name}主治：{zhuzhi}。", opts, 3, "应用"))
    # 8 判断 主治对
    add(Question("判断", "针灸学", channel, f"腧穴【{name}】可治疗【{zhuzhi}】。", "对",
                 f"{name}主治{zhuzhi}。", [("A", "对"), ("B", "错")], 3, "应用"))
    # 9 判断 特定穴对
    add(Question("判断", "针灸学", channel, f"腧穴【{name}】是{teshuxue}。", "对",
                 f"{name}为{teshuxue}。", [("A", "对"), ("B", "错")], 3, "记忆"))
    # 10 定位反选
    opts, key = shuffle_opts(name, distractor(pt_name_pool, name, 5), 4)
    add(Question("A", "针灸学", channel, f"定位在【{dingwei}】的腧穴是？", key,
                 f"{name}定位：{dingwei}。", opts, 3, "记忆"))
    # 11 判断 定位对
    add(Question("判断", "针灸学", channel, f"腧穴【{name}】的定位是{dingwei}。", "对",
                 f"{name}定位：{dingwei}。", [("A", "对"), ("B", "错")], 2, "记忆"))
    # 12 特定穴反选
    opts, key = shuffle_opts(name, distractor(pt_name_pool, name, 5), 4)
    add(Question("A", "针灸学", channel, f"下列腧穴中，{teshuxue}的是？", key,
                 f"{name}为{teshuxue}。", opts, 3, "理解"))


for p in POINTS:
    gen_point(p)

# ---------------- 脉象 ----------------
def gen_pulse(p):
    name, tedian, zhubing = p
    opts, key = shuffle_opts(tedian, distractor(pulse_tedian_pool, tedian, 5), 4)
    add(Question("A", "中医诊断学", "切诊", f"【{name}】的脉象特点是？", key,
                 f"{name}：{tedian}。", opts, 3, "记忆"))
    opts, key = shuffle_opts(zhubing, distractor(pulse_zhubing_pool, zhubing, 5), 4)
    add(Question("A", "中医诊断学", "切诊", f"【{name}】的主病是？", key,
                 f"{name}主{zhubing}。", opts, 3, "理解"))
    add(Question("判断", "中医诊断学", "切诊", f"【{name}】多主{zhubing}。", "对",
                 f"{name}主{zhubing}。", [("A", "对"), ("B", "错")], 2, "记忆"))
    opts, key = shuffle_opts(name, distractor(pulse_name_pool, name, 5), 4)
    add(Question("A", "中医诊断学", "切诊", f"脉象表现为【{tedian}】的是哪一种脉？", key,
                 f"该脉象为{name}。", opts, 3, "理解"))
    add(Question("填空", "中医诊断学", "切诊", f"脉见【{tedian}】，此为____脉。", name,
                 f"此为{name}脉。", [], 3, "理解"))
    w = distractor(pulse_tedian_pool, tedian, 1)[0]
    add(Question("判断", "中医诊断学", "切诊", f"【{name}】的脉象特点是【{w}】。", "错",
                 f"{name}脉象为{tedian}，而非{w}。", [("A", "对"), ("B", "错")], 3, "理解"))


for p in PULSES:
    gen_pulse(p)

# ---------------- 中医内科学 ----------------
def gen_internal(i):
    bing, zhuzheng, bingji, zhifa, daibiaofang = i
    opts, key = shuffle_opts(zhifa, distractor([i2[3] for i2 in INTERNAL], zhifa, 5), 4)
    add(Question("A", "中医内科学", "总论", f"中医内科学病证【{bing}】的治法是？", key,
                 f"{bing}治法：{zhifa}。", opts, 3, "理解"))
    opts, key = shuffle_opts(daibiaofang, distractor(int_daibiaofang_pool, daibiaofang, 5), 4)
    add(Question("A", "中医内科学", "总论", f"病证【{bing}】的代表方是？", key,
                 f"{bing}代表方为{daibiaofang}。", opts, 3, "理解"))
    opts, key = shuffle_opts(bingji, distractor(int_bingji_pool, bingji, 5), 4)
    add(Question("A", "中医内科学", "总论", f"病证【{bing}】的基本病机是？", key,
                 f"{bing}病机：{bingji}。", opts, 4, "理解"))
    add(Question("判断", "中医内科学", "总论", f"【{bing}】以{zhuzheng}为主症。", "对",
                 f"{bing}主症：{zhuzheng}。", [("A", "对"), ("B", "错")], 2, "记忆"))
    add(Question("填空", "中医内科学", "总论", f"病证【{bing}】的代表方是____。", daibiaofang,
                 f"{bing}代表方为{daibiaofang}。", [], 3, "记忆"))
    opts, key = shuffle_opts(bing, distractor(int_bing_pool, bing, 5), 4)
    add(Question("A", "中医内科学", "总论", f"下列病证中，以【{zhuzheng}】为主症的是？", key,
                 f"{bing}以{zhuzheng}为主症。", opts, 4, "应用"))
    # 治法反选
    opts, key = shuffle_opts(bing, distractor(int_bing_pool, bing, 5), 4)
    add(Question("A", "中医内科学", "总论", f"下列病证中，治法为【{zhifa}】的是？", key,
                 f"{bing}治法：{zhifa}。", opts, 4, "应用"))
    # 判断 病机对
    add(Question("判断", "中医内科学", "总论", f"【{bing}】的基本病机是{bingji}。", "对",
                 f"{bing}病机：{bingji}。", [("A", "对"), ("B", "错")], 3, "理解"))


for i in INTERNAL:
    gen_internal(i)

# ---------------- B 型（配伍题） ----------------
def gen_btype():
    # 方剂配伍：备选项为方剂名，配对题干为功效
    cat_groups = {}
    for f in FANGJIS:
        cat_groups.setdefault(f[1], []).append(f)
    n = 0
    for cat, fs in cat_groups.items():
        random.shuffle(fs)
        for i in range(0, len(fs) - 4, 5):
            grp = fs[i:i + 5]
            names = [g[0] for g in grp]
            gongxiaos = [g[3] for g in grp]
            # 备选项 A-E 为方剂名
            opts = [(k, nm) for k, nm in zip("ABCDE", names)]
            for gi, g in enumerate(grp):
                stem = ("（配伍题）备选项："
                        + "  ".join([f"{k}.{nm}" for k, nm in zip("ABCDE", names)])
                        + "。请选择功效为【" + g[3] + "】的方剂（　）")
                add(Question("B", "方剂学", cat, stem, "ABCDE"[gi],
                             f"{g[0]}功效：{g[3]}。", opts, 4, "应用"))
                n += 1
    # 中药配伍：备选项为药名，配对题干为功效
    cat_groups = {}
    for h in HERBS:
        cat_groups.setdefault(h[1], []).append(h)
    for cat, hs in cat_groups.items():
        random.shuffle(hs)
        for i in range(0, len(hs) - 4, 5):
            grp = hs[i:i + 5]
            names = [g[0] for g in grp]
            gongxiaos = [g[4] for g in grp]
            opts = [(k, nm) for k, nm in zip("ABCDE", names)]
            for gi, g in enumerate(grp):
                stem = ("（配伍题）备选项："
                        + "  ".join([f"{k}.{nm}" for k, nm in zip("ABCDE", names)])
                        + "。请选择功效为【" + g[4] + "】的中药（　）")
                add(Question("B", "中药学", cat, stem, "ABCDE"[gi],
                             f"{g[0]}功效：{g[4]}。", opts, 4, "应用"))
                n += 1


gen_btype()

# ---------------- 手写题库 ----------------
for subj, kp, qtype, stem, ans, diff in TERMS:
    add(Question(qtype, subj, kp, f"名词解释：{stem}", ans, "", [], diff, "理解", 5.0))
for subj, kp, qtype, stem, ans, diff in SHORT:
    add(Question(qtype, subj, kp, f"简答题：{stem}", ans, "", [], diff, "理解", 10.0))
for subj, kp, qtype, stem, ans, diff in ESSAY:
    add(Question(qtype, subj, kp, f"论述题：{stem}", ans, "", [], diff, "分析", 20.0))
for subj, kp, stem, ans, diff in CASES:
    add(Question("病案", subj, kp, f"病案分析：{stem}", ans, "", [], diff, "分析", 20.0))
for subj, kp, qtype, stem, opts, ans, analysis in BASIC_THEORY:
    add(Question(qtype, subj, kp, stem, ans, analysis, list(opts.items()), 2, "记忆"))
for subj, kp, qtype, stem, opts, ans, analysis in DIAGNOSIS:
    add(Question(qtype, subj, kp, stem, ans, analysis, list(opts.items()), 3, "理解"))

# ---------------- 统计 ----------------
from collections import Counter
type_cnt = Counter(q.qtype for q in QUESTIONS)
subj_cnt = Counter(q.subject for q in QUESTIONS)
print("总题数:", len(QUESTIONS))
print("题型分布:", dict(type_cnt))
print("学科分布:", dict(subj_cnt))
print("其中客观题:", len(QUESTIONS) - type_cnt.get("名解", 0) - type_cnt.get("简答", 0)
      - type_cnt.get("论述", 0) - type_cnt.get("病案", 0))

if len(QUESTIONS) < 10000:
    print(f"!! 题数不足 10000，仅 {len(QUESTIONS)}，需要扩充事实数据表。", file=sys.stderr)
    sys.exit(1)

# ---------------- 输出 SQL ----------------
OUT = "D:/aiproject/claudecode/demo1/backend/src/main/resources/sql/seed_questions.sql"
lines = []
lines.append("-- 中医考试题库种子数据（程序化生成，勿手工修改）")
lines.append(f"-- 总题数：{len(QUESTIONS)}")
lines.append("")
lines.append("-- ============ 学科 ============")
for name, sid in sorted(SUBJECTS.items(), key=lambda x: x[1]):
    lines.append(f"INSERT INTO subject(id, name) VALUES ({sid}, '{name}');")
lines.append("")
lines.append("-- ============ 知识点 ============")
for sid, kps in sorted(KP.items()):
    for name, kpid in sorted(kps.items(), key=lambda x: x[1]):
        lines.append(f"INSERT INTO knowledge_point(id, subject_id, name, parent_id) "
                     f"VALUES ({kpid}, {sid}, '{name}', NULL);")
lines.append("")
lines.append("-- ============ 试题 ============")
qid = 1
for q in QUESTIONS:
    lines.append(q.to_sql(qid))
    qid += 1
lines.append("")
lines.append("-- question.id 为显式主键（应用侧 @TableId(INPUT) 自备 id），无需重置序列；"
             "各 identity 子表序列由 PG 自动管理")

with open(OUT, "w", encoding="utf-8") as fp:
    fp.write("\n".join(lines))

print("SQL 已写入:", OUT)
