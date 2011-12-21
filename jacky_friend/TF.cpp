#include<iostream>
#include<fstream>
#include<vector>
#include<map>
#include<set>
#include<ctime>
#include<stdlib.h>
#include<cmath>

#define K 50 //number of features
#define SIGMA 0.1 // parameter for random initialization
#define USER 0
#define TAG 1
#define ITEM 2
#define RATE 0.05
#define LAMBDA 0.0

using namespace std;

class Triple{
	public:
	int user;
	int tag;
	int item;
};

vector<Triple*>* triples = new vector<Triple*>() ;//training data
map<pair<int,int>,set<int>*>* posts = new map<pair<int,int>,set<int>*>() ;
//vector<Triple*>* triples_test = new vector<Triple*>(); //testing data

vector<int>* users = new vector<int>();
vector<int>* tags = new vector<int>();
vector<int>* items = new vector<int>();

map<int,double*>* user_feature = new map<int,double*>();
map<int,double*>* tag_feature = new map<int,double*>();
map<int,double*>* item_feature = new map<int,double*>();

/////////////////// Printer
void printArr(double* arr,int size){
	
	cerr << "[" ;
	for(int i=0;i<size;i++)
		cerr << arr[i] << ",";
	cerr << "]";
}

void printMap(map<int,double*>* m){

	map<int,double*>::iterator iter;
	cerr << "[" ;
	for(iter=m->begin();iter!=m->end();iter++){
		cerr << iter->first << "=>" ;
		printArr(iter->second,K);
		cerr << ",";
	}
	cerr << "]" ;
}

void printSet(set<int>* s){
	
	set<int>::iterator iter;
	cerr << "(" ;
	for(iter = s->begin(); iter != s->end() ; iter++){
		
		cerr << *iter << "," ;
	}
	cerr << ")" ;
}

void printPair(pair<int,int> pair){
	
	cerr << pair.first << "_" << pair.second ;
}

void printPost(map<pair<int,int>,set<int>*>* posts){
	
	map<pair<int,int>,set<int>*>::iterator iter;
	cerr << "[" ;
	for(iter = posts->begin(); iter != posts->end() ; iter++){
		
		printPair(iter->first);
		cerr << "=>" ;
		printSet(iter->second);
		cerr << ",";
	}
	cerr << "]" ;
}
///////////////////End Printer

///////////////////Vector Operation
double dot(double* a, double* b){
	double sum=0.0;
	for(int i=0;i<K;i++)
		sum += a[i]*b[i];
	return sum;
}

double* vmul(double c, double* v1){
	
	double* v2 = new double[K];
	for(int i=0;i<K;i++)
		v2[i] = v1[i]*c ;
	return v2;
}

double* vadd(double* v1, double* v2){
	
	double* v3 = new double[K];
	for(int i=0;i<K;i++)
		v3[i] = v1[i] + v2[i] ;
	return v3;
}

double* vsub(double* v1, double* v2){
	
	double* v3 = new double[K];
	for(int i=0;i<K;i++)
		v3[i] = v1[i] - v2[i] ;
	return v3;
}
//////////////////END Vector Operation


void readData(){
	
	ifstream fin("small_user_tag_item.txt");
	Triple* t ;
	char str[100];
	int tmp;
	fin.getline(str,100);
	while( !fin.eof() ){
		
		t = new Triple();
		fin >> tmp >> t->user >> t->tag >> t->item ;
		if( t->user==0 )break;

		triples->push_back(t);
		if( posts->find( pair<int,int>(t->user,t->item) )==posts->end() )
			posts->insert( pair<pair<int,int>,set<int>*>( pair<int,int>(t->user,t->item), new set<int>() ) );
		posts->find(pair<int,int>(t->user,t->item))->second->insert(t->tag);
		
		if( user_feature->find(t->user)==user_feature->end() ){
			users->push_back(t->user);
			user_feature->insert( pair<int,double*>(t->user,new double[K]) );
		}
		if( tag_feature->find(t->tag)==tag_feature->end() ){ 
			tags->push_back(t->tag);
			tag_feature->insert( pair<int,double*>(t->tag,new double[K]) );
		}
		if( item_feature->find(t->item)==item_feature->end() ){
			items->push_back(t->item);
			item_feature->insert( pair<int,double*>(t->item,new double[K]) );
		}
	}
}

void init_features(){
	
	srand(time(NULL));
	map<int,double*>::iterator iter;
	for(iter=user_feature->begin();iter!=user_feature->end();iter++){
		for(int k=0;k<K;k++)
			(iter->second)[k] = 0 - (SIGMA/2) + ((double)rand()/RAND_MAX)*SIGMA ;
	}
	for(iter=tag_feature->begin();iter!=tag_feature->end();iter++){
		for(int k=0;k<K;k++)
			(iter->second)[k] = 0 - (SIGMA/2) + ((double)rand()/RAND_MAX)*SIGMA ;
	}
	for(iter=item_feature->begin();iter!=item_feature->end();iter++){
		for(int k=0;k<K;k++)
			(iter->second)[k] = 0 - (SIGMA/2) + ((double)rand()/RAND_MAX)*SIGMA ;
	}
}

