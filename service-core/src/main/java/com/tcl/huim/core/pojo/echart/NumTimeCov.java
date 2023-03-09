package com.tcl.huim.core.pojo.echart;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 封装echart：数量，时间，迭代次数的实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="Echart 结果对象-数量，时间，收敛", description="用于展示挖掘结果")
public class NumTimeCov {

    @ApiModelProperty(value = "数据集")
    private String dataSet;

    @ApiModelProperty(value = "2个算法-5个效用阈值-HUI的数量")
    private List<List<Integer>> huiNumber;

    @ApiModelProperty(value = "2个算法-5个效用阈值-运行时间")
    private List<List<Integer>> runtime;

    @ApiModelProperty(value = "5个最小效用值列表")
    // 注意是字符串,好像不是也可以
    private List<Long> minUtilArray;

    @ApiModelProperty(value = "算法收敛次数")
    private List<List<String>> convergence;
}
