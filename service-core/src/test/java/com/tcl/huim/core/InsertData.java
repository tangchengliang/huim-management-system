package com.tcl.huim.core;

import com.tcl.huim.core.pojo.entity.DataSets;
import com.tcl.huim.core.pojo.entity.HuimData;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class InsertData {

    private String dataFile = "\\src\\main\\java\\com\\tcl\\huim\\core\\data_set\\";


    @Test
    public void insertByTxt() throws IOException {
        String filePath = "D:/IdeaProject1/huim/service-core/src/main/java/com/tcl/huim/core/huis/mushroom.txt";
        FileInputStream fin = new FileInputStream(filePath);
        InputStreamReader reader = new InputStreamReader(fin);
        BufferedReader buffReader = new BufferedReader(reader);
        String data = "";
        List<HuimData> list = new LinkedList<>();
        while((data = buffReader.readLine())!=null){
            String[] split = data.trim().split("#");
            HuimData huimData = new HuimData();
            huimData.setAlgorithm("mushroom");
            huimData.setMinLength(1);
            huimData.setMaxLength(7);
            huimData.setMinUtil(200000l);
            StringJoiner sj = new StringJoiner(",");
            for (String value : split[0].split(" ")) {
                sj.add(value);
            }
            huimData.setItems(sj.toString());
            huimData.setUtil(Long.parseLong(split[1].substring(6,split[1].length()-2)));
            huimData.setStatus(0);
            list.add(huimData);
        }
        buffReader.close();
        for(HuimData data1:list){
            System.out.println(data1);
        }
    }

    @Test
    public void getDir(){
        String input = System.getProperty("user.dir") + "\\src\\java";
        System.out.println(input);
    }

    @Test
    public void calculateDataSet() throws IOException {
        String filename = "easyTest";
        String desc = "hello";
        // 获取路径，文件名重复会覆盖
        String dir = System.getProperty("user.dir");
        String filePath = dir+dataFile+filename+".txt";

        System.out.println(filePath);
        FileInputStream fin = new FileInputStream(filePath);
        InputStreamReader reader = new InputStreamReader(fin);
        BufferedReader buffReader = new BufferedReader(reader);
        String data = "";
        DataSets dataSets = new DataSets();
        Long countTransaction = 0l;
        Set<String> items = new TreeSet<>();
        while((data = buffReader.readLine())!=null) {
            String[] split = data.trim().split(":");
            String[] itemStr = split[0].split(" ");
            for (int i = 0; i < itemStr.length; i++) {
                items.add(itemStr[i]);
            }
            countTransaction++;
        }
        dataSets.setDataName(filename);
        dataSets.setItemNumber(items.size());
        dataSets.setTransactionNumber(countTransaction);
        dataSets.setDataDesc(desc);
    }
}
