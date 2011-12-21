#include<iostream>
#include<fstream>
#include<string>
#include<map>
#include<vector>
#include<stdlib.h>

#define INF 1000000

using namespace std;

class Message{
	public:
	string content;
	vector<string> who_likes;
	vector<string> who_comments;
};

class Photo{
	public:
	string creator;
	vector<string> people;
};

void dumpVect(vector<string>& vect){

	cout << "[" ;
	for(int i=0;i<vect.size();i++){
		
		cout << vect[i] << "," ;
	}
	cout << "]" << endl;
}

class Parser{
	
	public:
	
	string host;

	map<string,string> friend_names;
	map<string,Message*> messages;
	map<string,Photo*> photos;

	Parser(char* fname){
		
		host = "627389346";
		parse(fname);
	}

	void parse(char* fname){
		
		ifstream fin(fname);
		char cstr[1000];
		
		//Find =====friend list=====
		fin.getline( cstr, INF );
		string str = cstr;
		while( str.find("=====friend list=====")==string::npos ){//until found
			fin.getline(cstr,INF);
			str = cstr;
		}
		
		//Parse friend_name:???? friend_id:?????
		//until =====statuses list=====
		fin.getline( cstr, INF);
		str = cstr ;
		int name_h,name_t,id_h,id_t;
		while( str.find("=====statuses list=====" ) == string::npos ){//until found
			
			name_h = str.find_first_of(":")+1;
			name_t = str.find("friend_id:")-1;
			id_h = str.find_last_of(":")+1;
			id_t = str.size()-1;
			
			string name = str.substr(name_h,name_t-name_h);
			string id = str.substr(id_h,id_t-id_h);
			
			friend_names.insert(pair<string,string>(id,name));

			fin.getline( cstr, INF );
			str = cstr;
		}
		
		//Parse status_id:???? message:?????
		//	who_likes:??,??,??,
		//	who_comments:??,??,??,
		//
		//until =====photos tag list===== found
		fin.getline( cstr, INF );
		str = cstr ;
		string cur_msg_id,content;
		int h,t;
		while( str.find("=====photos tag list=====") == string::npos ){//until found
			
			if( str.find("status_id:") != string::npos ){ //find status_id
				
				h = str.find_first_of(":")+1;
				t = str.find_first_of(" ");
				cur_msg_id = str.substr( h , t-h ) ;
				
				h = str.find_first_of(":",t)+1;
				t = str.size()-1;
				content = str.substr(h,t-h);

				Message* msg = new Message();
				messages.insert( pair<string,Message*>(cur_msg_id,msg) );
				msg->content = content;
				
			}
			
			if( str.find("who_likes:") != string::npos ){
				
				Message* msg = messages.find( cur_msg_id )->second;
				
				int h = str.find_first_of(":") + 1;
				int t = str.find_first_of(",");
				while( t != string::npos ){
					
					msg->who_likes.push_back( str.substr(h,t-h) );
					
					h = t+1;
					t = str.find_first_of(",",h);
				}

			}
			
			if( str.find("who_comments:") != string::npos ){
				
				Message* msg = messages.find( cur_msg_id )->second;
				
				int h = str.find_first_of(":") + 1;
				int t = str.find_first_of(",");
				while( t != string::npos ){
					
					msg->who_comments.push_back( str.substr(h,t-h) );
					
					h = t+1;
					t = str.find_first_of(",",h);
				}

			}

			fin.getline( cstr, INF );
			str = cstr ;
		}
		
		
		//Parse photo_id:???? creator_id:?????
		//	tags:??,??,??,
		//until end of file
		fin.getline( cstr, INF );
		str = cstr ;
		string cur_photo_id,creator;
		while( !fin.eof() ){

			if( str.find("photo_id:") != string::npos ){ //find photo_id
				
				h = str.find_first_of(":")+1;
				t = str.find_first_of(" ");
				cur_photo_id = str.substr( h , t-h ) ;

				h = str.find_first_of(":",t)+1;
				t = str.size()-1;
				creator = str.substr(h,t-h);

				Photo* photo = new Photo();
				photos.insert( pair<string,Photo*>(cur_photo_id,photo) );
				photo->creator = creator;

			}

			if( str.find("tags:") != string::npos ){
				
				Photo* photo = photos.find( cur_photo_id )->second;
				
				int h = str.find_first_of(":") + 1;
				int t = str.find_first_of(",");
				while( t != string::npos ){
					
					photo->people.push_back( str.substr(h,t-h) );
					
					h = t+1;
					t = str.find_first_of(",",h);
				}
			}
			fin.getline( cstr, INF );
			str = cstr ;
		}
		fin.close();
	}
};

