import java.util.Arrays;
import java.util.Date;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

    public class Matrix{  
        public static void main(String args[]){  
          
        String s = "";

        System.out.println("0. Exit ");
        System.out.println("1. Multiplication ");
        System.out.println("2. Line Multiplication ");
        System.out.println("3. Line Multiplication BIG NUMBERS ");
        //System.out.println("4. Exercise 1");
        //System.out.println("5. Exercise 2");

        try{
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            s = bufferRead.readLine();
    
            System.out.println(s);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        switch(Integer.parseInt(s)){
            case 0:
            System.out.println("Exit");
            break;
            case 1:
            for(int i=600;i<=3000;i+=400){
                System.out.println("Matrix " + i + "x" + i);    
                multLineMatrix(i);
            }
            break;
            case 2:
            for(int i=600;i<=3000;i+=400){
                System.out.println("Matrix " + i + "x" + i);    
                multColMatrix(i);
            }
            case 3:
            for(int i=4096;i<=10240;i+=2048){
                System.out.println("Matrix " + i + "x" + i);    
                multColMatrix(i);
            }
            break;
            case 4:
            break;
        }

      
        }

        public static void multLineMatrix(int size){

            double a[][] = new double[size][size];
            double b[][] = new double[size][size];
            /*Result*/
            double c[][]=new double[size][size];

            /*fill a */
            for(int i = 0; i < size; i++){
                Arrays.fill(a[i],1.0);
            }

            
            /*fill b*/
            b = sequential(b);

            Date startTime = new Date();

            for(int i=0;i<size;i++){    
                for(int j=0;j<size;j++){       
                    for(int k=0;k<size;k++){      
                        c[i][j]+=a[i][k]*b[k][j];  
                    }
                }
            }

            Date endTime = new Date();

            double numSeconds = ((endTime.getTime() - startTime.getTime()) / (double)1000);
            System.out.format("Measured Time:" + "%.4f\n", numSeconds);

            printMatrix(size, c);

            }


            public static void multColMatrix(int size){

                double a[][] = new double[size][size];
                double b[][] = new double[size][size];
                /*Result*/
                double c[][]=new double[size][size];
    
                /*fill a */
                for(int i = 0; i < size; i++){
                    Arrays.fill(a[i],1.0);
                }
    
                /*fill b*/
                b = sequential(b);

                Date startTime = new Date();
    
                for(int i=0;i<size;i++){    
                    for(int k=0;k<size;k++){       
                            for(int j=0;j<size;j++){      
                                c[i][j]+=a[i][k]*b[k][j];     
                            }
                        }
                    }

                    Date endTime = new Date();

                    double numSeconds = ((endTime.getTime() - startTime.getTime()) / (double)1000);
                    System.out.format("Measured Time:" + "%.4f\n", numSeconds);

                    printMatrix(size, c);
                }

            public static double[][] sequential(double[][]arr){
                double[][]newC=new double[arr.length][arr[0].length];
                for(int i=0;i<arr.length;i++){
                    for(int j=0;j<arr[0].length;j++){
                        newC[i][j]= i + 1;
                    }
                }
                return newC; // rerunning the array witch created inside this method.
            }

            public static void printMatrix(int size, double[][] matrix){

                for(int j = 0; j < 10; j++){
                        System.out.print(matrix[0][j] + " ");
                }
                System.out.println();
            }
    }  