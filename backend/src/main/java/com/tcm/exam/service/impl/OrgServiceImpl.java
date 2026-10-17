package com.tcm.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tcm.exam.entity.Clazz;
import com.tcm.exam.entity.Department;
import com.tcm.exam.mapper.ClazzMapper;
import com.tcm.exam.mapper.DepartmentMapper;
import com.tcm.exam.service.OrgService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrgServiceImpl implements OrgService {

    private final DepartmentMapper departmentMapper;
    private final ClazzMapper clazzMapper;

    @Override
    public List<Department> listDepartments() {
        return departmentMapper.selectList(new LambdaQueryWrapper<Department>()
                .orderByAsc(Department::getId));
    }

    @Override
    public List<Clazz> listClazzes(Long departmentId) {
        LambdaQueryWrapper<Clazz> qw = new LambdaQueryWrapper<>();
        if (departmentId != null) qw.eq(Clazz::getDepartmentId, departmentId);
        return clazzMapper.selectList(qw.orderByAsc(Clazz::getId));
    }
}
