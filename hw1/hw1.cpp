/**
 * @file hw1.cpp - Assignment 1- Using two threads to 
 * speed up the performance of hw1_setup.cpp.
 * @author Ruoyang Qiu
 * @see "Seattle University, CPSC5600, Winter 2021"
 * 
*/

#include <iostream>
#include "ThreadGroup.h"

using namespace std;

const int N_THREADS = 2;

int encode(int v) {
	// do something time-consuming (and arbitrary)
	for (int i = 0; i < 500; i++)
		v = ((v * v) + v) % 10;
	return v;
}

int decode(int v) {
	// do something time-consuming (and arbitrary)
	return encode(v);
}

/**
 * @struct
 * Data Structure store the pointer and length of the array 
 * 
*/
struct MySharedDataStruct {
    int length;
    int* data;
};

/**
 * @class EncodeThread - an implementation of the ()-operator fot
 * ThreadGroup template
 */
class EncodeThread{
public:
    void operator()(int id, void *sharedData){
        MySharedDataStruct *ourData = (MySharedDataStruct *)sharedData;
        int length = ourData->length / N_THREADS;
        int start = length * id;
        int end = length + start;
        for (int i = start; i < end; i++){
            ourData->data[i] = encode(ourData->data[i] );
        }    
    }
};

/**
 * @class DecodeThread - an implementation of the ()-operator fot
 * ThreadGroup template
 */
class DecodeThread {
public:
    void operator()(int id, void *sharedData){
        MySharedDataStruct *ourData = (MySharedDataStruct *)sharedData;
        int length = ourData->length / N_THREADS;
        int start = length * id;
        int end = length + start;
        for (int i = start; i < end; i++){
            ourData->data[i] = decode(ourData->data[i] );
        }
    }
};

/**
 * prefixSum function
 * 
 * @param data: pointer to the target array
 * @param length: length of the array 
*/
void prefixSums(int *data, int length) {
    // Shared data for threads
    MySharedDataStruct ourData = {length, data};

    // Encode array with two thread
    ThreadGroup<EncodeThread> encoders;
    encoders.createThread(0, &ourData);
    encoders.createThread(1, &ourData);
    encoders.waitForAll();

    // accumulate numbers
	int encodedSum = 0;
	for (int i = 0; i < length; i++) {
		encodedSum += data[i];
        data[i] = encodedSum;
	}

    // Decode numbers
    ThreadGroup<DecodeThread> decoders;
    decoders.createThread(0, &ourData);
    decoders.createThread(1, &ourData);
    decoders.waitForAll();
}

int main() {
	int length = 1000*1000;

	// make array
	int *data = new int[length];
	for (int i = 1; i < length; i++)
		data[i] = 1;
	data[0] = 6;

	// transform array into converted/deconverted prefix sum of original
	prefixSums(data, length);

	// printed out result is 6, 6, and 2 when data[0] is 6 to start and the rest 1
	cout << "[0]: " << data[0] << endl
			<< "[" << length/2 << "]: " << data[length/2] << endl 
			<< "[end]: " << data[length-1] << endl; 

    delete[] data;
	return 0;
}
