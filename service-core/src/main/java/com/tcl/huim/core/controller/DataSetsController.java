package com.tcl.huim.core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcl.common.exception.BusinessException;
import com.tcl.common.result.R;
import com.tcl.common.result.ResponseEnum;
import com.tcl.huim.core.pojo.entity.DataSets;
import com.tcl.huim.core.service.DataSetsService;
import com.tcl.huim.core.util.FileUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@CrossOrigin
@RestController
@Api(tags = "数据集管理")
@RequestMapping("/admin/core/dataSets")
@Slf4j
public class DataSetsController {

    private String dataFile = "\\service-core\\src\\main\\java\\com\\tcl\\huim\\core\\data_set\\";

    @Resource
    private HttpServletResponse response;

    @Resource
    private HttpServletRequest request;

    @Resource
    private DataSetsService dataSetsService;

    @ApiOperation("项集列表")
    @GetMapping("/list")
    public R listAll() {
        List<DataSets> list = dataSetsService.list();
        return R.ok().data("list", list).message("获取列表成功");
    }

    @ApiOperation("获取数据集分页列表")
    @GetMapping("/list/{page}/{limit}")
    public R listPage(
            @ApiParam(value = "当前页码", required = true)
            @PathVariable Long page,

            @ApiParam(value = "每页记录数", required = true)
            @PathVariable Long limit) {

        Page<DataSets> pageParam = new Page<>(page, limit);
        IPage<DataSets> pageModel = dataSetsService.listPage(pageParam);
        return R.ok().data("pageModel", pageModel);
    }

    @ApiOperation("数据集的导入")
    @PostMapping("/import")
    public R dataSetImport(
            @ApiParam(value = "数据集文件", required = true)
            @RequestParam("file") MultipartFile file

//            @ApiParam(value = "描述")
//            @RequestParam("desc") String desc
    ) {
        try {
            String desc = "";
            // 获取根路径
            String dir = System.getProperty("user.dir");

            // 获取文件名
            String fileName = file.getOriginalFilename();
            // 不带txt的文件名
            fileName = fileName.substring(0, fileName.length() - 4);
            // 获取路径
            String path = dir + dataFile;

            // 写入Mysql，需要判断是否存在
            LambdaQueryWrapper<DataSets> queryWrapper = new LambdaQueryWrapper<>();
            // 因为这里filename是带了txt的
            log.info("filename=" + fileName);
            queryWrapper.eq(DataSets::getDataName, fileName);

            int count = dataSetsService.count(queryWrapper);
            if (count > 0) {
                return R.error().message("数据集" + fileName + "已存在");
            }

            // 后端保存文件
            try {
                File files = new File(path, fileName + ".txt");
                file.transferTo(files);
            } catch (Exception e) {
                throw new BusinessException(ResponseEnum.UPLOAD_LOCATION_ERROR, e);
            }

            // 计算项集数量，计算总记录，添加描述
            DataSets dataSets = calculateDataSet(fileName, desc);

            //  mysql保存数据
            dataSetsService.save(dataSets);
            return R.ok().message("数据集导入成功");

        } catch (Exception e) {
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR, e);
        }
    }

    @ApiOperation("数据集的计算")
    @PostMapping("/calculateValue")
    public DataSets calculateDataSet(String filename, String desc) throws IOException {
        // 获取路径，文件名重复会覆盖
        String dir = System.getProperty("user.dir");
        String filePath = dir + dataFile + filename + ".txt";

        System.out.println(filePath);
        FileInputStream fin = new FileInputStream(filePath);
        InputStreamReader reader = new InputStreamReader(fin);
        BufferedReader buffReader = new BufferedReader(reader);
        String data = "";
        DataSets dataSets = new DataSets();
        Long countTransaction = 0l;
        Set<String> items = new TreeSet<>();
        while ((data = buffReader.readLine()) != null) {
            String[] split = data.trim().split(":");
            String[] itemStr = split[0].split(" ");
            for (int i = 0; i < itemStr.length; i++) {
                items.add(itemStr[i]);
            }
            countTransaction++;
        }
        // 注意这里可能加了txt
        dataSets.setDataName(filename);
        dataSets.setItemNumber(items.size());
        dataSets.setTransactionNumber(countTransaction);
        dataSets.setDataDesc(desc);

        return dataSets;
    }

    @ApiOperation("更新数据描述")
    @PutMapping("/update")
    public R updateById(
            @ApiParam(value = "数据集对象", required = true)
            @RequestBody DataSets dataSets) {

        boolean result = dataSetsService.updateById(dataSets);
        if (result) {
            return R.ok().message("更新成功");
        } else {
            return R.error().message("更新失败");
        }
    }

    @ApiOperation(value = "根据id删除数据记录")
    @DeleteMapping("/remove/{id}")
    public R removeById(
            @ApiParam(value = "数据id", example = "100", required = true)
            @PathVariable Long id) {
        // 注意删除的时候，还要删除本地文件夹
        boolean result = dataSetsService.removeByIdAndFile(id);
        if (result) {
            return R.ok().message("删除成功");
        } else {
            return R.error().message("删除失败");
        }
    }

    @ApiOperation("根据id获取数据集信息")
    @GetMapping("/get/{id}")
    public R getById(
            @ApiParam(value = "数据id", required = true, example = "1")
            @PathVariable Long id) {

        DataSets dataSets = dataSetsService.getById(id);
        if (dataSets != null) {
            return R.ok().data("record", dataSets);
        } else {
            return R.error().message("数据获取失败");
        }
    }

    @ApiOperation("根据id下载数据集")
    @GetMapping("/downLoad/{id}")
    public R downloadFileHttpStream(
            @ApiParam(value = "数据id", required = true, example = "1")
            @PathVariable Long id) {
        // 获取文件名
        DataSets dataSet = dataSetsService.getById(id);
        // 文件本地位置
        String dir = System.getProperty("user.dir");
        String filePath = dir + dataFile + dataSet.getDataName() + ".txt";
        log.info("filthPath="+filePath);
        // 文件名称
        String fileName = dataSet.getDataName()+".txt";
        log.info("fileName="+fileName);
        File file = new File(filePath);
        FileUtil.downloadFile(file, request, response, fileName);
        // 浏览器访问：http://x.x.x.x/test/down/file

        return R.ok().message("文件下载成功");
    }
}
