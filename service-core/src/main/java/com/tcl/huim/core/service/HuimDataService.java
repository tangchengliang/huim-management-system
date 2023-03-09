package com.tcl.huim.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcl.huim.core.pojo.entity.HuimData;
import com.tcl.huim.core.pojo.query.HuimDataQuery;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author tcl
 * @since 2023-01-06
 */
public interface HuimDataService extends IService<HuimData> {
    IPage<HuimData> listPage(Page<HuimData> pageParam, HuimDataQuery huimDataQuery);

    void mark(Long id, Integer status);
}
