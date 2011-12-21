#include<string>
#include<iostream>

using namespace std;

int main(){
	
	string str = "friend_name:吳蕙如 friend_id:100001367205479" ;
	
	
	int name_h = str.find_first_of(":")+1;
	int name_t = str.find("friend_id:")-1;
	int id_h = str.find_last_of(":")+1;
	int id_t = str.size();

	cout << str.substr(name_h,name_t-name_h) << endl;
	cout << str.substr(id_h,id_t-id_h) << endl;

	return 0;
}
