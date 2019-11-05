package com.uca.tools;

public class Tools {

    public static int linearSearch(String[] array, String str){
        for (int i=0; i<array.length; i++){
            if (array[i].equals(str)){
                return i;
            }
        }
        return -1;
    }

    public static int binarySearch(String[] array, String str){
        int left = 0;
        int right = array.length-1;
        while (left <= right){
            int midpoint = (left+right)/2;
            if (array[midpoint].equals(str)){
                return midpoint;
            }
            if (array[midpoint].compareTo(str) < 0){
                left = midpoint+1;
            }else{
                right = midpoint-1;
            }
        }
        return -1;
    }
}
