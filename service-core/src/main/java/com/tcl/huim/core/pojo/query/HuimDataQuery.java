package com.tcl.huim.core.pojo.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description="项集搜索对象")
public class HuimDataQuery {

    @ApiModelProperty(value = "数据集")
    private String dataSet;

    @ApiModelProperty(value = "算法")
    private String algorithm;

    @ApiModelProperty(value = "最小长度")
    private Integer minLength;

    @ApiModelProperty(value = "最大长度")
    private Integer maxLength;

    @ApiModelProperty(value = "最小效用阈值")
    private Long minUtil;

    @ApiModelProperty(value = "0是未标记，1是标记")
    private Integer status;

}
