/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datavisualizer;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author santi
 */
public abstract class DistanceMatrix {    
    public String names[] = null;
            
    public DistanceMatrix(int n) {
        names = new String[n];
    }
     
    public DistanceMatrix(List<String> a_names) {
        names = new String[a_names.size()];                
        for(int i = 0;i<a_names.size();i++) names[i] = a_names.get(i);
    }
    
    public abstract double get(int i, int j);
    public abstract DistanceMatrix square();

}

