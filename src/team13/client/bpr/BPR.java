package team13.client.bpr;
import java.util.*;

class BPR{
	
	final static int K=20;
	final static double NOISE_LEVEL=1e-4;
	final static double LEARN_RATE=0.01;
	final static double LAMBDA=1e-6;
	static Random ran = new Random();
	
	Map<String,Vector> features = new HashMap<String,Vector>();
	Map<String,Integer> entity_types = new HashMap<String,Integer>();
	Map<Integer,List<String>> type_entities = new HashMap<Integer,List<String>>();
	
	public BPR(){
	}
	
	public BPR(BPR bpr){
		this();
		addAll(bpr);
	}
	
	public boolean containsEntity(String e){
		return entity_types.containsKey(e);
	}

	public void addAll(BPR bpr){
		
		features.putAll(bpr.features);
		entity_types.putAll(bpr.entity_types);
		for(Integer type: bpr.type_entities.keySet()){
			Set<String> set = new HashSet<String>();
			if(type_entities.containsKey(type))set.addAll(type_entities.get(type));
			set.addAll(bpr.type_entities.get(type));
			type_entities.put(type,new ArrayList<String>(set));
		}
	}

	double logistic(double score){
		
		return 1.0/(1+Math.exp(-score));
	}
	
	String drawNegative( Collection<String> tuple, Integer type){ 
		
		String neg_example;
		List<String> list;
		do{
			list = type_entities.get(type); 
			neg_example = list.get( ran.nextInt(list.size()) );
			
		}while( tuple.contains(neg_example) );
		
		return neg_example;
	}
	
	int iteration=100;
	Vector cooccurFea( Collection<String> occur ){
		
		Vector[] posFeas = new Vector[occur.size()];
		Integer[] types = new Integer[occur.size()];
		int k=0;
		for(String id: occur){
			posFeas[k] = features.get(id);
			types[k] = entity_types.get(id);
			k++;
		}
		
		Vector negFea,posFea ;
		Vector qfea = Vector.zeros(K);
		double err;
		for(int i=0;i<iteration;i++){
			for(int j=0;j<posFeas.length;j++){
				
				posFea = posFeas[j];
				negFea = features.get( drawNegative( occur, types[j] ) );
				err = 1-logistic(qfea.dot(posFea) - qfea.dot(negFea));
				
				qfea = qfea.add( posFea.sub(negFea).mul(err).sub( qfea.mul(LAMBDA) ).mul(LEARN_RATE) );
			}
		}

		return qfea;
	}
}
	
/*	
	double predict(Vector[] feas){
		
		double squareSum = 0.0;
		for(int i=0;i<feas.length;i++)
			squareSum+= feas[i].dot(feas[i]);

		Vector vsum = Vector.zeros(K);
		for(int i=0;i<feas.length;i++)
			vsum = vsum.add(feas[i]);

		double sumSquare = vsum.dot(vsum);
		
		return sumSquare - squareSum;
	}
	*/
	
