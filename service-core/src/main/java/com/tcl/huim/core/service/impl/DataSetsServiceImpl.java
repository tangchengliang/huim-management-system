package com.tcl.huim.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcl.huim.core.mapper.DataSetsMapper;
import com.tcl.huim.core.pojo.entity.DataSets;
import com.tcl.huim.core.service.DataSetsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
@Slf4j
@Service
public class DataSetsServiceImpl extends ServiceImpl<DataSetsMapper, DataSets> implements DataSetsService {

    private String dataFile = "\\service-core\\src\\main\\java\\com\\tcl\\huim\\core\\data_set\\";


    @Override
    public IPage<DataSets> listPage(Page<DataSets> pageParam) {
        return baseMapper.selectPage(pageParam, null);
    }

    @Override
    public boolean removeByIdAndFile(Long id) {
        // 获取数据
        DataSets dataSets = this.getById(id);

        // 获取文件名
        String fileName = dataSets.getDataName();

        // 数据库删除
        boolean result = this.removeById(id);
        // 删除本地目路
        // 获取根路径
        String dir = System.getProperty("user.dir");
        String path = dir+dataFile + fileName+".txt";
        log.info("path="+path);
        try {
            Files.deleteIfExists(
                    Paths.get(path));
        }
        catch (NoSuchFileException e) {
            log.info("No such file/directory exists");
            System.out.println("No such file/directory exists");
        }
        catch (DirectoryNotEmptyException e) {
            log.info("Directory is not empty");
            System.out.println("Directory is not empty.");
        }
        catch (IOException e) {
            log.info("Invalid permissions.");
            System.out.println("Invalid permissions.");
        }
        return result;
    }
}
