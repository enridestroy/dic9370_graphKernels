package dic9370_gk;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author Enridestroy
 */
public class DIC9370_GK {
    public static void main(String[] args) {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        
//        int[][] edges = new int[][]{ {4,5},{3,4},{2,3},{1,2},{1,3},{1,4} };
//        
//        
//        Grph g = new InMemoryGrph();
//        int vertex_to_add = 5;
//        for(int i=0;i<vertex_to_add;i++)
//            g.addVertex();
//        
//        for(int[] e : edges){
//            if(e.length>1){
//                g.addDirectedSimpleEdge(e[0]-1, e[1]-1);
//            }    
//        }
        
        /**
         * Conversion du fichier Matlab
         */
        Matlab2RDF mm = new Matlab2RDF();
        mm.convert();
        
        ArrayList<double[][]> ams = Matlab2RDF.ams;
        ArrayList<Grph> tous_les_graphes = new ArrayList<>();
        
        for(double[][] d : ams){
            System.out.println("-------------------------------------");
            Grph one_g = new InMemoryGrph();
            for(int z=0;z<d.length;z++){
                one_g.addVertex();
            }
            int line = 0;
            for(double[] dd : d){
                int cell=0;
                for(double ddd : dd){
                    if(ddd > 0f)
                        one_g.addDirectedSimpleEdge(line, cell);
                    cell++;
                }
                line++;
            }
            tous_les_graphes.add(one_g);
        }
        
        System.out.println("graphes:"+tous_les_graphes.size());
        Grph[] graphes = tous_les_graphes.toArray(new Grph[tous_les_graphes.size()]);

        /*
        Cree des graphes manuellement
        Grph[] graphes2 = new Grph[2];
        graphes2[0] = new InMemoryGrph();
        graphes2[0].addVertex();
        graphes2[0].addVertex();
        graphes2[0].addVertex();
        graphes2[0].addUndirectedSimpleEdge(0, 1);
        graphes2[0].addUndirectedSimpleEdge(1, 2);
        graphes2[0].addUndirectedSimpleEdge(2, 0);
        
        graphes2[1] = new InMemoryGrph();
        graphes2[1].addVertex();
        graphes2[1].addVertex();
        graphes2[1].addVertex();
        graphes2[1].addVertex();
        graphes2[1].addUndirectedSimpleEdge(0, 1);
        graphes2[1].addUndirectedSimpleEdge(1, 2);
        graphes2[1].addUndirectedSimpleEdge(2, 3);
        graphes2[1].addUndirectedSimpleEdge(3, 0);
        
        Permet d'assigner des labels a des noeuds ou arcs
        Property p = new StringProperty("Node??");
        p.setValue(0, "tototo");
        g.setVerticesLabel(p);

        Property[] toArray = g.getProperties().toArray(new Property[g.getProperties().size()]);
        HashSet<Property> a = new HashSet();
        for(Property pp : toArray) a.add(p);

        Property findProperty = Property.findProperty("Node??", a);
        System.out.println(findProperty.getValueAsString(0));
        */
        
        //on prend que les 50 premiers graphes
        graphes = Arrays.copyOfRange(graphes, 0, 50);

        GraphKernel kernel;
        GKParams params = new GKParams();
        //pour le noyau a marche geometrique
        params.addParam("lambda", 0.01f);//facteur de degradation
        kernel = new GeometricWalkKernel();
        //pour le noyau a marche aleatoire
        //params.addParam("c", 0.001f);//probabilite d'arret de la random walk
        //params.addParam("k", 10);//nombre de random walks a calculer
        //kernel = new RandomWalkKernel();
        
        params.addParam("assume_symetry", true);//permet de calculer uniquement k(x,y) et pas k(y,x) car normalement les deux sont egaux
        
        double[][] computeKernelGramMatrix = computeKernel(kernel, graphes, params);
        
        writeGMatrixToHTML(computeKernelGramMatrix, kernel.getClass().getSimpleName()+"_"+params.toString()+"_output"+new Random().nextInt()+".html", 
                Color.white, Color.red);//couleurs du degrade dans la matrice
        
    }
    
