package com.tcm.exam.vo;

import com.tcm.exam.dto.QuestionDTO;
import lombok.Data;

import java.util.List;

/**
 * 练习会话视图（题目已净化：不含答案/解析）
 */
@Data
public class PracticeSessionVO {
    private String sessionId;
    private String mode;          // CHAPTER | RANDOM
    private Integer count;
    private List<QuestionDTO> questions;
}
