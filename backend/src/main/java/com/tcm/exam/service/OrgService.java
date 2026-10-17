package com.tcm.exam.service;

import com.tcm.exam.entity.Clazz;
import com.tcm.exam.entity.Department;

import java.util.List;

public interface OrgService {
    List<Department> listDepartments();

    List<Clazz> listClazzes(Long departmentId);
}
