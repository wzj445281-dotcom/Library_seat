package com.example.zhizuo.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.zhizuo.core.entity.ResourceOrder;
import com.example.zhizuo.core.entity.Resources;
import com.example.zhizuo.core.mapper.ResourceOrderMapper;
import com.example.zhizuo.core.mapper.ResourcesMapper;
import com.example.zhizuo.core.service.ResourceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-12-25
 */
@Service
public class ResourceServiceImpl extends ServiceImpl<ResourcesMapper, Resources> implements ResourceService {

    // 修复：更改变量名，避免与新的 OrderMapper (bean name 'orderMapper') 冲突
    // Spring 默认按名称装配，如果变量名叫 orderMapper，它会去应用中找 orderMapper bean
    @Resource
    private ResourceOrderMapper resourceOrderMapper;

    @Override
    public Page<Resources> getResourceList(int page, int size, String keyword, String category) {
        Page<Resources> pageInfo = new Page<>(page, size);
        QueryWrapper<Resources> wrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("name", keyword);
        }

        if (category != null && !category.isEmpty()) {
            wrapper.eq("category", category);
        }

        // 默认只查上架的
        wrapper.eq("status", 1);

        return this.page(pageInfo, wrapper);
    }
}