int draw_neg_example(Triple* triple, int type){ //type can be USER,TAG, or ITEM
	
	vector<int>* list;
	int pos_example;
	
	if(type==USER){
		list = users;
		pos_example = triple->user;
	}
	else if(type==TAG){
		list = tags;
		pos_example = triple->tag;
	}
	else{
		list = items;
		pos_example = triple->item;
	}

	int neg_example ;
	do{
		neg_example = list->at( rand() % (list->size()) ) ;
	}while(neg_example==pos_example);
	
	return neg_example ;
}


double predict(int u,int t,int i){
	
	double* userf = user_feature->find(u)->second ;
	double* tagf = tag_feature->find(t)->second ;
	double* itemf = item_feature->find(i)->second ;

	return dot(userf,tagf) + dot(tagf,itemf) + dot(userf,itemf)  ;
}

double rmse( vector<Triple*>* triples_test ){ //root mean squre error for "positive example"
	
	int user,tag,item;
	double pred ;//predict
	double sq_sum = 0.0;
	for(int i=0;i<triples_test->size();i++){
		
		user = triples_test->at(i)->user;
		tag = triples_test->at(i)->tag;
		item = triples_test->at(i)->item;
		pred = predict(user,tag,item);
		sq_sum += (1-pred)*(1-pred);
	}

	return sqrt( sq_sum/triples_test->size() );
}

double sample_rank_err(vector<Triple*>* triples_test){//now only for tag
	
	Triple* triple;
	int tag2 ;//negative tag
	double* ufea,*tfea,*ifea,*tfea2;//features of user,tag,item
	double* grad_ufea, *grad_tfea, *grad_ifea, *grad_tfea2 ;//greadients of features
	double pred_pos, pred_neg, pred_prob, lnErr;
	
	double sum =0.0;
	for(int i=0;i<triples_test->size();i++){
		
		triple = triples_test->at(i);
		ufea = user_feature->find(triple->user)->second;
		tfea = tag_feature->find(triple->tag)->second;
		ifea = item_feature->find(triple->item)->second;
		
		//draw negative tag example (consider draw negatie item example in the future)
		tag2 = draw_neg_example(triple,TAG) ;
		tfea2 = tag_feature->find(tag2)->second ;
		
		pred_pos = predict(triple->user,triple->tag,triple->item);
		pred_neg = predict(triple->user,tag2,triple->item);
		
		pred_prob = 1.0/( 1 + exp(-(pred_pos-pred_neg)) );
		lnErr = 1-pred_prob;
		sum += lnErr;
	}

	return sum/triples_test->size() ;
}
/*
pair<double,double> recall_precision( map<pair<int,int>,set<int>*>*  posts , int topK ){//now only for tag
	
	pair<int,int>* u_i;
	set<int>* pos_tags ;
	double* tag_score = new double[tags->size()];
	map<pair<int,int>,set<int>*>::iterator iter;
	//for each post <u,i> compute score of each tag
	for( iter=posts->begin(); iter!=posts->end() ; iter++ ){
		
		u_i = &(iter->first) ;
		pos_tags = iter->second ;

		for(int i
	}
}
*/
void train_features(int iter){ // no garbage collection for now
	
	Triple* triple;
	int tag2 ;//negative tag
	double *ufea, *tfea, *ifea, *tfea2;//features of user,tag,item
	double *grad_ufea, *grad_tfea, *grad_ifea, *grad_tfea2 ;//greadients of features
	double err_grad, pred_pos, pred_neg, pred_prob ;

	for(int i=0;i<iter;i++){
		
		triple = triples->at( rand() % triples->size() );
		ufea = user_feature->find(triple->user)->second;
		tfea = tag_feature->find(triple->tag)->second;
		ifea = item_feature->find(triple->item)->second;
		
		//draw negative tag example (consider draw negatie item example in the future)
		tag2 = draw_neg_example(triple,TAG) ;
		tfea2 = tag_feature->find(tag2)->second ;
		
		pred_pos = predict(triple->user,triple->tag,triple->item);
		pred_neg = predict(triple->user,tag2,triple->item);
		
		pred_prob = 1.0/( 1 + exp(-(pred_pos-pred_neg)) );
		err_grad = 1-pred_prob;
		
		grad_ufea = vsub( vmul(err_grad, vsub(tfea,tfea2) ) , vmul(LAMBDA,ufea) ) ;
		grad_ifea = vsub( vmul(err_grad, vsub(tfea,tfea2) ) , vmul(LAMBDA,ifea) );
		grad_tfea = vsub( vmul(err_grad,vadd(ufea,ifea)) , vmul(LAMBDA,tfea) );
		grad_tfea2 = vsub( vmul(-err_grad,vadd(ufea,ifea)) , vmul(LAMBDA,tfea2) );
		
		(*user_feature)[triple->user] = vadd( ufea , vmul(RATE,grad_ufea) );
		(*tag_feature)[triple->tag] = vadd( tfea , vmul(RATE,grad_tfea) );
		(*item_feature)[triple->item] = vadd( ifea , vmul(RATE,grad_ifea) );
		(*tag_feature)[tag2] = vadd( tfea2 , vmul(RATE,grad_tfea2) );
	}
}

int main(){
	
	readData();

	init_features();
	
	while(1){
	  train_features(10000);
	  cout << sample_rank_err(triples) << endl;
	}

	return 0;
}
