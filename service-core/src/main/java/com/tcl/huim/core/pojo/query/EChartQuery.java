package com.tcl.huim.core.pojo.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description="EChart查询对象")
public class EChartQuery {

    @ApiModelProperty(value = "数据集")
    private String dataSet;

    @ApiModelProperty(value = "最小长度")
    private Integer minLength;

    @ApiModelProperty(value = "最大长度")
    private Integer maxLength;

    @ApiModelProperty(value = "minUtil1")
    private Long minUtil1;

    @ApiModelProperty(value = "minUtil2")
    private Long minUtil2;

    @ApiModelProperty(value = "minUtil3")
    private Long minUtil3;

    @ApiModelProperty(value = "minUtil4")
    private Long minUtil4;

    @ApiModelProperty(value = "minUtil5")
    private Long minUtil5;

    @ApiModelProperty(value = "收敛阈值")
    private Long convergenceMinUtil;
}
