/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datavisualizer;

import java.io.*;
import java.util.StringTokenizer;

/**
 *
 * @author santi
 */
public class InstancePositions {    
    public String names[] = null;
    public double positions[][];
    
    public InstancePositions(int size, int dimensions) {
        positions = new double[size][dimensions];
        names = new String[size];
    }

    public static InstancePositions loadFromFile(String fileName) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        int n = 0;
        int d = 0;
        while(true) {
            String line = br.readLine();
            if (d==0) {
                StringTokenizer st = new StringTokenizer(line, "\t");
                while(st.hasMoreTokens()) {
                    st.nextToken();
                    d++;
                }
                d--;    // since the first is the name
            }
            if (line==null) break;
            n++;
        }
        br.close();
        InstancePositions ip = new InstancePositions(n,d);
        br = new BufferedReader(new FileReader(fileName));
        for(int i = 0;i<n;i++) {
            String line = br.readLine();
            StringTokenizer st = new StringTokenizer(line, "\t");
            ip.names[i] = st.nextToken();
            for(int j = 0;j<d;j++) {
                ip.positions[i][j] = Double.parseDouble(st.nextToken());
            }
        }
        br.close();
        return ip;
    }
}

