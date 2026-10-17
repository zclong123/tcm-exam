package com.tcm.exam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcm.exam.common.PageResult;
import com.tcm.exam.entity.SysUser;

import java.util.List;

public interface UserService {
    PageResult<SysUser> page(int current, int size, String keyword, String roleCode, Long clazzId);

    void create(SysUser user);

    void updateUser(SysUser user);

    void delete(Long id);

    void updateStatus(Long id, Integer status);

    SysUser currentUser();
}
