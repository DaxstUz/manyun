package com.ch.mhy.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.os.Build.VERSION;
import android.os.Environment;

/**
 * 读取sd卡帮助类
 * 
 * @author DaxstUz 2416738717 2015年8月13日
 *
 */
public class SDUtil {

	// 获取外置存储卡的根路径，如果没有外置存储卡，则返回null
	public static String getPath2() {
		String sdcard_path = null;
		String sd_default = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		if (sd_default.endsWith("/")) {
			sd_default = sd_default.substring(0, sd_default.length() - 1);
		}
		// 得到路径
		try {
			Runtime runtime = Runtime.getRuntime();
			Process proc = runtime.exec("mount");
			InputStream is = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			String line;
			BufferedReader br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				if (line.contains("secure"))
					continue;
				if (line.contains("asec"))
					continue;
				if (line.contains("fat") && line.contains("/mnt/")) {
					String columns[] = line.split(" ");
					if (columns != null && columns.length > 1) {
						if (sd_default.trim().equals(columns[1].trim())) {
							continue;
						}
						sdcard_path = columns[1];
					}
				} else if (line.contains("fuse") && line.contains("/mnt/")) {
					String columns[] = line.split(" ");
					if (columns != null && columns.length > 1) {
						if (sd_default.trim().equals(columns[1].trim())) {
							continue;
						}
						sdcard_path = columns[1];
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sdcard_path;
	}
	
	// 返回值不带File seperater "/",如果没有外置第二个sd卡,返回第一个sd卡  
    public static String getSecondExterPath() {  
    	int sysVersion = Integer.parseInt(VERSION.SDK); 
    	if(sysVersion>18){
    		return getFirstExterPath(); 
    	}
    	
        List<String> paths = getExtSDCardPath();  
//        List<String> paths = getAllExterSdcardPath();  
  
        if (paths.size() == 2) {  
  
            for (String path : paths) {  
                if (path != null && !path.equals(getFirstExterPath())) {  
                    return path;  
                }  
            }  
  
            return getFirstExterPath();  
  
        } else {  
            return getFirstExterPath();  
        }  
    }  
  
    public static boolean isFirstSdcardMounted(){  
        if (!Environment.getExternalStorageState().equals(  
                Environment.MEDIA_MOUNTED)) {  
            return false;  
        }  
        return true;  
    }  
      
    public static boolean isSecondSDcardMounted() {  
        String sd2 = getSecondExterPath();  
        if (sd2 == null) {  
            return false;  
        }  
        return checkFsWritable(sd2 + File.separator);  
  
    }  
  
    // 测试外置sd卡是否卸载，不能直接判断外置sd卡是否为null，因为当外置sd卡拔出时，仍然能得到外置sd卡路径。我这种方法是按照android谷歌测试DICM的方法，  
    // 创建一个文件，然后立即删除，看是否卸载外置sd卡  
    // 注意这里有一个小bug，即使外置sd卡没有卸载，但是存储空间不够大，或者文件数已至最大数，此时，也不能创建新文件。此时，统一提示用户清理sd卡吧  
    private static boolean checkFsWritable(String dir) {  
  
        if (dir == null)  
            return false;  
  
        File directory = new File(dir);  
  
        if (!directory.isDirectory()) {  
            if (!directory.mkdirs()) {  
                return false;  
            }  
        }  
  
        File f = new File(directory, ".keysharetestgzc");  
        try {  
            if (f.exists()) {  
                f.delete();  
            }  
            if (!f.createNewFile()) {  
                return false;  
            }  
            f.delete();  
            return true;  
  
        } catch (Exception e) {  
        }  
        return false;  
  
    }  
  
    public static String getFirstExterPath() {  
        return Environment.getExternalStorageDirectory().getPath();  
    }  
  
    public static List<String> getAllExterSdcardPath() {  
        List<String> SdList = new ArrayList<String>();  
  
        String firstPath = getFirstExterPath();  
  
        // 得到路径  
        try {  
            Runtime runtime = Runtime.getRuntime();  
            Process proc = runtime.exec("mount");  
            InputStream is = proc.getInputStream();  
            InputStreamReader isr = new InputStreamReader(is);  
            String line;  
            BufferedReader br = new BufferedReader(isr);  
            while ((line = br.readLine()) != null) {  
                // 将常见的linux分区过滤掉  
                if (line.contains("secure"))  
                    continue;  
                if (line.contains("asec"))  
                    continue;  
                if (line.contains("media"))  
                    continue;  
                if (line.contains("system") || line.contains("cache")  
                        || line.contains("sys") || line.contains("data")  
                        || line.contains("tmpfs") || line.contains("shell")  
                        || line.contains("root") || line.contains("acct")  
                        || line.contains("proc") || line.contains("misc")  
                        || line.contains("obb")) {  
                    continue;  
                }  
  
                if (line.contains("fat") || line.contains("fuse") || (line  
                        .contains("ntfs"))) {  
                      
                    String columns[] = line.split(" ");  
                    if (columns != null && columns.length > 1) {  
                        String path = columns[1];  
                        if (path!=null&&!SdList.contains(path)&&path.contains("sd"))  
                            SdList.add(columns[1]);  
                    }  
                }  
            }  
        } catch (Exception e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }  
  
        if (!SdList.contains(firstPath)) {  
            SdList.add(firstPath);  
        }  
  
        return SdList;  
    }  
    
    
    /** 
     * 获取内置SD卡路径 
     * @return 
     */  
    public String getInnerSDCardPath() {    
        return Environment.getExternalStorageDirectory().getPath();    
    } 
    
    
    /** 
     * 获取外置SD卡路径 
     * @return  应该就一条记录或空 
     */  
    public static List<String> getExtSDCardPath()  
    {  
        List<String> lResult = new ArrayList<String>();  
        try {  
            Runtime rt = Runtime.getRuntime();  
            Process proc = rt.exec("mount");  
            InputStream is = proc.getInputStream();  
            InputStreamReader isr = new InputStreamReader(is);  
            BufferedReader br = new BufferedReader(isr);  
            String line;  
            while ((line = br.readLine()) != null) {  
                if (line.contains("extSdCard"))  
                {  
                    String [] arr = line.split(" ");  
                    String path = arr[1];  
                    File file = new File(path);  
                    if (file.isDirectory())  
                    {  
                        lResult.add(path);  
                    }  
                }  
            }  
            isr.close();  
        } catch (Exception e) {  
        }  
        return lResult;  
    } 
    
}
