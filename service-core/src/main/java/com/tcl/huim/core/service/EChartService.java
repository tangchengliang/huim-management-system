package com.tcl.huim.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tcl.huim.core.pojo.echart.NumTimeCov;
import com.tcl.huim.core.pojo.entity.EChart;
import com.tcl.huim.core.pojo.query.EChartQuery;

public interface EChartService extends IService<EChart> {
    NumTimeCov compareList(EChartQuery eChartQuery);
    NumTimeCov compareEvolution(EChartQuery eChartQuery);
}
