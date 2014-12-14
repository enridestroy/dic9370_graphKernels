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


public abstract class KernelFunction {
    public abstract double compute(Object a, Object b);
    
    public abstract double[][] computeKernelGramMatrix(Grph[] graphs_to_compare, GKParams params);
}
