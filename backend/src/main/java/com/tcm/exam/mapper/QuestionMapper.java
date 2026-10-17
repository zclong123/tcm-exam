package com.tcm.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.exam.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 智能组卷：按题型/难度/知识点随机抽题（qtype 必填）
     */
    @Select("""
        <script>
        SELECT * FROM question q
        WHERE q.status = 1 AND q.deleted_at IS NULL
          AND q.qtype = #{qtype}
        <if test="subjectId != null"> AND q.subject_id = #{subjectId}</if>
        <if test="difficulty != null"> AND q.difficulty = #{difficulty}</if>
        <if test="kpId != null"> AND q.knowledge_point_id = #{kpId}</if>
        ORDER BY random()
        LIMIT #{limit}
        </script>
        """)
    List<Question> randomPick(String qtype, Long subjectId, Integer difficulty, Long kpId, int limit);

    /**
     * 随机抽题（qtype 可空）：练习场景按学科/知识点/难度随机抽取
     */
    @Select("""
        <script>
        SELECT * FROM question q
        WHERE q.status = 1 AND q.deleted_at IS NULL
        <if test="qtype != null"> AND q.qtype = #{qtype}</if>
        <if test="subjectId != null"> AND q.subject_id = #{subjectId}</if>
        <if test="difficulty != null"> AND q.difficulty = #{difficulty}</if>
        <if test="kpId != null"> AND q.knowledge_point_id = #{kpId}</if>
        ORDER BY random()
        LIMIT #{limit}
        </script>
        """)
    List<Question> randomPickAny(@Param("qtype") String qtype, @Param("subjectId") Long subjectId,
                                 @Param("difficulty") Integer difficulty, @Param("kpId") Long kpId,
                                 @Param("limit") int limit);
}
