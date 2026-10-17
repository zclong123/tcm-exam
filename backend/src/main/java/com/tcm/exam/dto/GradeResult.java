package com.tcm.exam.dto;

/**
 * 单题判分结果：是否正确 + 标准答案文本
 */
public record GradeResult(boolean correct, String correctAnswer) {
}
