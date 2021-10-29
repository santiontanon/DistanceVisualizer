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
public class DenseDistanceMatrix extends DistanceMatrix{    
    double matrix[][] = null;
    
    
    public DenseDistanceMatrix(List<String> a_names, double a_matrix[][]) {
        super(a_names);
        matrix = a_matrix;
    }
    
    public DenseDistanceMatrix(List<String> a_names, float a_matrix[][]) {
        super(a_names);
        matrix = new double[a_matrix.length][a_matrix.length];
        for(int i = 0;i<a_matrix.length;i++) {
            for(int j = 0;j<a_matrix.length;j++) {
                matrix[i][j] = a_matrix[i][j];
            }
        }
    }
    
    public double get(int i, int j) {
        return matrix[i][j];
    }
    
    public double[][] getMatrix() {
        return matrix;
    }
    
    public void makeSymmetric() {
        for(int i = 0;i<names.length;i++) {
            for(int j = i;j<names.length;j++) {
                if (j==i) {
                    matrix[i][j] = 0;
                } else {
                    if (Double.isNaN(matrix[i][j])) {
                        matrix[i][j] = matrix[j][i];
                    } else if (Double.isNaN(matrix[j][i])) {
                        matrix[j][i] = matrix[i][j];
                    } else {
                        double tmp = (matrix[i][j] + matrix[j][i])/2;
                        matrix[j][i] = tmp;
                        matrix[i][j] = tmp;
                    }
                }
            }
        }
    }
            
    public static DistanceMatrix combination(DenseDistanceMatrix m1, DenseDistanceMatrix m2) {
        DenseDistanceMatrix m = new DenseDistanceMatrix(m1.matrix.length);
        m.names = m1.names;
        
        for(int i = 0;i<m1.matrix.length;i++) {
            for(int j = 0;j<m1.matrix.length;j++) {
                m.matrix[i][j] = Math.sqrt(m1.matrix[i][j]*m1.matrix[i][j] + m2.matrix[i][j]*m2.matrix[i][j]);
            }
        }
        return m;
    }
    

    public static DistanceMatrix sqrt(DenseDistanceMatrix m1) {
        DenseDistanceMatrix m = new DenseDistanceMatrix(m1.matrix.length);
        m.names = m1.names;
        
        for(int i = 0;i<m1.matrix.length;i++) {
            for(int j = 0;j<m1.matrix.length;j++) {
//                m.matrix[i][j] = Math.sqrt(Math.log(1+m1.matrix[i][j]));
                m.matrix[i][j] = Math.sqrt(m1.matrix[i][j]);
            }
        }
        return m;
    }
    
    
    public DistanceMatrix square() {
        DenseDistanceMatrix m = new DenseDistanceMatrix(matrix.length);
        m.names = names;
        
        for(int i = 0;i<matrix.length;i++) {
            for(int j = 0;j<matrix[i].length;j++) {
//                m.matrix[i][j] = Math.sqrt(Math.log(1+m1.matrix[i][j]));
                m.matrix[i][j] = matrix[i][j]*matrix[i][j];
            }
        }
        return m;
    }
    
