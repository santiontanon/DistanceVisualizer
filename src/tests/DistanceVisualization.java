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

    public static void main(String args[]) throws IOException, Exception {
        List<Pair<String, DistanceMatrix>> matrices = new LinkedList<>();
        List<Pair<String, List<String>>> labels = new LinkedList<>();
        List<String> names = new LinkedList<>();       
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
            } else if (args[i].equals("-3d")) {
                ThreeD = true;
            } else {
                System.out.println("Unrecognized parameter '"+args[i]+"'");
                System.exit(1);
            }
        }
        
        if (matrices.size()==0) {
            System.out.println("No distance matrix specified!! Please specify at least one distance matrix as: '-DM <distance matrix file>");
            System.exit(1);
        } else {
            
            setIndexLabels("index", matrices, labels);
            
            createVisualization(new ArrayList<>(), matrices, labels, names, ThreeD);        
        }
    }


    public static void createVisualization(List<Pair<String, InstancePositions>> positions,
                                           List<Pair<String, DistanceMatrix>> matrices, 
                                           List<Pair<String, List<String>>> labels, 
                                           List<String> names, 
                                           boolean ThreeD) {
        if (ThreeD) {
            Visualization3DMultiViewGUI.SHOW_KEY = true;
            Visualization3DMultiViewGUI w = new Visualization3DMultiViewGUI("Data Visualization", matrices, positions, labels, names, null, null, 1);
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

}
