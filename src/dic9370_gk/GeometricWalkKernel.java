/**
 *
 * @author Enridestroy
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dic9370_gk;

import grph.Grph;
import grph.algo.AdjacencyMatrix;
import grph.in_memory.InMemoryGrph;
import java.util.Arrays;


public class GeometricWalkKernel extends GraphKernel{
    private float lambda = 0.001f;
    private final float d_lambda = 0.001f;
    private boolean assume_sym = false;
    
    public float compute(Grph g, Grph g_){        
        //calculer le tensor product
        //Grph tensorproduct = this.tensorproduct(g, g);
        //float[][] kernel_matrix = new float[1][1];
        
        double[] arrayForKronecker = this.getArrayForKronecker(g);
        double[] arrayForKronecker1 = this.getArrayForKronecker(g_);
        
        int m = (int)Math.sqrt(arrayForKronecker.length);
        int n = (int)Math.sqrt(arrayForKronecker1.length);
        
        double[] product = KroneckerOperation.product(arrayForKronecker, m, m, arrayForKronecker1, n, n);
        
        //m*m*n*n => size
        //donc cote de m*n
        //reconstruire un graphe => visualisation.
        Grph tensorproduct = new InMemoryGrph();
        for(int k=0;k<(m*n);k++){
            tensorproduct.addVertex();
        }
        
        int m_n = m*n;
        for(int z=0;z<(m*n);z++){
            double[] d = Arrays.copyOfRange(product, m_n*z, (m_n*z)+m_n);
            for(int p=0;p<d.length;p++){
                if(d[p] > 0f)
                    tensorproduct.addDirectedSimpleEdge(z, p);
            }
        }
//        afficher les graphes:
//        g.display();
//        g_.display();
//        tensorproduct.display();
        
        //prendre le nombre vertex
        int vx = tensorproduct.getNumberOfVertices();

        float _lambda = this.lambda;
        float stop_threshold = 0.00001f;//ou utiliser Float.MIN_VALUE
        AdjacencyMatrix adjacencyMatrix = tensorproduct.getAdjacencyMatrix();
        
        int base_k = 2;
        
        float cur_l = _lambda;
        int _k=base_k;
        while(cur_l > stop_threshold){
            cur_l = (float) Math.pow(_lambda, _k);
            _k++;
        }
        AdjacencyMatrix[] adj_pow = new AdjacencyMatrix[_k-base_k];
        float[] lamda_pow = new float[_k-base_k];
        for(int ii=0;ii<adj_pow.length;ii++){
            adj_pow[ii] = AdjacencyMatrix.power(adjacencyMatrix, (ii+base_k));
        }
        for(int ii=0;ii<lamda_pow.length;ii++){
            lamda_pow[ii] = (float) Math.pow(_lambda, (ii+base_k));
        }
        
        
        float sum = 0.f;
        for(int i=0;i<vx;i++){
            for(int j=0;j<vx;j++){
                int k=base_k;
                float _sum = 0.f;
                while((k-base_k)<adj_pow.length){
                    AdjacencyMatrix power = adj_pow[k-base_k];
                    float decay_pow = lamda_pow[k-base_k];
                    _sum+= decay_pow * power.get(i, j);
                    k++;
                }
                sum+=_sum;
            }
        }
        System.out.println("GWK:"+sum);
        //pour chaque vertex, chercher les reoutes de degre n dans la matrice d'adjacence
        return sum;
    }
    
    private int init_j(int i){
        if(this.assume_sym) return i+1;
        return 0;
    }
    
    /**
     * 
     * @param graphs_to_compare
     * @param lambda
     * @return 
     */
    private double[][] computeKernelGramMatrix(Grph[] graphs_to_compare, float lambda){
        double[][] kernel_matrix = new double[graphs_to_compare.length][graphs_to_compare.length];
        this.lambda = lambda;
        for(int i=0;i<graphs_to_compare.length;i++){
            int j;
            for(j=init_j(i);j<graphs_to_compare.length;j++){
                //si les deux graphes sont les memes, ca sert a rien de les comparer
                if(i == j){
                    kernel_matrix[i][j] = 1.0f;//en fait osef...
                    continue;
                }
                System.out.println("Comparing two more graphes..."+i+"@"+j);
                kernel_matrix[i][j] = this.compute(graphs_to_compare[i], graphs_to_compare[j]);
            }
        }
        return kernel_matrix;
    }
    
    @Override
    public double compute(Object a, Object b) {
        return this.compute((Grph)a, (Grph)b);
    }
    
    /**
     * calcule le tensor product de deux graphes
     * @param a
     * @param b
     * @return 
     */
    public Grph tensorproduct(Grph a, Grph b){
        return null;
    }

    public double[] getArrayForKronecker(Grph g){
        AdjacencyMatrix adjacencyMatrix = g.getAdjacencyMatrix();
        int m = adjacencyMatrix.getSize();
        double[] flat_matrix = new double[m*m];
        
        for(int i=0;i<m;i++){
            for(int j=0;j<m;j++){
                flat_matrix[(i*m)+j] = adjacencyMatrix.get(i, j);
            }
        }
        return flat_matrix;
    }

    @Override
    public double[][] computeKernelGramMatrix(Grph[] graphs_to_compare, GKParams params) {
        float _lambda;boolean as;
        if(params.checkParam("lambda")){
            _lambda = (float)params.getParam("lambda");
        }
        else{
            _lambda = this.d_lambda;
        }
        if(params.checkParam("assume_symetry")){
            this.assume_sym = (boolean)params.getParam("assume_symetry");
        }
        return this.computeKernelGramMatrix(graphs_to_compare, _lambda);
    }
}
