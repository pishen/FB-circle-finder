import java.util.*;

public class BPRTrain extends BPR {
		
	List<List<String>> tuples = new ArrayList();
	
	void addEntity(String entity,Integer type){
		
		entity_types.put(entity,type);
		Vector noise = Vector.rand(K).sub( Vector.ones(K).mul(0.5) ).mul(NOISE_LEVEL);
		features.put(entity, Vector.zeros(K).add(noise));
	}

	void addData(List<String> tuple){
		
		tuples.add(tuple);
	}
	
	//大有問題，posSum和negSum should be weighted by prediction error
	void updateFeature( List<String> tuple ){
		
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
}
