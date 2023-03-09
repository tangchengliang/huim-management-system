package com.tcl.huim.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcl.common.exception.BusinessException;
import com.tcl.huim.core.mapper.EChartMapper;
import com.tcl.huim.core.pojo.echart.NumTimeCov;
import com.tcl.huim.core.pojo.entity.EChart;
import com.tcl.huim.core.pojo.query.EChartQuery;
import com.tcl.huim.core.service.EChartService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class EChartServiceImpl extends ServiceImpl<EChartMapper, EChart> implements EChartService {


    @Override
    public NumTimeCov compareList(EChartQuery eChartQuery) {
        // 数据集
        String dataSet = eChartQuery.getDataSet();
        // 长度约束
        Integer minLength = eChartQuery.getMinLength();
        Integer maxLength = eChartQuery.getMaxLength();
        // 收敛阈值
        Long convergenceMinUtil = eChartQuery.getConvergenceMinUtil();
        // 效用阈值
        Long minUtil1 = eChartQuery.getMinUtil1();
        Long minUtil2 = eChartQuery.getMinUtil2();
        Long minUtil3 = eChartQuery.getMinUtil3();
        Long minUtil4 = eChartQuery.getMinUtil4();
        Long minUtil5 = eChartQuery.getMinUtil5();

        // 根据条件封装Echart结果
        NumTimeCov numTimeCov = new NumTimeCov();
        numTimeCov.setDataSet(dataSet);
        // 封装效用列表
        List<Long> minUtilArray = new LinkedList<>();
        minUtilArray.add(minUtil1);
        minUtilArray.add(minUtil2);
        minUtilArray.add(minUtil3);
        minUtilArray.add(minUtil4);
        minUtilArray.add(minUtil5);

        numTimeCov.setMinUtilArray(minUtilArray);

        String[] algorithm = {"HUIM-LC-BA", "HUIM-LC-MPA"};
        // 查询并封装数量
        List<List<Integer>> huiNumber = new LinkedList<>();
        // 查询并封装时间
        List<List<Integer>> runtime = new LinkedList<>();
        // 查询并封装迭代次数
        List<List<String>> convergence = new LinkedList<>();
        for (int j = 0; j < algorithm.length; j++) {
            List<Integer> hui = new LinkedList<>();
            List<Integer> time = new LinkedList<>();
            List<String> cov = new LinkedList<>();
            for (int i = 0; i < 5; i++) {
                QueryWrapper<EChart> eChartQueryWrapper = new QueryWrapper<>();
                eChartQueryWrapper
                        .eq(StringUtils.isNotBlank(dataSet), "data_set", dataSet)
                        .eq(StringUtils.isNotBlank(algorithm[j]), "algorithm", algorithm[j])
                        .eq(minLength != null, "min_length", minLength)
                        .eq(maxLength != null, "max_length", maxLength)
                        .eq(minUtilArray.get(i) != null, "min_util", minUtilArray.get(i));
                List<EChart> list = this.list(eChartQueryWrapper);
                if(list.size()<=0){
                    //todo 说明没有数据，需要执行算法,需要抛出异常使用R来抛出
                    throw new BusinessException("{数据集="+dataSet+",算法="+algorithm[j]+",最大长度="+maxLength+",minUtil="+minUtilArray.get(i)+"没有数据，需要先执行算法！！！");
                }
                hui.add(list.get(0).getHuiNumber());
                time.add(list.get(0).getRuntime());
                if(convergenceMinUtil.equals(minUtilArray.get(i))) {
                    String[] coverHui = list.get(0).getIterHui().trim().split(",");
                    for (int k = 0; k < coverHui.length; k++) {
                        cov.add(coverHui[k]);
                    }
                }
            }
            huiNumber.add(hui);
            runtime.add(time);
            convergence.add(cov);
        }
        // 封装Echart结果
        numTimeCov.setHuiNumber(huiNumber);
        numTimeCov.setRuntime(runtime);
        numTimeCov.setConvergence(convergence);

        return numTimeCov;
    }

    @Override
    public NumTimeCov compareEvolution(EChartQuery eChartQuery) {
        // 数据集
        String dataSet = eChartQuery.getDataSet();

        // 收敛阈值
        Long convergenceMinUtil = eChartQuery.getConvergenceMinUtil();
        // 效用阈值
        Long minUtil1 = eChartQuery.getMinUtil1();
        Long minUtil2 = eChartQuery.getMinUtil2();
        Long minUtil3 = eChartQuery.getMinUtil3();
        Long minUtil4 = eChartQuery.getMinUtil4();
        Long minUtil5 = eChartQuery.getMinUtil5();

        // 根据条件封装Echart结果
        NumTimeCov numTimeCov = new NumTimeCov();
        numTimeCov.setDataSet(dataSet);
        // 封装效用列表
        List<Long> minUtilArray = new LinkedList<>();
        minUtilArray.add(minUtil1);
        minUtilArray.add(minUtil2);
        minUtilArray.add(minUtil3);
        minUtilArray.add(minUtil4);
        minUtilArray.add(minUtil5);

        numTimeCov.setMinUtilArray(minUtilArray);

        String[] algorithm = {"HUIM-BA","HUIM-PSO","HUIM-GA","HUIM-MPA"};
        // 查询并封装数量
        List<List<Integer>> huiNumber = new LinkedList<>();
        // 查询并封装时间
        List<List<Integer>> runtime = new LinkedList<>();
        // 查询并封装迭代次数
        List<List<String>> convergence = new LinkedList<>();
        for (int j = 0; j < algorithm.length; j++) {
            List<Integer> hui = new LinkedList<>();
            List<Integer> time = new LinkedList<>();
            List<String> cov = new LinkedList<>();
            for (int i = 0; i < 5; i++) {
                QueryWrapper<EChart> eChartQueryWrapper = new QueryWrapper<>();
                eChartQueryWrapper
                        .eq(StringUtils.isNotBlank(dataSet), "data_set", dataSet)
                        .eq(StringUtils.isNotBlank(algorithm[j]), "algorithm", algorithm[j])
                        .eq("min_length", 0)
                        .eq("max_length", 0)
                        .eq(minUtilArray.get(i) != null, "min_util", minUtilArray.get(i));
                List<EChart> list = this.list(eChartQueryWrapper);
                // 判断是否有数据
                if(list.size()<=0){
                    //todo 说明没有数据，需要执行算法,需要抛出异常使用R来抛出
                    throw new BusinessException(algorithm[j]+"没有数据，需要先执行算法！！！");
                }
                hui.add(list.get(0).getHuiNumber());
                time.add(list.get(0).getRuntime());
                if(convergenceMinUtil.equals(minUtilArray.get(i))) {
                    String[] coverHui = list.get(0).getIterHui().trim().split(",");
                    for (int k = 0; k < coverHui.length; k++) {
                        cov.add(coverHui[k]);
                    }
                }
            }
            huiNumber.add(hui);
            runtime.add(time);
            convergence.add(cov);
        }
        // 封装Echart结果
        numTimeCov.setHuiNumber(huiNumber);
        numTimeCov.setRuntime(runtime);
        numTimeCov.setConvergence(convergence);

        return numTimeCov;
    }
}
