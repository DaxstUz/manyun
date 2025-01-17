package com.ch.mhy.util;

import android.widget.ProgressBar;

/**
 * 进度条更新线程
 * @author wenli.liu
 * edit by xc.li
 * @date 2015年7月30日
 */
public class ProgressBarThread extends Thread {
	private ProgressBar progressBar;
	private boolean isRunning = true;//是否是销毁
	private boolean isWaiting = false;//是否等待中
	
	public ProgressBarThread(ProgressBar progressBar){
		this.progressBar = progressBar;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	public boolean isWaiting() {
		return isWaiting;
	}
	public void setWaiting(boolean isWaiting) {
		this.isWaiting = isWaiting;
	}
	@Override
	public void run() {
		while(isRunning){
			
			while(isWaiting&&!this.isInterrupted()){
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			int max = progressBar.getMax();
			int progress = progressBar.getProgress();
			while(progressBar != null && progress<=max){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				progressBar.incrementProgressBy(1);
				progress += 1;
				if(progressBar.getProgress()==max){
					progressBar.setProgress(0);
				}
			}
		}
	}


}
