package com.tcl.huim.core.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="挖掘结果对象", description="用于展示挖掘结果")
public class EChart {

    @ApiModelProperty(value = "编号")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "数据集")
    private String dataSet;

    @ApiModelProperty(value = "算法")
    private String algorithm;

    @ApiModelProperty(value = "最小长度")
    private Integer minLength;

    @ApiModelProperty(value = "最大长度")
    private Integer maxLength;

    @ApiModelProperty(value = "HUI的数量")
    private Integer huiNumber;

    @ApiModelProperty(value = "运行时间")
    private Integer runtime;

    @ApiModelProperty(value = "最小效用值")
    private Integer minUtil;

    @ApiModelProperty(value = "算法收敛次数")
    private String iterHui;
}
