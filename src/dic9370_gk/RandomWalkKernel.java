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
import java.util.ArrayList;
import java.util.Random;
import toools.set.IntSet;


public class RandomWalkKernel extends GraphKernel{
    
    public final float c_constant = 0.000f;
    public final int number_of_random_walks = 20;
    private boolean assume_sym = false;
    
    public RandomWalkKernel(){
    }
    
    private int init_j(int i){
        if(this.assume_sym) return i+1;
        return 0;
    }
    
    /**
     * 
     * @param g
     * @param proba_stop
     * @return 
     */
    public static ArrayList<Integer> computeRandomWalk3(Grph g, float proba_stop){
        
        AdjacencyMatrix adjacencyMatrix = g.getAdjacencyMatrix();
        
        Random r = new Random();
        int point_de_depart = r.nextInt(g.getNumberOfVertices()-1);
        
        ArrayList<Integer> random_walk = new ArrayList<>();
        
        int curr_vertex=point_de_depart;
        int old_vertex=-1;
        boolean use_edges = true;
        int no_edge = 0;
        do{
            if(use_edges && old_vertex > -1){
                IntSet edgesConnecting = g.getEdgesConnecting(old_vertex, curr_vertex);
                random_walk.add(edgesConnecting.toIntArray()[0]);
            }
            random_walk.add(curr_vertex);
            if(do_we_need_to_stop(proba_stop)) {
                break;
            }
            
            ArrayList<Integer> possible_choices = new ArrayList<>();
            //on recupere les points disponibles
            for(int j=0;j<g.getNumberOfVertices();j++){
                int get = adjacencyMatrix.get(curr_vertex, j);
                if(get>0){
                    possible_choices.add(j);
                }
            }

            if(possible_choices.size()>0){
                 //on choisit une option
                int nextInt = r.nextInt(possible_choices.size());
                //on recupere le vertex
                old_vertex = curr_vertex;
                
                curr_vertex = possible_choices.get(nextInt);
                
            }
            else{
                System.out.println("No more available paths!!!!");
                break;//on a plus de choix possible, on doit s'arreter
            }
        }
        while(true);
        
        return random_walk;
    }
    
    /**
     * 
     * @param random_walk
     * @param g
     * @param c
     * @return 
     */
    public static float computeAPosterioriProbabiltyOfRandomWalk(ArrayList<Integer> random_walk, Grph g, float c){
        float[] p_t;
        float pX_knowingG;
        float p_q = c;
        float p_s = p_s(g);
        if(random_walk.size()>1){
            p_t = p_t(g, random_walk, c);
            float _t = p_t[0];
            int l = p_t.length;
            for(int i=1;i<l;i++){
                _t*=p_t[i];
            }
            pX_knowingG = p_s * _t * p_q;
        }
        else{
            pX_knowingG = p_s * p_q;
        }
        return pX_knowingG;
    }
    
    /**
     * 
     * @param probability
     * @return 
     */
    public static boolean do_we_need_to_stop(float probability){
        Random r = new Random();
        float nextFloat = r.nextInt(100);
        return nextFloat/100f < probability;
    }
    
    /**
     * 
     * @param g
     * @return 
     */
    public static float p_s(Grph g){
        float number_of_vertices = (float)g.getNumberOfVertices();
        float f = 1 / number_of_vertices;
        return f;
    }
    
    /**
     * calcule la condition de sortie.
     * @param xl
     * @return 
     */
    public static float p_q(String xl, float c){
        Random r = new Random();
        float f = r.nextFloat();
        if(f>c){
            return 1.0f;
        }
        return 0f;
    }
    
