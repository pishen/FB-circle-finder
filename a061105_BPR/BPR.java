import java.util.*;

class BPR{
	
	final static int USER=0;
	final static int TAG=1;
	final static int ITEM=2;
	final static int K=100;
	final static double NOISE_LEVEL=1e-4;
	final static double LEARN_RATE=0.01;
	final static double LAMBDA=1e-5;
	static Random ran = new Random();
	
	Map<String,Vector> features = new HashMap();
	Map<String,Integer> entity_types = new HashMap();
	List<List<String>> type_entities = new ArrayList();
	
	String drawNegative( Collection<String> tuple, Integer type){ 
		
		String neg_example;
		List<String> list;
		do{
			list = type_entities.get(type); 
			neg_example = list.get( ran.nextInt(list.size()) );
			
		}while( tuple.contains(neg_example) );
		
		return neg_example;
	}
	
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
	
	double logistic(double score){
		
		return 1.0/(1+Math.exp(-score));
	}

	Vector cooccurFea( List<String> occur , int iter){
		
		Vector[] posFeas = new Vector[occur.size()];
		Integer[] types = new Integer[occur.size()];
		String id;
		for(int i=0;i< occur.size();i++){
			id = occur.get(i);
			posFeas[i] = features.get(id);
			types[i] = entity_types.get(id);
		}
		
		Vector negFea,posFea ;
		Vector qfea = Vector.zeros(K);
		Vector[] feas_diff = new Vector[occur.size()];
		double err;

		for(int i=0;i<posFeas.length;i++)
			feas_diff[i] = posFeas[i];
		
		for(int i=0;i<iter;i++){
			for(int j=0;j<posFeas.length;j++){
				
				posFea = posFeas[j];
				negFea = features.get( drawNegative( occur, types[j] ) );
				
				feas_diff[j] = posFeas[j].sub( negFea );
				err = 1-logistic(predict(fea_diff));
				feas_diff[j] = posFea[j];

				qfea = qfea.add( fea.sub(negFea).mul(err).sub( qfea.mul(LAMBDA) ).mul(LEARN_RATE) );
			}
		}
	}
}
