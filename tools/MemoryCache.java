package com.sky.redhome;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import android.graphics.Bitmap;
import android.text.Spanned;
import android.util.Log;

public class MemoryCache {

    private static final String TAG = "SpannedMemoryCache";
    private Map<String, Spanned> cache=Collections.synchronizedMap(
            new LinkedHashMap<String, Spanned>(10,1.5f,true));//Last argument true for LRU ordering
    private long size=0;//current allocated size
    private long limit=1000000;//max memory in bytes

    public MemoryCache(){
        //use 25% of available heap size
        setLimit(Runtime.getRuntime().maxMemory()/4);
    }
    
    public void setLimit(long new_limit){
        limit=new_limit;
        Log.i(TAG, "MemoryCache will use up to "+limit/1024./1024.+"MB");
    }

    public Spanned get(String id){
        try{
            if(!cache.containsKey(id))
                return null;
            //NullPointerException sometimes happen here http://code.google.com/p/osmdroid/issues/detail?id=78 
            return cache.get(id);
        }catch(NullPointerException ex){
            ex.printStackTrace();
            return null;
        }
    }

    public void put(String id, Spanned sp){
        try{
            cache.put(id, sp);
        }catch(Throwable th){
            th.printStackTrace();
        }
    }
    
    public void clear() {
        try{
            //NullPointerException sometimes happen here http://code.google.com/p/osmdroid/issues/detail?id=78 
            cache.clear();
        }catch(NullPointerException ex){
            ex.printStackTrace();
        }
    }

}