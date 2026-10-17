package com.tcm.exam.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StatsVO {

    @Data
    public static class ClassStatsVO {
        private Integer studentCount;
        private BigDecimal avgScore;
        private BigDecimal maxScore;
        private BigDecimal minScore;
        private BigDecimal passRate;          // 通过率 0-1
        private List<ScoreVO> topScores;      // 前10
        private List<ScoreVO> allScores;
    }
}
