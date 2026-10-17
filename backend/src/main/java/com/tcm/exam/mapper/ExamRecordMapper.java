package com.tcm.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.exam.entity.ExamRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {
}
