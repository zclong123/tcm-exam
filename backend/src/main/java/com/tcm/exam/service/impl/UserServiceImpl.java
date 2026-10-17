package com.tcm.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcm.exam.common.BizException;
import com.tcm.exam.common.PageResult;
import com.tcm.exam.entity.SysUser;
import com.tcm.exam.mapper.SysUserMapper;
import com.tcm.exam.security.UserContext;
import com.tcm.exam.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<SysUser> page(int current, int size, String keyword, String roleCode, Long clazzId) {
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword));
        }
        if (StringUtils.hasText(roleCode)) {
            qw.eq(SysUser::getRoleCode, roleCode);
        }
        if (clazzId != null) {
            qw.eq(SysUser::getClazzId, clazzId);
        }
        qw.orderByDesc(SysUser::getCreatedAt);
        Page<SysUser> page = userMapper.selectPage(new Page<>(current, size), qw);
        page.getRecords().forEach(u -> u.setPassword(null));
        return PageResult.of(page);
    }

    @Override
    public void create(SysUser user) {
        Long cnt = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, user.getUsername()));
        if (cnt != null && cnt > 0) {
            throw new BizException(400, "用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(StringUtils.hasText(user.getPassword())
                ? user.getPassword() : "123456"));
        if (user.getStatus() == null) user.setStatus(1);
        if (!StringUtils.hasText(user.getRoleCode())) user.setRoleCode("STUDENT");
        userMapper.insert(user);
    }

    @Override
    public void updateUser(SysUser user) {
        SysUser exist = userMapper.selectById(user.getId());
        if (exist == null) throw new BizException(400, "用户不存在");
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        userMapper.updateById(user);
    }

    @Override
    public void delete(Long id) {
        userMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public SysUser currentUser() {
        SysUser user = userMapper.selectById(UserContext.userId());
        if (user != null) user.setPassword(null);
        return user;
    }
}
