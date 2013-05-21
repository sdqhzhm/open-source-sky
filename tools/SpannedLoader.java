package com.sky.redhome;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.sky.redhome.lazylist.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

public class SpannedLoader {
	
	FileCache fileCache;
	MemoryCache memoryCache = new MemoryCache();
    private Map<TextView, String> textViews=Collections.synchronizedMap(new WeakHashMap<TextView, String>());
    //private Map<TextView, Spanned> caches=Collections.synchronizedMap(new WeakHashMap<TextView, Spanned>());
    ExecutorService executorService; 
    
    public SpannedLoader(Context c){
    	fileCache = new FileCache(c);
        executorService=Executors.newFixedThreadPool(5);        
    }
    
    final Spanned error_sp = Html.fromHtml("Error: final Spanned sp; sky!");
    

    public void DisplayMessage(TextView tv, String msg){
    	
    	textViews.put(tv, msg);
    	//稍后加入缓存功能
    	Spanned sp = memoryCache.get(msg);
    	if(sp != null){
    		tv.setText(sp);
    	}else {
    		queueMsg(msg, tv);
    		//默认加载内容
        	tv.setText(Html.fromHtml(msg));
    		//tv.setText(msg);
    	}
    }

    private void queueMsg(String msg, TextView tv){
    	MsgToLoad p=new MsgToLoad(msg, tv);
        executorService.submit(new MsgsLoader(p));
    }
    
    private class MsgToLoad {
    	public String msg;
    	public TextView textView;
    	public MsgToLoad(String m, TextView t) {
    		msg = m;
    		textView = t;
    	}
    }
    
    //获取Spanned中的图片
    private Spanned getSpanned(String msg) {
		try{
			Spanned sp = null;
			ImageGetter getter = new ImageGetter(){
				@Override
                public Drawable getDrawable(String source) {
					//即使不含有<img> 仍然会调用ImageGetter
					if(source == null)
						return null;
					
                	File f = fileCache.getFile(source);    
                	Drawable d2 = decodeFile(f);
                	if(d2!=null){         
                		Log.i("SpannedLoader", "drawable from file");
                		return d2;
                	}
                	
                    InputStream is = null;
                    try {
                    	Log.i("SpannedLoader", "drawable from web");
                    	
                        is = (InputStream) new URL(source).getContent();
                        
                        OutputStream os = new FileOutputStream(f);
                        Utils.CopyStream(is, os);
                        os.close();                        
                        Drawable d = decodeFile(f);
                        is.close();
                        return d;
                    } catch (Exception e) {
                        return null;
                    }
                }
			};
			
			sp = Html.fromHtml(msg, getter, null);
			return sp;
		}catch(Throwable e){
			e.printStackTrace();
			return null;
		}
    }
    
	private Drawable decodeFile(File f){
		Log.i("SpannedLoader","File: "+ f.getAbsolutePath());
    	try{
    		FileInputStream stream1=new FileInputStream(f);
    		
    		Drawable d = Drawable.createFromStream(stream1, "src");
            d.setBounds(0, 0, d.getIntrinsicWidth(),
                    d.getIntrinsicHeight());
            
            return d;
    	}catch(FileNotFoundException e){
    		
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    	return null;
    }
    
    class MsgsLoader implements Runnable {
    	MsgToLoad msgToLoad;
    	MsgsLoader(MsgToLoad msgToLoad){
    		this.msgToLoad = msgToLoad;
    	}
    	
		@Override
		public void run() {
			try {
				if(textViewReused(msgToLoad))
					return;
				Spanned sp = getSpanned(msgToLoad.msg);
				memoryCache.put(msgToLoad.msg, sp);
				if(textViewReused(msgToLoad))
					return;
				SpannedDisplayer sd = new SpannedDisplayer(sp, msgToLoad);
				//返回主线程修改message的关键
				Activity a = (Activity)msgToLoad.textView.getContext();
				a.runOnUiThread(sd);
			}catch(Throwable th){
				th.printStackTrace();
			}
			
		}
    	
    }
    
    //有疑问，待确定
    boolean textViewReused(MsgToLoad msgToLoad){
        String tag=textViews.get(msgToLoad.textView);
        if(tag==null || !tag.equals(msgToLoad.msg))
            return true;
        return false;
    }
    
    private class SpannedDisplayer implements Runnable {
    	Spanned sp;
    	MsgToLoad msgToLoad;
    	public SpannedDisplayer(Spanned s, MsgToLoad m){
    		sp = s;
    		msgToLoad = m;    		
    	}
    	public void run() {
    		if(textViewReused(msgToLoad))
                return;
            if(sp!=null){
            	msgToLoad.textView.setText(sp);
            	//TextView 内部点击实现
            	msgToLoad.textView.setMovementMethod(LinkMovementMethod.getInstance());
            }else{
            	Log.i("SpannedLoader", "error_sp, 不能成功获取spanned");
            	msgToLoad.textView.setText(error_sp);
            }
            	
    	}
    }
    

    
    public void clearCache() {
    	try{
    		memoryCache.clear();
    	}catch(NullPointerException e){
    		e.printStackTrace();
    	}
    	
    }
    
    public class FileCache {
        
        private File cacheDir;
        
        public FileCache(Context context){
            //Find the dir to save cached images
            if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
                cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"Redhome/smiley");
            else
                cacheDir=context.getCacheDir();
            if(!cacheDir.exists())
                cacheDir.mkdirs();
            
            Log.i("SpannedLoader", "FileCache Redhome/smiley");
        }
        
        public File getFile(String url){
            //I identify images by hashcode. Not a perfect solution, good for the demo.
            String filename=String.valueOf(url.hashCode());            
            //Another possible solution (thanks to grantland)
        	//String filename = url.substring(42);
            File f = new File(cacheDir, filename);
            return f;
            
        }
        
        public void clear(){
            File[] files=cacheDir.listFiles();
            if(files==null)
                return;
            for(File f:files)
                f.delete();
        }

    }
    
    public String ReData(String d){
     	String data = d;
     	String result = "";
     	String BASE_URL = "http://bbs.redhome.cc";    	
     	Document doc = Jsoup.parse(d);
     	
			// 原分析
			Elements imgs = doc.select("img");
			int imgSize = imgs.size();
			String[] id = new String[imgSize];
			String[] key = new String[imgSize];
			if(imgSize != 0){
				for(int i = 0; i < imgSize; i++){
					//判断附件图片还有表情，会对原信息修改
					if(imgs.get(i).hasClass("zoom"))
						Log.i("!!!!!!", "has class zoom");
					/*
					id[i] = imgs.get(i).attr("id").toString();
					//无用的img标签跳出
					if(id[i].equals("")){
						break;						
					}
					id[i] = id[i].substring(5);
					
					//BASE_URL根据网站返回Message的不同确定是否添加
					key[i] = imgs.get(i).attr("file").toString();
					String p[] = key[i].split("/");
					key[i] = p[p.length-1].substring(0, p[5].length()-4);
					result = "<div><p><img src=\""+BASE_URL+"forum.php?mod=image&aid=" +
							id[i] + 
							"&size=268x380&key=" +
							key[i] +
							"&type=fixnone\"></p></div>";				
					imgs.get(i).wrap(result);
					imgs.get(i).select("img").get(0).remove();
					*/
				}
			}

     	return doc.toString();
     }
}