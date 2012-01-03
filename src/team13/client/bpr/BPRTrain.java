package team13.client.bpr;
import java.util.*;

public class BPRTrain extends BPR {
		
	List<List<String>> tuples = new ArrayList<List<String>>();
	
	void addEntity(String entity,Integer type){
		
		if(!type_entities.containsKey(type)){
			type_entities.put(type,new ArrayList<String>());
		}

		entity_types.put(entity,type);
		type_entities.get(type).add(entity);
		Vector noise = Vector.rand(K).sub( Vector.ones(K).mul(0.5) ).mul(NOISE_LEVEL);
		features.put(entity, Vector.zeros(K).add(noise));
	}

	void addData(List<String> tuple){
		
		tuples.add(tuple);
	}
	
	double predErr( List<String> tuple ){
		
		Vector leadFea = features.get(tuple.get(0));
		
		Vector[] negFea = new Vector[tuple.size()-1];
		Vector[] posFea = new Vector[tuple.size()-1];
		
		String pos,neg;
		int type;
		for(int i=1;i<tuple.size();i++){
			
			pos = tuple.get(i);
			type = entity_types.get(pos);
			neg = drawNegative(tuple,type);
			posFea[i-1] = features.get(pos);
			negFea[i-1] = features.get(neg);
		}
		
		double sum=0.0;
		double score_diff;
		for(int i=0;i<posFea.length;i++){
			score_diff = posFea[i].dot(leadFea) - negFea[i].dot(leadFea);
			sum += 1-logistic(score_diff);
		}

		return sum/posFea.length;
	}

	//update with tuple[0] as  leading entity
	void updateFeature( List<String> tuple ){
		
		Vector leadFea = features.get(tuple.get(0));
		
		Vector[] negFea = new Vector[tuple.size()-1];
		Vector[] posFea = new Vector[tuple.size()-1];
		
		//System.out.println(tuple.get(0));
		String pos,neg;
		int type;
		for(int i=1;i<tuple.size();i++){
			
			pos = tuple.get(i);
			type = entity_types.get(pos);
			neg = drawNegative(tuple,type);
			//System.out.println(i+":("+pos+","+neg+")");
			posFea[i-1] = features.get(pos);
			negFea[i-1] = features.get(neg);
		}
		
		double[] predErr = new double[tuple.size()-1];
		double score_diff;
		for(int i=0;i<posFea.length;i++){
			score_diff = posFea[i].dot(leadFea) - negFea[i].dot(leadFea);
			predErr[i] = 1-logistic(score_diff);
		}
		
		Vector[] posGrad = new Vector[tuple.size()-1];
		Vector[] negGrad = new Vector[tuple.size()-1];
		Vector	leadGrad ;
		
		for(int i=0;i<posGrad.length;i++){
			leadGrad =  posFea[i].sub(negFea[i]).mul(predErr[i]);
		        leadGrad = leadGrad.sub(leadFea.mul(LAMBDA));
			leadFea = leadFea.add(leadGrad.mul(LEARN_RATE));
		}

		for(int i=0;i<posGrad.length;i++){
			
			posGrad[i] = leadFea.mul(predErr[i]);
			posGrad[i] = posGrad[i].sub( posFea[i].mul(LAMBDA) );
			posFea[i] = posFea[i].add(posGrad[i].mul(LEARN_RATE));
			
			negGrad[i] = leadFea.mul(-predErr[i]);
			negGrad[i] = negGrad[i].sub( posFea[i].mul(LAMBDA) );
			negFea[i] = negFea[i].add( negGrad[i].mul(LEARN_RATE) );
		}
		
		features.put(tuple.get(0),leadFea);
		for(int i=1;i<tuple.size();i++)
			features.put(tuple.get(i),posFea[i-1]);
	}
	
	public void trainUniform(int iter){
		
		int r;
		for(int i=0;i<iter;i++){
			r = ran.nextInt(tuples.size());
			updateFeature(tuples.get(r));
		}
	}
	
	final static int NUM_SAMPLES=100;
	public double sampleTrainErr(){
		
		int r;
		double sum=0.0;
		for(int i=0;i<NUM_SAMPLES;i++){
			r = ran.nextInt(tuples.size());
			sum += predErr(tuples.get(r));
		}
		return sum/NUM_SAMPLES;
	}

	/*
	//問題，posSum和negSum should be weighted by prediction error
	void updateFeature2( List<String> tuple ){
		
		Integer type;
		String pos,neg;
		Vector[] negFea = new Vector[tuple.size()];
		Vector[] posFea = new Vector[tuple.size()];
		for(int i=0;i<tuple.size();i++){
			
			pos = tuple.get(i);
			type = entity_types.get(pos);
			neg = drawNegative(tuple,type);
			
			posFea[i] = features.get(pos);
			negFea[i] = features.get(neg);
		}
		
		Vector posSum = Vector.zeros(K);
		for(Vector v: posFea)
			posSum = posSum.add( v );
		Vector negSum = Vector.zeros(K);
		for(Vector v: negFea)
			negSum = negSum.add( v );
		
		Vector[] posGrad = new Vector[tuple.size()];
		for(int i=0;i<posGrad.length;i++)
			posGrad[i] = ( posSum.sub(posFea[i]).mul(2) ).sub( negSum.sub(negFea[i]) )
					.sub( posFea[i].mul(LAMBDA) );
		
		for(int i=0;i<posFea.length;i++)
			posFea[i] = posFea[i].add( posGrad[i].mul(LEARN_RATE) );

		Vector[] negGrad = new Vector[tuple.size()];
		for(int i=0;i<negGrad.length;i++)
			negGrad[i] = posSum.sub(posFea[i]).mul(-1)
					.sub( negFea[i].mul(LAMBDA) );

		for(int i=0;i<negFea.length;i++)
			negFea[i] = negFea[i].add( negGrad[i].mul(LEARN_RATE) );
		
	}
	*/
}
