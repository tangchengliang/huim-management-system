package com.tcl.huim.core.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcl.common.result.R;
import com.tcl.huim.core.pojo.entity.HuimData;
import com.tcl.huim.core.pojo.query.HuimDataQuery;
import com.tcl.huim.core.service.HuimDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author tcl
 * @since 2023-01-06
 */
@CrossOrigin
@RestController
@Api(tags = "HUIM管理")
@RequestMapping("/admin/core/huimResult")
public class HuimDataController {

    @Resource
    private HuimDataService huimDataService;

    @ApiOperation("项集列表")
    @GetMapping("/list")
    public R listAll(){
        List<HuimData> list = huimDataService.list();
        return R.ok().data("list", list).message("获取列表成功");
    }

    @ApiOperation("获取项集分页列表")
    @GetMapping("/list/{page}/{limit}")
    public R listPage(
            @ApiParam(value = "当前页码", required = true)
            @PathVariable Long page,

            @ApiParam(value = "每页记录数", required = true)
            @PathVariable Long limit,

            @ApiParam(value = "查询对象", required = false)
                    HuimDataQuery huimDataQuery) {

        Page<HuimData> pageParam = new Page<>(page, limit);
        IPage<HuimData> pageModel = huimDataService.listPage(pageParam, huimDataQuery);
        return R.ok().data("pageModel", pageModel);
    }

    @ApiOperation("更新项集状态")
    @PutMapping("/update")
    public R updateById(
            @ApiParam(value = "项集信息", required = true)
            @RequestBody HuimData huimData){

        boolean result = huimDataService.updateById(huimData);
        if(result){
            return R.ok().message("更新成功");
        }else{
            return R.error().message("更新失败");
        }
    }

    @ApiOperation("是否标记")
    @PutMapping("/mark/{id}/{status}")
    public R mark(
            @ApiParam(value = "用户id", required = true)
            @PathVariable("id") Long id,

            @ApiParam(value = "标记状态（0：未标记 1：标记）", required = true)
            @PathVariable("status") Integer status){

        huimDataService.mark(id, status);
        return R.ok().message(status==1?"标记成功":"标记成功");
    }

//    @ApiOperation("根据参数添加项集")
//    @PostMapping("/save")
//    public R insertByTxt(String filePath) throws IOException {
//        FileInputStream fin = new FileInputStream(filePath);
//        InputStreamReader reader = new InputStreamReader(fin);
//        BufferedReader buffReader = new BufferedReader(reader);
//        String data = "";
//        List<HuimData> list = new LinkedList<>();
//        while((data = buffReader.readLine())!=null){
//            String[] split = data.trim().split("#");
//            HuimData huimData = new HuimData();
//            huimData.setDataSet("mushroom");
//            huimData.setAlgorithm("huim-lc-ba");
//            huimData.setMinLength(1);
//            huimData.setMaxLength(7);
//            huimData.setMinUtil(200000l);
//            StringJoiner sj = new StringJoiner(",");
//            for (String value : split[0].split(" ")) {
//                sj.add(value);
//            }
//            huimData.setItems(sj.toString());
//            huimData.setStatus(0);
//            huimData.setUtil(Long.parseLong(split[1].substring(6,split[1].length()-2)));
//            list.add(huimData);
//        }
//        buffReader.close();
//        boolean result = huimDataService.saveBatch(list);
//        if(result){
//            return R.ok().message("HUIs写入Mysql成功");
//        }else{
//            return R.error().message("HUIs写入Mysql失败");
//        }
//    }
}

