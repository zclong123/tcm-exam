package com.tcm.exam.service;

import com.tcm.exam.vo.ScoreVO;
import com.tcm.exam.vo.StatsVO;

import java.util.List;
import java.util.Map;

public interface StatsService {
    /**
     * 我的成绩列表
     */
    List<ScoreVO> myScores();

    /**
     * 班级成绩分析（教师/管理员）
     */
    StatsVO.ClassStatsVO classStats(Long examId, Long clazzId);

    /**
     * 知识点掌握度（雷达图数据）
     */
    List<Map<String, Object>> knowledgeMastery(Long examId, Long clazzId);

    /**
     * 题目正确率/难度/区分度
     */
    List<Map<String, Object>> questionStats(Long subjectId);
}
