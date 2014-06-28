package com.example.test1001;

import java.io.Serializable;
import java.util.ArrayList;

public class TitleBean implements Serializable{
	private String bTitle;
	private ArrayList<String> data;
	private String command;
	
	TitleBean(){
	}
	
	
	public void setDataCollection(ArrayList<String> al){
		this.data = al;
	}
	
	public void setServerCommand(String str){
		this.command = str;
	}
}
