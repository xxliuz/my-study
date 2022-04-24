package com.example.test.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @Author: zhou.liu
 * @Date: 2022/4/24 13:46
 * @Description:
 */
public class Collections {
    public static void main(String[] args) {
        HashMap<Integer,String> map = new HashMap<>();
        ArrayList<String> arrayList = new ArrayList<>();
        LinkedList<String> linkedList = new LinkedList<>();

        long startTime,endTime;

        startTime = System.nanoTime();
        for(int i=0;i<10000;i++){
            map.put(i,"this is" +i);
        }
        endTime =System.nanoTime();
        System.out.println("HashMap :" +(endTime-startTime));


        startTime = System.nanoTime();
        for(int i=0;i<10000;i++){
            arrayList.add("this is" +i);
        }
        endTime =System.nanoTime();
        System.out.println("ArrayList :" +(endTime-startTime));

        startTime = System.nanoTime();
        for(int i=0;i<10000;i++){
            linkedList.add("this is" +i);
        }
        endTime =System.nanoTime();
        System.out.println("LinkedList :" +(endTime-startTime));
    }
}