    /**
     * computes the probability of x knowing x-1
     * @param g
     * @param walk
     * @param c
     * @return 
     */
    public static float[] p_t(Grph g, ArrayList<Integer> walk, float c){
       float f = 1.0f - c;
       float ff;
       
       int s = walk.size()-2;
       if(s<1){
           System.out.println("La walk ne fait que deux steps...Pas de probas a calculer.");
           return null;
       }
       else if(s==1){
           
       }
       else{
           s /= 2; 
       }

       
       AdjacencyMatrix adjacencyMatrix = g.getAdjacencyMatrix();
       
       float[] p_t = new float[s];
       int j=0;
       for(int i=2;i<walk.size()-1;i++){
            int d=0;
            for(int z=0;z<g.getNumberOfVertices();z++){
                int get = adjacencyMatrix.get(walk.get(i-2), z);
                if(get>0){
                    d++;
                }
            }
            ff = (float)d;
            assert ff > 0f : "Alert, division by zero";
           
            p_t[j] = f/ff;
           
            j++;
            i++;//on saute les edges
       }
       
       return p_t;
    }
    
    
    /**
     * 
     * @param graphs_to_compare
     * @param stop_proba
     * @param number_of_walks
     * @return 
     */
    private double[][] computeKernelGramMatrix(Grph[] graphs_to_compare, float stop_proba, int number_of_walks){
        double[][] kernel_matrix = new double[graphs_to_compare.length][graphs_to_compare.length];
        //calculer le kernel
        for(int i=0;i<graphs_to_compare.length;i++){
            for(int j=init_j(i);j<graphs_to_compare.length;j++){
                //si les deux graphes sont les memes, ca sert a rien de les comparer
                if(i == j){
                    kernel_matrix[i][j] = 1.0f;//en fait osef...
                    continue;
                }
                kernel_matrix[i][j] = RandomWalkKernel.computeOne(graphs_to_compare[i], graphs_to_compare[j], stop_proba, number_of_walks);
            }
        }
        return kernel_matrix;
    }
    
    @Override
    public double compute(Object a, Object b){
        return (float)this.compute((Grph)a, (Grph)b);
    }
    
    /**
     * 
     * @param g
     * @param g2
     * @return 
     */
    private double compute(Grph g, Grph g2){
        //pour chaque paire de graphes possible, 
        float stop_proba = 0.1f;
        double computeOne = RandomWalkKernel.computeOne(g, g2, stop_proba, 100);
        return computeOne;
    }
    
    /**
     * 
     * @param g
     * @param g_
     * @param stop_proba
     * @param number_of_walks
     * @return 
     */
    private static double computeOne(Grph g, Grph g_, float stop_proba, int number_of_walks){
        if(number_of_walks<1){
            return 0f;
        }
        double[] sum_of_kernel_products = new double[number_of_walks];
        for(int o=0;o<number_of_walks;o++){
            ArrayList<Integer> rwg = RandomWalkKernel.computeRandomWalk3(g, stop_proba);
            ArrayList<Integer> rwg_ = RandomWalkKernel.computeRandomWalk3(g_, stop_proba);
            
            double p1 = RandomWalkKernel.computeAPosterioriProbabiltyOfRandomWalk(rwg, g, stop_proba);
            //System.out.println("pX_G="+p1);
            double p2 = RandomWalkKernel.computeAPosterioriProbabiltyOfRandomWalk(rwg_, g_, stop_proba);
            //System.out.println("pX_G_="+p2);
            double kz = new SequenceKernel().compute(rwg, rwg_);
            //System.out.println("kz="+kz);
            double r = kz*p1*p2;
            
            if(Double.isNaN(p1)){
                System.out.println("p1 is Nan");
            }
            if(Double.isNaN(p2)){
                System.out.println("p2 is nan");
            }
            if(Double.isNaN(kz)){
                System.out.println("kz is Nan");
            }
            sum_of_kernel_products[o] = r;
        }
        double kresult = sum_of_kernel_products[0];
        for(int o=1;o<number_of_walks;o++){
            //System.out.println("k_temp_"+o+"="+sum_of_kernel_products[o]);
            kresult += sum_of_kernel_products[o];
            if(Double.isNaN(kresult)){
                System.out.println("result is nan for"+o);
            }
        }
        return kresult;
    }


    @Override
    public double[][] computeKernelGramMatrix(Grph[] graphs_to_compare, GKParams params) {
        float c;
        int k;
        
        if(params.checkParam("c")){
            c = (float)params.getParam("c");
        }
        else{
            c = this.c_constant;
        }
        if(params.checkParam("k")){
            k = (int)params.getParam("k");
        }
        else{
            k = this.number_of_random_walks;
        }
        if(params.checkParam("assume_symetry")){
            this.assume_sym = (boolean)params.getParam("assume_symetry");
        }

        return this.computeKernelGramMatrix(graphs_to_compare, c, k);
    }
}
