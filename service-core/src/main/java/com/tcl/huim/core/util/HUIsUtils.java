package com.tcl.huim.core.util;

import com.tcl.huim.core.pojo.entity.HuimData;
import com.tcl.huim.core.pojo.query.CalculateQuery;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

public class HUIsUtils {

    //
    public static List<HuimData> insertByTxt(String filePath, CalculateQuery calculateQuery) throws IOException {
        FileInputStream fin = new FileInputStream(filePath);
        InputStreamReader reader = new InputStreamReader(fin);
        BufferedReader buffReader = new BufferedReader(reader);
        String data = "";
        List<HuimData> list = new LinkedList<>();
        while ((data = buffReader.readLine()) != null) {
            String[] split = data.trim().split("#");
            HuimData huimData = new HuimData();
            huimData.setDataSet(calculateQuery.getDataSet());
            huimData.setAlgorithm(calculateQuery.getAlgorithm());
            huimData.setMinLength(calculateQuery.getMinLength());
            huimData.setMaxLength(calculateQuery.getMaxLength());
            huimData.setMinUtil(calculateQuery.getMinUtil());
            StringJoiner sj = new StringJoiner(",");
            for (String value : split[0].split(" ")) {
                sj.add(value);
            }
            huimData.setItems(sj.toString());
            huimData.setStatus(0);
            huimData.setUtil(Long.parseLong(split[1].substring(6, split[1].length() - 2)));
            list.add(huimData);
        }
        buffReader.close();
        return list;
    }
}
