/**
 * Homework7
 * 
 * Ruoyang Qiu
 * 
 * Using MPI to implement Homework 1
*/

#include <mpi.h>
#include <iostream>
#define MASTER 0

using namespace std;

const int N_THREAD = 2;
const int LENGTH = 1000*1000;

int encode(int v) {
    for(int i = 0; i < 500; i++)
        v = ((v * v) + v) % 10;
    return v;
}

int decode(int v){
    return encode(v);
}



int main(int argc, char *argv[]) {

    int numProcs,           /*number of processes*/
        procId,             /*nprocess id*/
        source,             /*id of sending process*/
        dest,               /*id of destination process*/
        start,              /*start index of the portion of the array for each process*/
        end,                /*end index of the portion of the array for each process*/
        length_per_task,    /*portion of array for each task*/
        tag2, tag1;         /*tags to distinguish messages*/
    MPI_Status status;
    int *data_array = new int[LENGTH];

    /*Initializing MPI*/
    MPI_Init(&argc, &argv);
    MPI_Comm_size(MPI_COMM_WORLD, &numProcs);
    MPI_Comm_rank(MPI_COMM_WORLD, &procId);
    
    length_per_task= LENGTH /numProcs;

    tag2 = 1;
    tag1 = 2;

    /*Master Processor*/
    if(procId == MASTER){
        /*Initialize data array*/
        for(int i = 1; i < LENGTH; i++){
            data_array[i] = 1;
        }
        data_array[0] = 6;

        /*********Encoding*********/

        /*Send data to other processors*/
        start = length_per_task;
        for(int i = 1; i < numProcs; i++){
            //cout << "Sending to " << i <<endl;
            dest = i;
            MPI_Send(&start, 1, MPI_INT, dest, tag1, MPI_COMM_WORLD);
            MPI_Send(&data_array[start], length_per_task, MPI_INT, dest, tag2, MPI_COMM_WORLD);
            start = start + length_per_task;
        }

        /*Do the actual work*/
        for(start = 0; start < length_per_task; start++){
            data_array[start] = encode(data_array[start]);
        }

        /*Receive data from other processors*/
        for(int i = 1; i < numProcs; i++){
            source = i;
            MPI_Recv(&start, 1, MPI_INT, source, tag1, MPI_COMM_WORLD, &status);
            MPI_Recv(&data_array[start], length_per_task, MPI_INT, source, tag2, MPI_COMM_WORLD, &status);
            start = start + length_per_task;
        }

        /*Accumulating encode numbers*/
        int encodeSum = 0;
        for(int i = 0; i <LENGTH; i ++){
            encodeSum += data_array[i];
            data_array[i] = encodeSum;
            //cout<< "encode " << i << ": " << data_array[i] << endl; 
        }

        /*********Decoding*********/

        /*Send data to other processors*/
        start = length_per_task;
        for(int i = 1; i < numProcs; i++){
            dest = i;
            MPI_Send(&start, 1, MPI_INT, dest, tag1, MPI_COMM_WORLD);
            MPI_Send(&data_array[start], length_per_task, MPI_INT, dest, tag2, MPI_COMM_WORLD);
            start = start + length_per_task;
        }
        
        /*Do the actual work*/
        for(start = 0; start < length_per_task; start++){
            data_array[start] = decode(data_array[start]);
        }

        /*Receive data from other processors*/
        for(int i = 1; i < numProcs; i++){
            source = i;
            MPI_Recv(&start, 1, MPI_INT, source, tag1, MPI_COMM_WORLD, &status);
            MPI_Recv(&data_array[start], length_per_task, MPI_INT, source, tag2, MPI_COMM_WORLD, &status);
            start = start + length_per_task;
        }

        /*********Printing Result*********/
        cout << "[0]: " << data_array[0] << endl
            << "[" << LENGTH/2 << "]: " << data_array[LENGTH/2] <<endl
            <<"[end]: " << data_array[LENGTH - 1] << endl;
    } 
    
    /*Other processors*/
    if(procId > MASTER) {
        
        source = MASTER;
        dest = MASTER;

        /*********Encoding*********/

        /*Receive data from Master*/
        MPI_Recv(&start, 1, MPI_INT, source, tag1, MPI_COMM_WORLD, &status);
        MPI_Recv(&data_array[start], length_per_task, MPI_INT, source, tag2, MPI_COMM_WORLD, &status);
        
        /*Do the actual work*/
        end = (procId == (numProcs - 1)) ? LENGTH : start + length_per_task;
        for(int i = start; i < end; i++){
            data_array[i] = encode(data_array[i]);
            //cout<<data_array[mystart]<<endl;
        }

        /*Send data back to Master*/
        MPI_Send(&start, 1, MPI_INT, dest, tag1, MPI_COMM_WORLD);
        MPI_Send(&data_array[start], length_per_task, MPI_INT, dest, tag2, MPI_COMM_WORLD);

        /*********Decode*********/

        /*Receive data from Master*/
        MPI_Recv(&start, 1, MPI_INT, source, tag1, MPI_COMM_WORLD, &status);
        MPI_Recv(&data_array[start], length_per_task, MPI_INT, source, tag2, MPI_COMM_WORLD, &status);

        /*Do the actual work*/
        end = (procId == (numProcs - 1)) ? LENGTH : start + length_per_task;
        for(int i = start; i < end; i++){
            data_array[i] = decode(data_array[i]);
        }

        /*Send data back to Master*/
        MPI_Send(&start, 1, MPI_INT, dest, tag1, MPI_COMM_WORLD);
        MPI_Send(&data_array[start], length_per_task, MPI_INT, dest, tag2, MPI_COMM_WORLD);

    }
    
    delete[] data_array;
    MPI_Finalize();
}

