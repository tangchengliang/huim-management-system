package com.tcl.huim.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcl.huim.core.mapper.HuimDataMapper;
import com.tcl.huim.core.pojo.entity.HuimData;
import com.tcl.huim.core.pojo.query.HuimDataQuery;
import com.tcl.huim.core.service.HuimDataService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author tcl
 * @since 2023-01-06
 */
@Service
public class HuimDataServiceImpl extends ServiceImpl<HuimDataMapper, HuimData> implements HuimDataService {

    @Override
    public IPage<HuimData> listPage(Page<HuimData> pageParam, HuimDataQuery huimDataQuery) {
        String dataSet = huimDataQuery.getDataSet();
        Integer status = huimDataQuery.getStatus();
        String algorithm = huimDataQuery.getAlgorithm();
        Integer minLength = huimDataQuery.getMinLength();
        Integer maxLength = huimDataQuery.getMaxLength();
        Long minUtil = huimDataQuery.getMinUtil();

        QueryWrapper<HuimData> huimDataQueryWrapper = new QueryWrapper<>();

        if(huimDataQuery == null){
            return baseMapper.selectPage(pageParam, null);
        }

        huimDataQueryWrapper
                .eq(StringUtils.isNotBlank(dataSet), "data_set", dataSet)
                .eq(StringUtils.isNotBlank(algorithm), "algorithm", algorithm)
                .eq(status != null, "status", huimDataQuery.getStatus())
                .eq(minLength != null, "min_length", minLength)
                .eq(maxLength != null, "max_length", maxLength)
                .eq(minUtil != null, "min_util", minUtil);
        return baseMapper.selectPage(pageParam, huimDataQueryWrapper);
    }

    @Override
    public void mark(Long id, Integer status) {
//        UserInfo userInfo = new UserInfo();
//        userInfo.setId(id);
//        userInfo.setStatus(status);
//        baseMapper.updateById(userInfo);
        HuimData huimData = new HuimData();
        huimData.setId(id);
        huimData.setStatus(status);
        baseMapper.updateById(huimData);
    }
}
