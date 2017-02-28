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
import java.io.File;
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
                File arffFile = new File(args[i+1]);
                arff = loadDataFromArff(arffFile.getAbsolutePath());
                double[][] norm_minmax = normalizeARFF(arff,NORMALIZE_MIN_MAX);
                double[][] norm_avgstd = normalizeARFF(arff,NORMALIZE_AVG_STD);
                DistanceMatrix m = euclideanFromARFF(arff, arff.originalData);
                matrices.add(new Pair<>("ARFF-"+arffFile.getName()+"-euclidean-distance", m));
                matrices.add(new Pair<>("ARFF-"+arffFile.getName()+"-euclidean-distance-norm-minmax", 
                        euclideanFromARFF(arff,norm_minmax)));
                matrices.add(new Pair<>("ARFF-"+arffFile.getName()+"-euclidean-distance-norm-std", 
                        euclideanFromARFF(arff,norm_avgstd)));
                matrices.add(new Pair<>("ARFF-"+arffFile.getName()+"-jaccard-distance", 
                        jaccardFromARFF(arff, arff.originalData)));
                matrices.add(new Pair<>("ARFF-"+arffFile.getName()+"-jaccard-distance-norm-minmax", 
                        jaccardFromARFF(arff,norm_minmax)));
                matrices.add(new Pair<>("ARFF-"+arffFile.getName()+"-jaccard-distance-norm-std", 
                        jaccardFromARFF(arff,norm_avgstd)));

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
        System.out.println("Loading "+arffFile);
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

        while (true) {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            StringTokenizer st = new StringTokenizer(line, ",");
            double instance[] = new double[ret.featureNames.size()];
            for (int i = 0; i < instance.length; i++) {
                instance[i] = Double.parseDouble(st.nextToken());
            }
            instances.add(instance);
            ret.classLabels.add(st.nextToken());
        }

        ret.originalData = new double[instances.size()][ret.featureNames.size()];
        for (int i = 0; i < ret.originalData.length; i++) {
            for (int j = 0; j < ret.originalData[i].length; j++) {
                ret.originalData[i][j] = instances.get(i)[j];
            }
        }
        return ret;
    }
    
    public final static int NORMALIZE_MIN_MAX = 1;
    public final static int NORMALIZE_AVG_STD = 2;
    
    private static double[][] normalizeARFF(ARFFInformation arff, int normalization) {
        int n = arff.classLabels.size();
        int nFeatures = arff.featureNames.size();
        double data[][] = new double[n][nFeatures];
        for (int i = 0; i < n; i++) {
            for (int f = 0; f < nFeatures; f++) {
                data[i][f] = arff.originalData[i][f];
            }
        }
        if (normalization > 0) {
            double norm_maximums[] = new double[nFeatures];
            double norm_minimums[] = new double[nFeatures];
            double norm_sum[] = new double[nFeatures];
            double norm_avg[] = new double[nFeatures];
            double norm_mean[] = new double[nFeatures];
            double norm_M2[] = new double[nFeatures];
            double norm_std[] = new double[nFeatures];
            for (int f = 0; f < nFeatures; f++) {
                norm_maximums[f] = Double.MIN_VALUE;
                norm_minimums[f] = Double.MAX_VALUE;
                norm_sum[f] = 0;
                norm_mean[f] = 0;
                norm_M2[f] = 0;
            }
            for (int i = 0; i < n; i++) {
                for (int f = 0; f < nFeatures; f++) {
                    if (data[i][f] > norm_maximums[f]) {
                        norm_maximums[f] = data[i][f];
                    }
                    if (data[i][f] < norm_minimums[f]) {
                        norm_minimums[f] = data[i][f];
                    }
                    norm_sum[f] += data[i][f];
                }
            }
            for (int f = 0; f < nFeatures; f++) {
                norm_avg[f] = norm_avg[f] / n;
            }
            for (int i = 0; i < n; i++) {
                for (int f = 0; f < nFeatures; f++) {
                    double delta = data[i][f] - norm_mean[f];
                    norm_mean[f] += delta / (i + 1);
                    double delta2 = data[i][f] - norm_mean[f];
                    norm_M2[f] += delta * delta2;
                }
            }
            for (int f = 0; f < nFeatures; f++) {
                norm_std[f] = norm_M2[f] / (n - 1);
            }

            for (int i = 0; i < n; i++) {
                for (int f = 0; f < nFeatures; f++) {
                    if (normalization == 1) {
                        data[i][f] = (data[i][f] - norm_minimums[f]) / (norm_maximums[f] - norm_minimums[f]);
                    } else if (normalization == 2) {
                        data[i][f] = (data[i][f] - norm_avg[f]) / norm_std[f];
                    }
                }
            }

        }
        return data;
    }

    public static DistanceMatrix euclideanFromARFF(ARFFInformation arff, double[][] data) {
        int n = arff.classLabels.size();
        int nFeatures = arff.featureNames.size();
        DenseDistanceMatrix m = new DenseDistanceMatrix(n);
        for (int i = 0; i < n; i++) {
            m.names[i] = "ARFFInstance" + i;
            for (int j = i + 1; j < n; j++) {
                double d = 0;
                for (int f = 0; f < nFeatures; f++) {
                    double fd = data[i][f] - data[j][f];
                    d += fd * fd;
                }
                d = Math.sqrt(d);
                m.getMatrix()[i][j] = d;
                m.getMatrix()[j][i] = d;
            }
        }
        return m;
    }
    public static DistanceMatrix jaccardFromARFF(ARFFInformation arff, double[][] data) {
        int n = arff.classLabels.size();
        int nFeatures = arff.featureNames.size();
        DenseDistanceMatrix m = new DenseDistanceMatrix(n);
        for (int i = 0; i < n; i++) {
            m.names[i] = "ARFFInstance" + i;
            for (int j = i + 1; j < n; j++) {
                double inter = 0;
                double union = 0;
                for (int f = 0; f < nFeatures; f++) {
                    inter += Math.min(data[i][f], data[j][f]);
                    union += Math.max(data[i][f], data[j][f]);
                }
                double d = 1-inter/union;
                m.getMatrix()[i][j] = d;
                m.getMatrix()[j][i] = d;
            }
        }
        return m;
    }

}
