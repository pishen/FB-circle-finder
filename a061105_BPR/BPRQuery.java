import java.util.*;

public class BPRQuery extends BPR{
	
	//random ranking
	public BPRQuery(Map<String,Integer> entity_types){
		
		this.entity_types = entity_types;
		for( String s: entity_types.keySet() )
			features.put(s,Vector.rand(K));
	}
	
	//ranking with recommendation
	public BPRQuery(Map<String,Vector> fea, Map<String,Integer> entity_types){
		
		features = fea;
		this.entity_types = entity_types;
	}
	
	public List<String> query(String query,Integer type){
		
		List<String> qList = new ArrayList();
		qList.add(query);
		
		return query(qList,type);
	}

	//input: query=[id1,id2,...]
	//output: ranking list=[id1,id2...]
	public List<String> query(Collection<String> query,Integer type){
		
		Vector qfea ;
		qfea = cooccurFea(query);
		
		/*
		qfea.zeros(K);
		for(String qid: query){
			qfea = qfea.add( features.get(qid) );
		}*/
		
		TreeMap<Double,String> score_id = new TreeMap(new Comparator<Double>(){
			public int compare(Double a,Double b){
				return -a.compareTo(b);
			}	
		});

		Double score;
		for(String eid: features.keySet()){
			if( !type.equals(-1) && !entity_types.get(eid).equals(type))
				continue;
			score = qfea.dot( features.get(eid) );
			score_id.put(score,eid);
		}

		List<String> rank = new ArrayList();
		for(String id: score_id.values()){
			if(!query.contains(id))
				rank.add(id);
		}

		return rank;
	}
}