    /**
     * 
     * @param gk
     * @param graphs
     * @param p
     * @return 
     */
    public static double[][] computeKernel(GraphKernel gk, Grph[] graphs, GKParams p){
        return gk.computeKernelGramMatrix(graphs, p);
    }
    
    /**
     * 
     * @param gmatrix
     * @param outputfile
     * @param deg_min
     * @param deg_max
     */
    public static void writeGMatrixToHTML(double[][] gmatrix, String outputfile, Color deg_min, Color deg_max){
        String content;
        StringBuilder bb = new StringBuilder();
        int item = 0;
        double min = 0f;
        double max = Integer.MIN_VALUE;

        int ranges = 50;

        for(int i=0;i<gmatrix.length;i++){
            for(int j=0;j<gmatrix[i].length;j++){
                double curr = gmatrix[i][j];
                if(curr<0) continue;//ca devrait pas etre negatif sauf si on l'a pas calcule(symetrie).
                if(curr < min)  min = curr;
                if(curr > max && curr != 1.0f) max = curr;
            }
       }
        System.out.println("max:"+max);
        System.out.println("min:"+min);
        double range = (max - min) / (float)(ranges);
        Color[] colors = new Color[ranges];

        Color c1 = deg_min;
        Color c2 = deg_max;
        for (int i = 0; i < colors.length; i++) {
          float ratio = (float)i / (float)colors.length;
          int red = (int)(c2.getRed() * ratio + c1.getRed() * (1 - ratio));
          int green = (int)(c2.getGreen() * ratio +c1.getGreen() * (1 - ratio));
          int blue = (int)(c2.getBlue() * ratio +c1.getBlue() * (1 - ratio));
          Color c = new Color(red, green, blue);
          colors[i] = c;
        }
            
        int tmax = 0;
        double __curr = max;
        while(__curr > 0f){
             __curr -= range;
             tmax++;
        }

        /**
         * Construction du fichier html
         */
        bb.append("<!DOCTYPE html><html><head>\n" +
        "<title>TODO supply a title</title>\n" +
        "<meta charset=\"UTF-8\">\n" +
        "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
        "</head><body>");

        bb.append("<div><table>");
        bb.append("<tr height=\"25\" width=\"25\"><th>\\</th>");
        for(int i=0;i<gmatrix.length;i++){
            bb.append("<th>").append(i).append("</th>");
        }
        bb.append("</tr>");
        for(int i=0;i<gmatrix.length;i++){
            StringBuilder b = new StringBuilder();
            for(int j=0;j<gmatrix[i].length;j++){
                if(i==j){
                    b.append("<td style=\"background-color:black;\">X</td>");
                }
                else{
                    double curr = gmatrix[i][j];
                    if(curr<=0){
                        //using symetric property
                        curr = gmatrix[j][i];
                    }
                    int degree =0;//(int)Math.floor(curr/range);
                    double _curr = curr;
                    while(_curr > 0f){
                        _curr -= range;
                        degree++;
                    }
                    
                    //System.out.println(""+(curr / range)+"///"+curr+"//"+range+"//"+degree);
                    int kqsqsd = ((degree*colors.length)/tmax);
                    if(kqsqsd<1) kqsqsd = 1;
                    String hex = Integer.toHexString(colors[kqsqsd-1].getRGB() & 0xffffff);
                    if (hex.length() < 6) {
                        hex = "0" + hex;
                    }
                    hex = "#"+hex;
                    String _content = ""+curr;
                    b.append("<td style=\"background-color:").append(hex).append("\">").append(_content.substring(0, 3)).append("</td>");

                }
            }
            bb.append("<tr height=\"25\" width=\"25\"><th>").append(item).append("</th>").append(b.toString()).append("</tr>");
            item++;
        }
        bb.append("</table></div></body>");
        content = bb.toString();

        Writer writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                  new FileOutputStream(outputfile), "utf-8"));
            writer.write(content);
        } catch (IOException ex) {
          // report
        } finally {
           try {writer.close();} catch (Exception ex) {}
       }
    }
    
}
