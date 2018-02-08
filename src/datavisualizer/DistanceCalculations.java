/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datavisualizer;

import datavisualizer.DenseDistanceMatrix;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author santi
 */
public class DistanceCalculations {
    public static double[][] oneHotEncodingNumericMatrix(Object[][] data)
    {
        int n = data.length;
        int nFeatures = data[0].length;
        int nOutputFeatures = 0;
        boolean numeric[] = new boolean[nFeatures];
        List<String> categories[] = new List[nFeatures];
        for (int f = 0; f < nFeatures; f++) {
            numeric[f] = true;
            for (int i = 0; i < n; i++) {
                if (data[i][f]!=null &&
                    data[i][f] instanceof String) {
//                    if (numeric[f]) {
//                        System.out.println("feature " + arff.featureNames.get(f) + " is not numeric because of '" + data[i][f] + "'");
//                    }
                    numeric[f] = false;
                }
            }
            if (!numeric[f]) {
                categories[f] = new ArrayList<String>();
                for (int i = 0; i < n; i++) {                
                    if (data[i][f]!=null &&
                        data[i][f] instanceof String) {
                        if (!categories[f].contains((String)data[i][f])) {
                            categories[f].add((String)data[i][f]);
                        }
                    }
                }
                nOutputFeatures += categories[f].size();
            } else {
                nOutputFeatures++;
            }
        }
        
        double output[][] = new double[n][nOutputFeatures];
        
        int of = 0;
        for (int f = 0; f < nFeatures; f++) {
            if (numeric[f]) {
                for (int i = 0; i < n; i++) {
                    if (data[i][f]!=null) {
                        output[i][of] = (Double)data[i][f];
                    } else {
                        output[i][of] = 0;
                    }
                }
                of++;
            } else {
                for (int i = 0; i < n; i++) {
                    for(int j = 0;j<categories[f].size();j++) output[i][of+j] = 0;
                    if (data[i][f]!=null) {
                        int idx = categories[f].indexOf((String)data[i][f]);
                        output[i][of+idx] = 1;
                    }
                }
                of += categories[f].size();
            }
        }
        return output;        
    }    
    
        
}
