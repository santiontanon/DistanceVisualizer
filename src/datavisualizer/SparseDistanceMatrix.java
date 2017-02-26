/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datavisualizer;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;

/**
 *
 * @author santi
 */
public class SparseDistanceMatrix extends DistanceMatrix {    
    HashMap<Integer,Double> rows[] = null;
            
    public SparseDistanceMatrix(int n) {
        super(n);
        rows = new HashMap[n];
        for(int i = 0;i<rows.length;i++) rows[i] = new HashMap<>(n/100);
    }
     
    public SparseDistanceMatrix(List<String> a_names) {
        super(a_names);
        rows = new HashMap[a_names.size()];
        for(int i = 0;i<rows.length;i++) rows[i] = new HashMap<>(rows.length/100);
    }
    
    
    public double get(int i, int j) 
    {
        Double tmp = rows[i].get(j);
        if (tmp==null) return Double.NaN;
        return tmp;
    }
    
    public HashMap<Integer,Double> getRow(int row) {
        return rows[row];
    }
    
    
    public void makeSymmetric() {
        for(int i = 0;i<rows.length;i++) {
            rows[i].put(i,0.0);
            for(Entry<Integer,Double> e:rows[i].entrySet()) {
                Integer j = e.getKey();
                if (j!=i) {
                    Double v = rows[j].get(i);
                    if (v!=null) {
                        double tmp = (e.getValue() + v)/2;
                        rows[i].put(j, tmp);
                        rows[j].put(i, tmp);
                    } else {
                        rows[j].put(i, e.getValue());
                    }
                }
            }
//            System.out.println(i);
        }
    }    
    
    public DistanceMatrix square()
    {
        SparseDistanceMatrix m = new SparseDistanceMatrix(rows.length);
        m.names = names;

        for(int i = 0;i<rows.length;i++) {
            for(Entry<Integer,Double> e:rows[i].entrySet()) {
                m.rows[i].put(e.getKey(),e.getValue()*e.getValue());
            }
        }
        return m;
    }
    
    
    public static SparseDistanceMatrix loadSparseMatrix(String fileName) throws Exception {
        SparseDistanceMatrix d = null;

        BufferedReader br = new BufferedReader(new FileReader(fileName));        
        // process th whole file, just to know the number of elements:
        int n = 0;
        while(true){
            String line = br.readLine();
            if (line==null) break;
            n++;
        }
        br.close();
        br = new BufferedReader(new FileReader(fileName));        
        d = new SparseDistanceMatrix(n);
        
        int row = 0;
        while(br.ready()) {
            String line = br.readLine();
            StringTokenizer st = new StringTokenizer(line,":\t");
            d.names[row] =st.nextToken();
            while(st.hasMoreTokens()) {
                int idx = Integer.parseInt(st.nextToken());
                d.rows[row].put(idx, Double.parseDouble(st.nextToken()));
            }
            row++;
//            System.out.println(row);
        }
        
        return d;
    }    
}

