/**
 * @file hw2.cpp - Assignment 2- Implementation of
 * Ladner-Fischer parallel prefix sum algorithm
 * @author Ruoyang Qiu
 * @see "Seattle University, CPSC5600, Winter 2021"
*/


#include <vector>
#include <iostream>
#include <future>

using namespace std;

typedef vector<int> Data;
int LEVEL = 4;

/**
 * @class Base class for Heaper structure 
*/
class Heaper
{
public:

    /**
     * Constructor
     * @param data: vector store the leaf nodes
     * 
    */
    Heaper(const Data *data) : n(data->size()), data(data)
    {
        interior = new Data(n - 1, 0);
    }

    /**
     * Destructor
    */
    virtual ~Heaper()
    {
        delete interior;
    }

protected:
    int n; // # of leaf
    const Data *data; // leaf nodes
    Data *interior; // interior nodes

    virtual int size()
    {
        return n - 1 + n;
    }

    virtual int value(int i)
    {
        if(i < n - 1)
        {
            return interior->at(i);
        } else 
        {
            return data->at(leafIndex(i));
        }
    }

    virtual bool isLeaf(int i)
    {
        return i >= n-1;
    }

    virtual int left(int i)
    {
        return 2 * i + 1;
    }

    virtual int right(int i)
    {
        return 2 * i + 2;
    }

    virtual int parent(int i)
    {
        return (i / 2) - 1;
    }

    // Get the index of the leaf node in leaf array
    virtual int leafIndex(int i)
    {
        return i - (n - 1);
    }

};

/**
 * @class SumHeap: extend Heaper that run prefix sum
*/
class SumHeap: public Heaper {
public:
    SumHeap(const Data *data): Heaper(data){
        calcSum(0, 0); // pair-wise sum pass
    }

    /**
     * Do prefix sum pass
     * @param prefix: arrray store prefix sum result
    */
    void prefixSums(Data *prefix)
    {
        calcPrefix(0, 0, 0, prefix);
    }
private:
    /**
     * calculate sum using two pair sum
     * @param i: index of the node in tha Heaper
     * @param level: level of the node
    */
    void calcSum(int i, int level)
    {
        if(isLeaf(i))
        {
            return;
        }

        if(level < LEVEL)
        {
            auto handle = async(launch::async, &SumHeap::calcSum, this, left(i), level + 1);
            calcSum(right(i), level + 1);
            handle.wait();
        } else
        {
            calcSum(left(i), level + 1);
            calcSum(right(i), level + 1);
        }
        interior->at(i) = value(left(i)) + value(right(i));
    }

    /**
     * calculate sum using pair-wise sum
     * @param i: index of the node in tha Heaper
     * @param prefixValue: prefix value given by parent node
     * @param level: level of the node
     * @param prefix: Data stores the prefix sum reault
    */
    void calcPrefix(int i, int prefixValue, int level, Data *prefix)
    {
        if(isLeaf(i))
        {
            prefix->at(leafIndex(i)) = prefixValue + value(i);
            return;
        }

        if(level < LEVEL)
        {
            auto handle = async(launch::async, &SumHeap::calcPrefix, this, left(i), prefixValue, level + 1, prefix);
            calcPrefix(right(i), prefixValue + value(left(i)), level + 1, prefix);
        } else 
        {
            calcPrefix(left(i), prefixValue, level + 1, prefix);
            calcPrefix(right(i), prefixValue + value(left(i)), level + 1, prefix);
        }
    }
};



const int N = 1<<26;

int main()
{
    Data data(N, 1);  // put a 1 in each element of the data array
    data[0] = 10;
    Data prefix(N, 1);

    // start timer
    auto start = chrono::steady_clock::now();

    SumHeap heap(&data);
    heap.prefixSums(&prefix);

    // stop timer
    auto end = chrono::steady_clock::now();
    auto elpased = chrono::duration<double,milli>(end-start).count();

    int check = 10;
    for (int elem: prefix)
        if (elem != check++) {
            cout << "FAILED RESULT at " << check-1;
            break;
        }
    cout << "in " << elpased << "ms" << endl;

    return 0;
}

    