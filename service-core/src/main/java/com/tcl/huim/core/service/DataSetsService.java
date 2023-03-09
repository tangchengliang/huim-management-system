package com.tcl.huim.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcl.huim.core.pojo.entity.DataSets;

public interface DataSetsService extends IService<DataSets> {
    IPage<DataSets> listPage(Page<DataSets> pageParam);


    boolean removeByIdAndFile(Long id);
}
