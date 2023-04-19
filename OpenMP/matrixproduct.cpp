#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <cstring>
//#include <papi.h>
#include <omp.h>

using namespace std;

#define SYSTEMTIME clock_t

 
void OnMult(int m_ar, int m_br) 
{
	
	SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
	

		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;



	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);

	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phc[i*m_br + j] = 0.0;

//	memset(phc, 0, (m_ar * m_ar) * sizeof(double));

	int counter = 0;

    Time1 = clock();

	//omp_set_dynamic(0);
	//omp_set_num_threads(4);

	#pragma omp parallel for private(i,j) num_threads(4) collapse(2) 
	
	for(i=0; i<m_ar; i++){	
		for(j=0; j<m_br; j++)
			{	
				temp = 0;
				#pragma omp simd private(k) reduction(+:temp)
				for(int k=0; k<m_ar; k++){	
					temp += pha[i*m_ar+k] * phb[k*m_br+j];
				}
				
				#pragma omp critical
				{
					counter ++;
					sprintf(st, "thread %d, (i,j) = (%d,%d), counter = %d\n", omp_get_thread_num(), i, j, counter);
					cout << st;
				}
				phc[i*m_ar+j]=temp;
			}
		}

	


    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);
	
	
}

// add code here for line x line matriz multiplication
void OnMultLine(int m_ar, int m_br)
{

	SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;

	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	memset(phc, 0, (m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;

	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);
	
	Time1 = clock();
	
	int counter = 0;
	#pragma omp parallel private(i, k) num_threads(4)
	for(i=0; i<m_ar; i++){	
		for(k=0; k<m_br; k++){
			temp = pha[m_ar+k];
			#pragma omp for private(j)
			for( j=0; j<m_ar; j++){	
				phc[i*m_ar+j] += temp * phb[k*m_br+j];
			}
			#pragma omp critical
				{
					
					counter ++;
					sprintf(st, "thread %d, (i,j) = (%d,%d), counter = %d\n", omp_get_thread_num(), i, j, counter);
					cout << st;
				}
		}
	}

    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;
    free(pha);
    free(phb);
    free(phc);

  
}

// add code here for block x block matriz multiplication
void OnMultBlock(int m_ar, int m_br, int bkSize)
{
    
	SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;

	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	memset(phc, 0, (m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;

	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);
	
	Time1 = clock();
	
    for (int i = 0; i < m_ar; i += bkSize) {
        for (int j = 0; j < m_ar; j += bkSize) {
            for (int k = 0; k < m_ar; k += bkSize) {
                for (int ii = i; ii < min(i + bkSize, m_ar); ii++) {
                    for (int jj = j; jj < min(j + bkSize, m_ar); jj++) {
                        for (int kk = k; kk < min(k + bkSize, m_ar); kk++) {
							phc[ii*m_ar+jj] += pha[ii*m_ar+kk] * phb[kk*m_br+jj];
                        }
                    }
                }
            }
        }
    }

    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;
    free(pha);
    free(phb);
    free(phc);
    
}



/*void handle_error (int retval)
{
  printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
  exit(1);
}*/

/* void init_papi() {
  int retval = PAPI_library_init(PAPI_VER_CURRENT);
  if (retval != PAPI_VER_CURRENT && retval < 0) {
    printf("PAPI library version mismatch!\n");
    exit(1);
  }
  if (retval < 0) handle_error(retval);

  std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
            << " MINOR: " << PAPI_VERSION_MINOR(retval)
            << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
} */



int main (int argc, char *argv[])
{

	char c;
	int lin, col, blockSize;
	int op;


	/*
	int EventSet = PAPI_NULL;
  	long long values[2];
  	int ret;
	
	ret = PAPI_library_init( PAPI_VER_CURRENT );
	if ( ret != PAPI_VER_CURRENT )
		std::cout << "FAIL" << endl;


	ret = PAPI_create_eventset(&EventSet);
		if (ret != PAPI_OK) cout << "ERROR: create eventset" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L1_DCM );
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_DCM" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCM" << endl;

	ret = PAPI_add_event(EventSet, PAPI_TOT_CYC);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_TOT_CYC" << endl;
	*/
	
	
	

	op=1;
	do {
		cout << endl << "0. Exit " << endl;
		cout << "1. Multiplication" << endl;
		cout << "2. Line Multiplication" << endl;
		cout << "3. Block Multiplication" << endl;
		cout << "4. Exercise 1" << endl;
		cout << "5. Exercise 2 (600 - 3000/400)" << endl;
		cout << "6. Exercise 2 (4096 - 10240/2048)" << endl;
		cout << "7. Exercise 3 (4096 - 10240/2048)" << endl;

		cout << "Selection?: ";
		cin >>op;
		if (op == 0)
			break;
		printf("Dimensions: lins=cols ? ");
   		cin >> lin;
   		col = lin;


		
		switch (op){
			case 1: 
				/*ret = PAPI_start(EventSet);
				if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;*/
				
				OnMult(lin, col);
				/*
				ret = PAPI_stop(EventSet, values);
				if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
				printf("L1 DCM: %lld \n",values[0]);
				printf("L2 DCM: %lld \n",values[1]);
				//print INS
				printf("NUM_INS: %lld \n", values[2]);

				ret = PAPI_reset( EventSet );
				if ( ret != PAPI_OK )
				std::cout << "FAIL reset" << endl; 
				*/
				
				break;
			case 2:
				/*ret = PAPI_start(EventSet);
				if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;
				*/
				OnMultLine(lin, col);
				/*
				ret = PAPI_stop(EventSet, values);
				if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
				printf("L1 DCM: %lld \n",values[0]);
				printf("L2 DCM: %lld \n",values[1]);
				printf("NUM_INS: %lld \n", values[2]);


				ret = PAPI_reset( EventSet );
				if ( ret != PAPI_OK )
				std::cout << "FAIL reset" << endl; 
				*/  
				break;
			case 3:
				cout << "Block Size? ";
				cin >> blockSize;

				/* ret = PAPI_start(EventSet);
				if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl; */

				OnMultBlock(lin, col, blockSize);

				/* ret = PAPI_stop(EventSet, values);
				if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
				printf("L1 DCM: %lld \n",values[0]);
				printf("L2 DCM: %lld \n",values[1]);
				printf("NUM_INS: %lld \n", values[2]);

				ret = PAPI_reset( EventSet );
				if ( ret != PAPI_OK )std::cout << "FAIL reset" << endl;  */
				break;
			case 4:
				for (int i = 600; i <= 3000; i+=400) {
					cout << "Dimension i =" << i << endl;

					/* ret = PAPI_start(EventSet);
					if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl; */
				
					OnMult(i, i);
					
					/* ret = PAPI_stop(EventSet, values);
					if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
					printf("L1 DCM: %lld \n",values[0]);
					printf("L2 DCM: %lld \n",values[1]);
					printf("NUM_INS: %lld \n", values[2]);

					ret = PAPI_reset( EventSet );
					if ( ret != PAPI_OK )std::cout << "FAIL reset" << endl;  */
				}
				break;
			case 5:
				for (int i = 600; i <= 3000; i+=400) {
					cout << "Dimension i =" << i << endl;

					/* ret = PAPI_start(EventSet);
					if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl; */
				
					OnMultLine(i, i);

					/* ret = PAPI_stop(EventSet, values);
					if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
					printf("L1 DCM: %lld \n",values[0]);
					printf("L2 DCM: %lld \n",values[1]);
					printf("NUM_INS: %lld \n", values[2]);

					ret = PAPI_reset( EventSet );
					if ( ret != PAPI_OK )std::cout << "FAIL reset" << endl; */ 
				}
				break;
			case 6:
				for (int i = 4096; i <= 10240; i+=2048) {
					cout << "Dimension i =" << i << endl;
					
					/* ret = PAPI_start(EventSet);
					if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl; */
				
					OnMultLine(i, i);

					/* ret = PAPI_stop(EventSet, values);
					if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
					printf("L1 DCM: %lld \n",values[0]);
					printf("L2 DCM: %lld \n",values[1]);
					printf("NUM_INS: %lld \n", values[2]);

					ret = PAPI_reset( EventSet );
					if ( ret != PAPI_OK )std::cout << "FAIL reset" << endl;  */
				}

				break;
			case 7:

				for (int i = 4096; i <= 10240; i+=2048) {
					for( int k = 0; k <= 2; k++ ){
					if (k == 0)
						blockSize = 128;
					if (k == 1)
						blockSize = 256;
					if (k == 2)
						blockSize = 512;

					cout << "Dimension i =" << i << endl;
					cout << "BlockSize = " << blockSize << endl;

					/* ret = PAPI_start(EventSet);
					if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl; */
				
					OnMultBlock(i, i, blockSize);

					/* ret = PAPI_stop(EventSet, values);
					if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
					printf("L1 DCM: %lld \n",values[0]);
					printf("L2 DCM: %lld \n",values[1]);
					printf("NUM_INS: %lld \n", values[2]);

					ret = PAPI_reset( EventSet );
					if ( ret != PAPI_OK )std::cout << "FAIL reset" << endl;  */
					}
				}
				break;

		}

	

	}while (op != 0);


	/*
	ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 
	
	ret = PAPI_remove_event( EventSet, PAPI_TOT_INS );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl;

	ret = PAPI_destroy_eventset( &EventSet );
	if ( ret != PAPI_OK )
		std::cout << "FAIL destroy" << endl;
	*/
	
	

}