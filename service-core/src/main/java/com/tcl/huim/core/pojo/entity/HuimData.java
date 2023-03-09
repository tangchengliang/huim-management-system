package com.tcl.huim.core.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author tcl
 * @since 2023-01-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="HuimData对象", description="")
public class HuimData implements Serializable {

    private static final long serialVersionUID = 1L;

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

    @ApiModelProperty(value = "最小效用阈值")
    private Long minUtil;

    @ApiModelProperty(value = "项目")
    private String items;

    @ApiModelProperty(value = "效用值")
    private Long util;

    @ApiModelProperty(value = "0是未标记，1是标记")
    private Integer status;

    @ApiModelProperty(value = "创建时间", example = "2019-01-01 8:00:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2019-01-01 8:00:00")
    private LocalDateTime updateTime;
}