    public static DistanceMatrix invert(DenseDistanceMatrix m1) {
        DenseDistanceMatrix m = new DenseDistanceMatrix(m1.matrix.length);
        m.names = m1.names;
        
        for(int i = 0;i<m1.matrix.length;i++) {
            for(int j = 0;j<m1.matrix.length;j++) {
                m.matrix[i][j] = 1/(1+m1.matrix[i][j]);
            }
        }
        return m;
    }
        
    
    public DenseDistanceMatrix(int size) {
        super(size);
        matrix = new double[size][size];
    }
    
        
    public static DenseDistanceMatrix loadDenseMatrix(String fileName) throws Exception {
        DenseDistanceMatrix d = null;

        BufferedReader br = new BufferedReader(new FileReader(fileName));        
        String line = br.readLine();
        // process the first line to know the number of elements:
        {
            String name = null;
            List<String> numbers = new LinkedList<String>();
            
            int state = 0;
            String tmp = "";
            for(int i = 0;i<line.length();i++) {
                char c = line.charAt(i);
                if (c=='\"') {
                    state = 1 - state;
                } else if (state == 0 && c=='\t') {
                    // end of chunk:
                    if (name==null) {
                        name = tmp.trim();
                        tmp = "";
                    } else {
                        numbers.add(tmp);
                        tmp = "";
                    }
                } else {
                    tmp += c;
                }
            }
            if (!tmp.equals("")) {
                numbers.add(tmp);
            }
            
//            System.out.println(name + " : " + numbers.size());
            
            d = new DenseDistanceMatrix(numbers.size());
            
            d.names[0] = name;
            for(int i = 0;i<numbers.size();i++) d.matrix[0][i] = Double.parseDouble(numbers.get(i));
        }

        int row = 1;
        while(br.ready()) {
            line = br.readLine();
            int state = 0;
            int column = -1;
            String tmp = "";
            for(int i = 0;i<line.length();i++) {
                char c = line.charAt(i);
                if (c=='\"') {
                    state = 1 - state;
                } else if (state ==0 && c=='\t') {
                    // end of chunk:
                    if (column==-1) {
                        d.names[row] = tmp.trim();
                        tmp = "";
//                        System.out.println(row + " = " + names[row]);
                        column++;
                    } else {
                        d.matrix[row][column] = Double.parseDouble(tmp);
                        tmp = "";
                        column++;
                    }
                } else {
                    tmp += c;
                }
            }
            if (!tmp.isEmpty()) {
                d.matrix[row][column] = Double.parseDouble(tmp);
                tmp = "";
            }
            row++;
        }
        
        return d;
    }
    
    
    public static DenseDistanceMatrix loadDenseMatrixWithoutNames(String fileName) throws Exception {
        return loadDenseMatrixWithoutNames(fileName, '\t');
    }


