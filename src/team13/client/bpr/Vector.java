package team13.client.bpr;

import java.util.*;

class Vector{
	
	double[] vect ;
	static Random ran = new Random();

	public Vector(int k){
		vect = new double[k];
	}

	public Vector(double[] v){
		vect = v;
	}
	
	public String toString(){
		
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("[");
		for(int i=0;i<vect.length;i++){
			sbuf.append(vect[i]+",");
		}
		sbuf.append("]");

		return sbuf.toString();
	}

	public static Vector zeros(int k){

		double[] v = new double[k];
		for(int i=0;i<k;i++)
			v[i] = 0;

		return new Vector(v);
	}

	public static Vector ones(int k){

		double[] v = new double[k];
		for(int i=0;i<k;i++)
			v[i] = 1;

		return new Vector(v);
	}

	public static Vector rand(int k){
		
		double[] v = new double[k];
		for(int i=0;i<k;i++)
			v[i] = ran.nextDouble();

		return new Vector(v);
	}
	
	public Vector add(Vector other){
		
		double[] v = vect;
		double[] v2 = other.vect;
		double[] v3 = new double[v.length];
		for(int i=0;i<v.length;i++)
			v3[i] = v[i] + v2[i];

		return new Vector(v3);
	}
	
	public Vector sub(Vector other){
		
		double[] v = vect;
		double[] v2 = other.vect;
		double[] v3 = new double[v.length];
		for(int i=0;i<v.length;i++)
			v3[i] = v[i] - v2[i];

		return new Vector(v3);
	}

	public Vector mul(double c){
		
		double[] v2 = new double[vect.length];
		for(int i=0;i<v2.length;i++)
			v2[i] = vect[i]*c;
		return new Vector(v2);
	}
	
	public double dot(Vector other){

		double sum=0;
		double[] v = vect;
		double[] v2 = other.vect;
		for(int i=0;i<v.length;i++)
			sum += v[i]*v2[i];
		
		return sum;
	}

	public double cos_sim(Vector v2){
		
		double L1 = this.dot(this);
		double L2 = v2.dot(v2);
		double product = this.dot(v2);

		return product/L1/L2;
	}
}
