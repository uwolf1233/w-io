package com.wolf.test;

public class ObjectsTest {

	public Object[] os = new Object[100];
	public int count = 0;
	
	public static void main(String[] args) {
		new ObjectsTest().init();
	}
	
	public void init(){
		for(int i=1;i<20;i++){
			add(i+"");
		}
		add(2,"1000");
		remove("8");
		remove(5);
		add(10,"2000");
		remove("2000");
		Object o = get(12);
		System.out.println(123);
	}
	
	public boolean add(String s){
		if(count == os.length){
			return false;
		}
		os[count] = s;
		count++;
		return true;
	}
	
	public boolean add(int index,String s){
		if(count == 0){
			os[0] = s;
		}else if(count == os.length){
			return false;
		}
		else{
			for(int i=count;i>=index;i--){
				os[i+1] = os[i];
			}
			os[index] = s;
		}
		count++;
		return true;
	}
	
	public boolean remove(Object o){
		if(count == 0){
			return false;
		}
		boolean ismove = false;
		for(int i=0;i<count;i++){
			Object cos = os[i];
			if(cos.equals(o) || cos == o){
				ismove = true;
				os[i] = null;
			}
			if(ismove && count < os.length){
				os[i] = os[i+1];//往前移一个
			}
		}
		if(count < os.length){
			os[count] = null;
		}
		count--;
		return ismove;
	}
	
	public boolean remove(int index){
		if(count == 0 || count < index){
			return false;
		}
		os[index] = null;
		for(int i=index;i<count;i++){
			os[i] = os[i+1];//往前移一个
			os[i+1] = null;
		}
		count--;
		return true;
	}
	
	public int size(){
		return count;
	}
	
	public Object get(int index){
		return os[index];
	}
	
}