    public static DenseDistanceMatrix loadDenseMatrixWithoutNames(String fileName, char separator) throws Exception {
        DenseDistanceMatrix d = null;

        BufferedReader br = new BufferedReader(new FileReader(fileName));        
        String line = br.readLine();
        // process the first line to know the number of elements:
        {
            List<String> numbers = new LinkedList<String>();
            
            int state = 0;
            String tmp = "";
            for(int i = 0;i<line.length();i++) {
                char c = line.charAt(i);
                if (c=='\"') {
                    state = 1 - state;
                } else if (state == 0 && c==separator) {
                    // end of chunk:
                    numbers.add(tmp);
                    tmp = "";
                } else {
                    tmp += c;
                }
            }
            if (!tmp.equals("")) {
                numbers.add(tmp);
            }
            
//            System.out.println(name + " : " + numbers.size());
            
            d = new DenseDistanceMatrix(numbers.size());
            
            d.names[0] = "row0";
            for(int i = 0;i<numbers.size();i++) d.matrix[0][i] = Double.parseDouble(numbers.get(i));
        }

        int row = 1;
        while(br.ready()) {
            line = br.readLine();
            d.names[row] = "row" + row;            
            int state = 0;
            int column = 0;
            String tmp = "";
            for(int i = 0;i<line.length();i++) {
                char c = line.charAt(i);
                if (c=='\"') {
                    state = 1 - state;
                } else if (state ==0 && c==separator) {
                    // end of chunk:
                    d.matrix[row][column] = Double.parseDouble(tmp);
                    tmp = "";
                    column++;
                } else {
                    tmp += c;
                }
            }
            row++;
        }
        
        return d;
    }    
    
    
    public static DenseDistanceMatrix loadDenseMatrixFromCSV(String fileName) throws Exception {
        DenseDistanceMatrix d = null;

        BufferedReader br = new BufferedReader(new FileReader(fileName));        
        String line = br.readLine();
        // process the first line to know the number of elements:
        {
            List<String> numbers = new LinkedList<String>();
            
            int state = 0;
            String tmp = "";
            for(int i = 0;i<line.length();i++) {
                char c = line.charAt(i);
                if (c=='\"') {
                    state = 1 - state;
                } else if (state == 0 && c=='\t') {
                    // end of chunk:
                    numbers.add(tmp);
                    tmp = "";
                } else {
                    tmp += c;
                }
            }
            if (!tmp.equals("")) {
                numbers.add(tmp);
            }
            
//            System.out.println(name + " : " + numbers.size());
            
            d = new DenseDistanceMatrix(numbers.size());
            
            d.names[0] = "row0";
            for(int i = 0;i<numbers.size();i++) d.matrix[0][i] = Double.parseDouble(numbers.get(i));
        }

        int row = 1;
        while(br.ready()) {
            line = br.readLine();
            int state = 0;
            int column = 0;
            String tmp = "";
            for(int i = 0;i<line.length();i++) {
                char c = line.charAt(i);
                if (c=='\"') {
                    state = 1 - state;
                } else if (state ==0 && c=='\t') {
                    d.matrix[row][column] = Double.parseDouble(tmp);
                    tmp = "";
                    column++;
                } else {
                    tmp += c;
                }
            }
            d.names[row] = "row" + row;
            row++;
        }
        
        return d;
    }
    
    
    public static DenseDistanceMatrix loadSparseMatrix(String fileName) throws Exception {
        DenseDistanceMatrix d = null;

        File f = new File(fileName);
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
        d = new DenseDistanceMatrix(n);

        for(int i = 0;i<n;i++) {
            for(int j = 0;j<n;j++) {
                d.matrix[i][j] = Double.NaN;
            }
        }
        
        int row = 0;
        while(br.ready()) {
            String line = br.readLine();
            int state = 0;
            int column = -1;
            String tmp = "";
            for(int i = 0;i<line.length();i++) {
                char c = line.charAt(i);
                if (c=='\"') {
                    state = 1 - state;
                } else if (state ==0 && c=='\t') {
                    // end of chunk:
                    if (column==-1) {
                        d.names[row] = tmp.trim();
                        tmp = "";
//                        System.out.println(row + " = " + names[row]);
                        column++;
                    } else {
                        StringTokenizer st = new StringTokenizer(tmp,":");
                        int idx = Integer.parseInt(st.nextToken());
                        d.matrix[row][idx] = Double.parseDouble(st.nextToken());
                        tmp = "";
                        column++;
                    }
                } else {
                    tmp += c;
                }
            }
            row++;
        }
        
        return d;
    }    
    
    
    public void printMatrix() {
        for(int i = 0;i<matrix.length;i++) {
            if (names!=null) System.out.print("\"" + names[i] +"\"\t");
                        else System.out.print("\"" + i + "\"");
            for(int j = 0;j<matrix.length;j++) {
                System.out.print(matrix[i][j] + "\t");
            }        
            System.out.println("");
        }
    }
    
    
    public void saveMatrix(String fileName) throws IOException {
        FileWriter fw = new FileWriter(fileName);
        for(int i = 0;i<matrix.length;i++) {
            if (names!=null) fw.write("\"" + names[i] +"\"\t");
                        else fw.write("\"" + i + "\"");
            for(int j = 0;j<matrix.length;j++) {
                fw.write(matrix[i][j] + "\t");
            }        
            fw.write("\n");
        }
        fw.close();
    }

    
    public String toString() {
        StringBuffer s = new StringBuffer();
        
        for(int i = 0;i<matrix.length;i++) {
            for(int j = 0;j<matrix.length;j++) {
                s.append(matrix[i][j] + ", ");
            }        
            s.append("\n");
        }
             
        return s.toString();
    }
}

