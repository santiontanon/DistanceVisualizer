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
import java.util.Arrays;
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
        List<String> outputFeatures = null;
        Object originalData[][] = null;
        List<String> classLabels = null;
    }
    
    
    public static void main(String args[]) throws IOException, Exception {
        List<Pair<String, DistanceMatrix>> matrices = new LinkedList<>();
        List<Pair<String, List<String>>> labels = new LinkedList<>();
        List<String> names = new LinkedList<>();       
        List<String> outputFeatures = new ArrayList<>();
        ARFFInformation arff = null;
        boolean ThreeD = false;
        outputFeatures.add("class");
        
        for(int i = 0;i<args.length;i++) {
            if (args[i].equals("-DM")) {
                String matrixFileName = args[i+1];
                loadDistanceMatrix(matrixFileName, matrixFileName, matrices, names);                
                i++;
            } else if (args[i].equals("-labels")) {
                String labelsFileName = args[i+1];
                loadLabels(labelsFileName, labelsFileName, labels);                
                i++;
            } else if (args[i].equals("-arffOutputFeatures")) {
                StringTokenizer st = new StringTokenizer(args[i+1],",");
                while(st.hasMoreTokens()) {
                    String fn = st.nextToken();
                    if (!outputFeatures.contains(fn)) outputFeatures.add(fn);
                }
                i++;
            } else if (args[i].equals("-arff")) {
                File arffFile = new File(args[i+1]);
                arff = loadDataFromArff(arffFile.getAbsolutePath());
                arff.outputFeatures = outputFeatures;
                Object[][] norm_minmax = normalizeARFF(arff,NORMALIZE_MIN_MAX);
                Object[][] norm_avgstd = normalizeARFF(arff,NORMALIZE_AVG_STD);
                DistanceMatrix m = euclideanFromARFF(arff, arff.originalData);
                matrices.add(new Pair<>("ARFF-"+arffFile.getName()+"-euclidean-distance", m));
                matrices.add(new Pair<>("ARFF-"+arffFile.getName()+"-euclidean-distance-norm-minmax", 
                        euclideanFromARFF(arff,norm_minmax)));
                matrices.add(new Pair<>("ARFF-"+arffFile.getName()+"-euclidean-distance-norm-std", 
                        euclideanFromARFF(arff,norm_avgstd)));
                matrices.add(new Pair<>("ARFF-"+arffFile.getName()+"-jaccard-distance", 
                        jaccardFromARFF(arff, arff.originalData, outputFeatures)));
                matrices.add(new Pair<>("ARFF-"+arffFile.getName()+"-jaccard-distance-norm-minmax", 
                        jaccardFromARFF(arff,norm_minmax, outputFeatures)));

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
        
        /*
        if (matrices.size()==0) {
            if (arff==null) {
                System.out.println("No distance matrix specified!! Please specify at least one distance matrix as: '-DM <distance matrix file>");
                System.exit(1);
            }
        } 
        */
 
        setIndexLabels("index", matrices, labels);
         
        createVisualization(new ArrayList<>(), matrices, labels, names, arff, outputFeatures, ThreeD);        
    }


    public static void createVisualization(List<Pair<String, InstancePositions>> positions,
                                           List<Pair<String, DistanceMatrix>> matrices, 
                                           List<Pair<String, List<String>>> labels, 
                                           List<String> names, 
                                           ARFFInformation arff,
                                           List<String> outputFeatures, 
                                           boolean ThreeD) {
        if (ThreeD) {
            Visualization3DMultiViewGUI.SHOW_KEY = true;
            Visualization3DMultiViewGUI w;
            if (arff==null) {
                w = new Visualization3DMultiViewGUI("Data Visualization", matrices, positions, labels, names, null, null, null, 1);
            } else {
                w = new Visualization3DMultiViewGUI("Data Visualization", matrices, positions, labels, names, arff.featureNames, arff.originalData, arff.outputFeatures, 1);
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
        System.out.println("Loaded matrix: " + fileName + " -> " + m.names.length + "x" + m.names.length);
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
        if (!matrices.isEmpty()) {
            for (int i = 0; i < matrices.get(0).m_b.names.length; i++) labels.add("" + i);
            all_labels.add(new Pair<String, List<String>>(name, labels));
        } else if (!all_labels.isEmpty()) {
            for (int i = 0; i < all_labels.get(0).m_b.size(); i++) labels.add("" + i);
            all_labels.add(new Pair<String, List<String>>(name, labels));
        }
    }       
    

    private static ARFFInformation loadDataFromArff(String arffFile) throws Exception 
    {
        System.out.println("Loading "+arffFile);
        ARFFInformation ret = new ARFFInformation();
        
        BufferedReader br = new BufferedReader(new FileReader(arffFile));
        ret.featureNames = new ArrayList<>();
        ret.classLabels = new ArrayList<>();
        List<Object []> instances = new ArrayList<>();
        
        while(true){
            String line = br.readLine();
            if (line==null) break;
            
            if (line.startsWith("@ATTRIBUTE") || 
                line.startsWith("@attribute")) {
                StringTokenizer st = new StringTokenizer(line);
                st.nextToken();
                String attribute = st.nextToken();
                if (!attribute.toLowerCase().equals("class")) {
                    ret.featureNames.add(attribute);
//                    String dataType = st.nextToken();
//                    if (!dataType.toLowerCase().equals("numeric")) {
//                        throw new Exception("non numeric attributes are not supported at this point when importing ARFF files! " + line);
//                    }
                }
            } else if (line.startsWith("@DATA") || line.startsWith("@data")) {
                break;
            }
        }
        
        while (true) {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            System.out.println(line + " -> " + ret.featureNames.size());
            StringTokenizer st = new StringTokenizer(line, ",");
            Object instance[] = new Object[ret.featureNames.size()];
            for (int i = 0; i < instance.length; i++) {
                String token = st.nextToken();
                try {
                    instance[i] = new Double(Double.parseDouble(token));
                } catch(Exception e) {
                    if (token.equals("?")) {
                        instance[i] = null;
                    } else {
                        instance[i] = token;
                    }
                }
//                if (ret.featureNames.get(i).equals("id") || ret.featureNames.get(i).equals("user")) instance[i] = 0;
            }
            instances.add(instance);
            ret.classLabels.add(st.nextToken());
        }

        ret.originalData = new Object[instances.size()][ret.featureNames.size()];
        for (int i = 0; i < ret.originalData.length; i++) {
            for (int j = 0; j < ret.originalData[i].length; j++) {
                ret.originalData[i][j] = instances.get(i)[j];
            }
        }
        return ret;
    }
    
    public final static int NORMALIZE_MIN_MAX = 1;
    public final static int NORMALIZE_AVG_STD = 2;
    
    private static Object[][] normalizeARFF(ARFFInformation arff, int normalization) {
        int n = arff.classLabels.size();
        int nFeatures = arff.featureNames.size();
        boolean numeric[] = new boolean[nFeatures];
        Object data[][] = new Object[n][nFeatures];
        for (int f = 0; f < nFeatures; f++) {
            numeric[f] = true;
            for (int i = 0; i < n; i++) {
                data[i][f] = arff.originalData[i][f];
                if (data[i][f]!=null &&
                    data[i][f] instanceof String) numeric[f] = false;
            }
        }
        if (normalization > 0) {
            Double norm_maximums[] = new Double[nFeatures];
            Double norm_minimums[] = new Double[nFeatures];
            Double norm_avg[] = new Double[nFeatures];
            Double norm_std[] = new Double[nFeatures];
            int nNumeric[] = new int[nFeatures];
            for (int f = 0; f < nFeatures; f++) {
                if (numeric[f]) {
                    norm_maximums[f] = Double.MIN_VALUE;
                    norm_minimums[f] = Double.MAX_VALUE;
                    norm_avg[f] = new Double(0);
                    norm_std[f] = new Double(0);
                } else {
                    norm_maximums[f] = null;
                    norm_minimums[f] = null;
                    norm_avg[f] = null;
                    norm_std[f] = null;
                }
            }   
            for (int i = 0; i < n; i++) {
                for (int f = 0; f < nFeatures; f++) {
                    if (numeric[f] && data[i][f]!=null) {
                        nNumeric[f]++;
                        if ((Double)data[i][f] > norm_maximums[f]) {
                            norm_maximums[f] = (Double)data[i][f];
                        }
                        if ((Double)data[i][f] < norm_minimums[f]) {
                            norm_minimums[f] = (Double)data[i][f];
                        }
                        norm_avg[f] += (Double)data[i][f];
                    }
                }
            }
            for (int f = 0; f < nFeatures; f++) {
                if (numeric[f]) {
                    norm_avg[f] /= nNumeric[f];
                }
            }
            for (int f = 0; f < nFeatures; f++) {
                if (numeric[f]) {
                    for (int i = 0; i < n; i++) {
                        if (data[i][f]!=null) {
                            double delta = (Double)data[i][f] - norm_avg[f];
                            norm_std[f] += delta * delta;
                        }
                    }
                }
            }
            for (int f = 0; f < nFeatures; f++) {
                if (numeric[f]) {
                    norm_std[f] = Math.sqrt(norm_std[f]);
                }
            }

            for (int i = 0; i < n; i++) {
                for (int f = 0; f < nFeatures; f++) {
                    if (numeric[f] && data[i][f]!=null) {
                        if (normalization == NORMALIZE_MIN_MAX) {
                            if ((norm_maximums[f] - norm_minimums[f])>0) {
                                data[i][f] = ((Double)data[i][f] - norm_minimums[f]) / (norm_maximums[f] - norm_minimums[f]);
                            } else {
                                data[i][f] = 0.0d;
                            }
                        } else if (normalization == NORMALIZE_AVG_STD) {
                            if (norm_std[f]>0) {
                                data[i][f] = ((Double)data[i][f] - norm_avg[f]) / norm_std[f];
                            } else {
                                data[i][f] = 0.0d;
                            }
                        }
                    }
                }
            }

        }
        return data;
    }

    
    public static DistanceMatrix euclideanFromARFF(ARFFInformation arff, Object[][] data) {
        int n = arff.classLabels.size();
        int nFeatures = arff.featureNames.size();
        boolean numeric[] = new boolean[nFeatures];
        boolean ignore[] = new boolean[nFeatures];
        for (int f = 0; f < nFeatures; f++) {
            numeric[f] = true;
            ignore[f] = false;
            if (arff.outputFeatures.contains(arff.featureNames.get(f))) ignore[f] = true;
            for (int i = 0; i < n; i++) {
                if (data[i][f]!=null &&
                    data[i][f] instanceof String) {
                    if (numeric[f]) {
                        System.out.println("feature " + arff.featureNames.get(f) + " is not numeric because of '" + data[i][f] + "'");
                    }
                    numeric[f] = false;
                }
            }
        }

        System.out.println(Arrays.toString(ignore));
        
        DenseDistanceMatrix m = new DenseDistanceMatrix(n);
        for (int i = 0; i < n; i++) {
            m.names[i] = "ARFFInstance" + i;
            for (int j = i + 1; j < n; j++) {
                double d = 0;
                for (int f = 0; f < nFeatures; f++) {
                    if (ignore[f]) continue;
                    if (data[i][f]!=null && data[j][f]!=null) {
                        if (numeric[f]) {
                            double fd = (Double)data[i][f] - (Double)data[j][f];
                            d += fd * fd;
                        } else {
                            if (!data[i][f].equals(data[j][f])) d+=1;
                        }
                    }
                }
                d = Math.sqrt(d);
                m.getMatrix()[i][j] = d;
                m.getMatrix()[j][i] = d;
            }
        }
        return m;
    }

    
    public static DistanceMatrix jaccardFromARFF(ARFFInformation arff, Object[][] data, List<String> arffFeaturesToIgnoreForDistance) {
        int n = arff.classLabels.size();
        int nFeatures = arff.featureNames.size();
        boolean numeric[] = new boolean[nFeatures];
        boolean ignore[] = new boolean[nFeatures];
        for (int f = 0; f < nFeatures; f++) {
            numeric[f] = true;
            ignore[f] = false;
            if (arffFeaturesToIgnoreForDistance.contains(arff.featureNames.get(f))) ignore[f] = true;
            for (int i = 0; i < n; i++) {
                if (data[i][f]!=null &&
                    data[i][f] instanceof String) numeric[f] = false;
            }
        }
        DenseDistanceMatrix m = new DenseDistanceMatrix(n);
        for (int i = 0; i < n; i++) {
            m.names[i] = "ARFFInstance" + i;
            for (int j = i + 1; j < n; j++) {
                double inter = 0;
                double union = 0;
                for (int f = 0; f < nFeatures; f++) {
                    if (ignore[f]) continue;
                    if (data[i][f]!=null && data[j][f]!=null) {
                        if (numeric[f]) {
                            inter += Math.min((Double)data[i][f], (Double)data[j][f]);
                            union += Math.max((Double)data[i][f], (Double)data[j][f]);
                        } else {
                            if (data[i][f].equals(data[j][f])) {
                                inter+=1;
                                union+=1;
                            } else {
                                union+=1;
                            }
                        }
                    }
                }
                double d = 1-inter/union;
                m.getMatrix()[i][j] = d;
                m.getMatrix()[j][i] = d;
            }
        }
        return m;
    }

}
