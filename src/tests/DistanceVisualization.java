package tests;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import datavisualizer.DenseDistanceMatrix;
import datavisualizer.DistanceMatrix;
import datavisualizer.InstancePositions;
import datavisualizer.threed.multiview.Visualization3DMultiViewGUI;
import datavisualizer.twod.VisualizationGUI;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.JFrame;
import util.Pair;

/**
 *
 * @author santi
 */
public class DistanceVisualization {

    public static class ARFFInformation {
        List<String> featureNames = null;
        double originalData[][] = null;
        List<String> classLabels = null;
    }
    
    
    public static void main(String args[]) throws IOException, Exception {
        List<Pair<String, DistanceMatrix>> matrices = new LinkedList<>();
        List<Pair<String, List<String>>> labels = new LinkedList<>();
        List<String> names = new LinkedList<>();       
        ARFFInformation arff = null;
        boolean ThreeD = false;
        
        
        for(int i = 0;i<args.length;i++) {
            if (args[i].equals("-DM")) {
                String matrixFileName = args[i+1];
                loadDistanceMatrix(matrixFileName, matrixFileName, matrices, names);                
                i++;
            } else if (args[i].equals("-labels")) {
                String labelsFileName = args[i+1];
                loadLabels(labelsFileName, labelsFileName, labels);                
                i++;
            } else if (args[i].equals("-arff")) {
                String arffFile = args[i+1];
                
                arff = loadDataFromArff(arffFile);
                DistanceMatrix m = euclideanFromARFF(arff);
                matrices.add(new Pair<>("ARFF-euclidean-distance", m));
                labels.add(new Pair<>("ARFF-class-labels",arff.classLabels));
                if (names.isEmpty()) {
                    for(String name:m.names) names.add(name);
                }
                i++;
            } else if (args[i].equals("-3d")) {
                ThreeD = true;
            } else {
                System.out.println("Unrecognized parameter '"+args[i]+"'");
                System.exit(1);
            }
        }
        
        if (matrices.size()==0) {
            if (arff==null) {
                System.out.println("No distance matrix specified!! Please specify at least one distance matrix as: '-DM <distance matrix file>");
                System.exit(1);
            }
        } 
        
        setIndexLabels("index", matrices, labels);
            
        createVisualization(new ArrayList<>(), matrices, labels, names, arff, ThreeD);        
    }


    public static void createVisualization(List<Pair<String, InstancePositions>> positions,
                                           List<Pair<String, DistanceMatrix>> matrices, 
                                           List<Pair<String, List<String>>> labels, 
                                           List<String> names, 
                                           ARFFInformation arff,
                                           boolean ThreeD) {
        if (ThreeD) {
            Visualization3DMultiViewGUI.SHOW_KEY = true;
            Visualization3DMultiViewGUI w;
            if (arff==null) {
                w = new Visualization3DMultiViewGUI("Data Visualization", matrices, positions, labels, names, null, null, 1);
            } else {
                w = new Visualization3DMultiViewGUI("Data Visualization", matrices, positions, labels, names, arff.featureNames, arff.originalData, 1);
            }
//            w.visualization.KEY_THRESHOLD = 1;
            w.setResizable(true);
            w.setVisible(true);
            w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            w.updateLoop();

        } else {
            VisualizationGUI.SHOW_KEY = true;
            VisualizationGUI w = new VisualizationGUI("Data Visualization", matrices, positions, labels, names);
            w.setResizable(true);
            w.setVisible(true);
            w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            w.updateLoop();
        }
    }
    

    public static void loadPositions(String fileName, String name,
        List<Pair<String, InstancePositions>> matrices,
        List<String> names) throws Exception {
        InstancePositions m = InstancePositions.loadFromFile(fileName);
        matrices.add(new Pair<String, InstancePositions>(name, m));
        if (names.isEmpty()) {
            for (int i = 0; i < m.names.length; i++) {
                names.add(m.names[i]);
            }
        }
    }    
    
    public static void loadDistanceMatrix(String fileName, String name,
        List<Pair<String, DistanceMatrix>> matrices,
        List<String> names) throws Exception {
        DenseDistanceMatrix m = DenseDistanceMatrix.loadDenseMatrix(fileName);
        m.makeSymmetric();
        matrices.add(new Pair<String, DistanceMatrix>(name, m));
        if (names.isEmpty()) {
            for (int i = 0; i < m.names.length; i++) {
                names.add(m.names[i]);
            }
        }
    }
    

    public static void loadLabels(String fileName, String name,
        List<Pair<String, List<String>>> all_labels) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        List<String> labels = new ArrayList<>();
        do {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            StringTokenizer st = new StringTokenizer(line, "\t");
            st.nextToken();
            labels.add(st.nextToken());
        } while (true);
        all_labels.add(new Pair<String, List<String>>(name, labels));
        br.close();
    }
    
    
    public static void setIndexLabels(String name,
        List<Pair<String, DistanceMatrix>> matrices,
        List<Pair<String, List<String>>> all_labels) {
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < matrices.get(0).m_b.names.length; i++) {
            labels.add("" + i);
        }
        all_labels.add(new Pair<String, List<String>>(name, labels));
    }       
    

    private static ARFFInformation loadDataFromArff(String arffFile) throws Exception 
    {
        ARFFInformation ret = new ARFFInformation();
        
        BufferedReader br = new BufferedReader(new FileReader(arffFile));
        ret.featureNames = new ArrayList<>();
        ret.classLabels = new ArrayList<>();
        List<double []> instances = new ArrayList<>();
        
        while(true){
            String line = br.readLine();
            if (line==null) break;
            
            if (line.startsWith("@ATTRIBUTE")) {
                StringTokenizer st = new StringTokenizer(line);
                st.nextToken();
                String attribute = st.nextToken();
                if (!attribute.equals("class")) {
                    ret.featureNames.add(attribute);
                    String dataType = st.nextToken();
                    if (!dataType.equals("NUMERIC")) {
                        throw new Exception("non numeric attributes are not supported at this point when importing ARFF files!");
                    }
                }
            } else if (line.startsWith("@DATA")) {
                break;
            }
        }
        
        while(true){
            String line = br.readLine();
            if (line==null) break;
            StringTokenizer st = new StringTokenizer(line,",");
            double instance[] = new double[ret.featureNames.size()];
            for(int i = 0;i<instance.length;i++) {
                instance[i] = Double.parseDouble(st.nextToken());
            }
            instances.add(instance);
            ret.classLabels.add(st.nextToken());
        }        
        
        ret.originalData = new double[instances.size()][ret.featureNames.size()];
        for(int i = 0;i<ret.originalData.length;i++) {
            for(int j = 0;j<ret.originalData[i].length;j++) {
                ret.originalData[i][j] = instances.get(i)[j];
            }
        }
        return ret;
    }
    
    
    public static DistanceMatrix euclideanFromARFF(ARFFInformation arff)
    {
        int n = arff.classLabels.size();
        int nFeatures = arff.featureNames.size();
        DenseDistanceMatrix m = new DenseDistanceMatrix(n);
        for(int i = 0;i<n;i++) {
            m.names[i] = "ARFFInstance" + i;
            for(int j = i+1;j<n;j++) {
                double d = 0;
                for(int f = 0;f<nFeatures;f++) {
                    double fd = arff.originalData[i][f] - arff.originalData[j][f];
                    d+=fd*fd;
                }
                d = Math.sqrt(d);
                m.getMatrix()[i][j] = d;
                m.getMatrix()[j][i] = d;
            }
        }
        return m;
    }

}
