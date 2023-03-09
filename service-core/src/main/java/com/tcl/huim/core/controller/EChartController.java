package com.tcl.huim.core.controller;

import com.tcl.common.result.R;
import com.tcl.huim.core.pojo.echart.NumTimeCov;
import com.tcl.huim.core.pojo.entity.EChart;
import com.tcl.huim.core.pojo.query.EChartQuery;
import com.tcl.huim.core.service.EChartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@CrossOrigin
@RestController
@Api(tags = "算法对比")
@RequestMapping("/admin/core/echart")
public class EChartController {

    @Resource
    private EChartService eChartService;

    @ApiOperation("结果列表")
    @GetMapping("/list")
    public R listAll() {

        List<EChart> list = eChartService.list();
        return R.ok().data("list", list).message("获取列表成功");
    }

    @ApiOperation("根据参数可视化ECharts")
    @PostMapping("/time")
    public R save(
            @ApiParam(value = "可视化参数——算法、数据集、长度约束等", required = true)
            @RequestBody EChartQuery eChartQuery){

        List<EChart> list = new LinkedList<>();
        return R.ok().data("list", list).message("获取列表成功");
    }

    @ApiOperation("根据参数可视化比较HUIM-LC-BA和HUIM-LC-MPA")
    @GetMapping("/compareNTC")
    public R compareNTC(
            @ApiParam(value = "可视化参数——算法、数据集、长度约束等", required = true)
                    EChartQuery eChartQuery){

        //todo echartQuery 数据点对应
        NumTimeCov numTimeCov = new NumTimeCov();
        numTimeCov = eChartService.compareList(eChartQuery);
        return R.ok().data("numTimeCov", numTimeCov).message("获取Echart结果成功");
    }

    @ApiOperation("根据参数可视化比较HUIM-BA、HUIM-MPA、HUIM-PSO和HUIM-BA")
    @GetMapping("/compareEvolution")
    public R compareEvolution(
            @ApiParam(value = "可视化参数——算法、数据集、长度约束等", required = true)
                    EChartQuery eChartQuery){

        //todo echartQuery 数据点对应
        NumTimeCov numTimeCov = new NumTimeCov();
        numTimeCov = eChartService.compareEvolution(eChartQuery);
        return R.ok().data("numTimeCov", numTimeCov).message("获取Echart-Evolution结果成功");
    }
}
