#include <iostream>

using namespace std;


//monkey and the doors.
int main()
{
    int mon;
    cin>>mon;
    int door=100;
    int arr[100];
    
    if(mon==0){
        return 0;
    }
    for(int i=0;i<100;i++){
        arr[i]=1;
    }
    int open=100;
    int closed=0;
    if(mon==1){
        return open;
    }
    for(int i=2;i<=mon;i++){
        for(int j=1;j<=100;j++){
            if(j%i==0){
                if(arr[j-1]==0){
                    arr[j-1]=1;
                }
                else{
                    arr[j-1]=0;
                }
            }
        }
    }
    
    for(int i=0;i<100;i++){
        if(arr[i]==1){
            count++;
        }
    }
    cout<<count<<endl;

    return 0;
}