package com.tcl.huim.core.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="数据集对象", description="用于挖掘的数据集")
public class DataSets {

    @ApiModelProperty(value = "编号")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "数据集")
    private String dataName;

    @ApiModelProperty(value = "项目数量")
    private Integer itemNumber;

    @ApiModelProperty(value = "事物数量")
    private Long transactionNumber;

    @ApiModelProperty(value = "描述")
    private String dataDesc;
}
