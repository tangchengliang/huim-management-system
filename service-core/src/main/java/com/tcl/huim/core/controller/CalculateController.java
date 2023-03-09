package com.tcl.huim.core.controller;

import com.tcl.common.result.R;
import com.tcl.huim.core.algorithm.HUIM_LC_BA;
import com.tcl.huim.core.pojo.entity.HuimData;
import com.tcl.huim.core.pojo.query.CalculateQuery;
import com.tcl.huim.core.service.HuimDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

import static com.tcl.huim.core.util.HUIsUtils.insertByTxt;

@CrossOrigin
@RestController
@Api(tags = "执行挖掘")
@RequestMapping("/admin/core/calculate")
public class CalculateController {

    private String dataFile = "\\service-core\\src\\main\\java\\com\\tcl\\huim\\core\\data_set\\";
    private String outFile = "\\service-core\\src\\main\\java\\com\\tcl\\huim\\core\\huis\\";

    @Resource
    private HuimDataService huimDataService;

    @ApiOperation("根据参数挖掘高效用项集")
    @PostMapping("/save")
    public R save(
            @ApiParam(value = "挖掘参数——算法、数据集、长度约束等", required = true)
            @RequestBody CalculateQuery calculateQuery

    ) throws IOException {
//            String input = "D:/IdeaProject1/huim/service-core/src/main/java/com/tcl/huim/core/controller/easyTest.txt";
        String input = System.getProperty("user.dir") + dataFile + calculateQuery.getDataSet() + ".txt";
        String output = System.getProperty("user.dir") + outFile + calculateQuery.getDataSet() + ".txt";

        //todo 待完善所有算法 执行算法
        HUIM_LC_BA lcBa = new HUIM_LC_BA();
        lcBa.runAlgorithm(input, output, calculateQuery.getMinUtil().intValue(), calculateQuery.getMinLength(), calculateQuery.getMaxLength());
        lcBa.printStats();

        List<HuimData> huimData = insertByTxt(output, calculateQuery);

        // todo 保存了HUIM结果
        boolean result = huimDataService.saveBatch(huimData);
        // todo 还需要保存Echart结果，写进一个service里，并加入事物

        if(result){
            return R.ok().message("HUIs挖掘成功");
        }else{
            return R.error().message("HUis挖掘失败");
        }

//        if(algorithm.equals("HUIM-LC-BA")){
//            String input = fileToPath(dataSet+".txt");
//
//            String output = "output.txt";
//
//            HUIM_LC_BA lcBa = new HUIM_LC_BA();
//            lcBa.runAlgorithm(input, output, minUtil,minLength,maxLength);
//            lcBa.printStats();
//
//            return R.ok().message("算法执行成功");
//        }

//        return R.error();

    }


//    public static String fileToPath(String filename) throws UnsupportedEncodingException {
//        URL url = HUIM_LC_BA.class.getResource(filename);
//        return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
//    }
}
