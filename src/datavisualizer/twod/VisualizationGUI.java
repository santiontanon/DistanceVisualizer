/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datavisualizer.twod;

import datavisualizer.twod.positioncontrollers.ForceDistanceMatrixVisualization;
import datavisualizer.twod.positioncontrollers.InstancePositionsVisualization;
import datavisualizer.twod.positioncontrollers.VisualizationPositionController;
import datavisualizer.DistanceMatrix;
import datavisualizer.InstancePositions;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Checkbox;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import javax.swing.*;
import util.IsNumber;
import util.Pair;

/**
 *
 * @author santi
 */
public class VisualizationGUI extends JFrame {
    
    public static int MINIMUM_THRESHOLD = 5;
    public static boolean SHOW_KEY = true;
    
    public static final int W_WIDTH = 1024;
    public static final int W_HEIGHT = 700;
    public static final int INFO_WIDTH = 320;
    
    List<Pair<String,DistanceMatrix>> matrices = null;
    List<Pair<String,InstancePositions>> positions = null;
    
    List<Pair<String,VisualizationPositionController>> controllers = null;
    List<String> names = null;
    List<Pair<String,List<String>>> labelSets = null;
    
    public Visualization visualization = null;
    JComboBox controllerSelectionBox = null;
    JComboBox labelSetSelectionBox1 = null;
    JComboBox labelSetSelectionBox2 = null;
    JComboBox labelSetSelectionBox3 = null;
    JTextArea statistics = null;
    JComboBox modelTargetSlectionBox = null;
    
    // only for discrete labels (select which ones to see):
    JList selectedLabelsBox = null;
    JList ignoredLabelsBox = null;
    List<String> allLabelsSelected = new LinkedList<>();
    List<String> allLabelsIgnored = new LinkedList<>();

    Checkbox freezeCheckBox = null;
    JPanel inputpanel = null;
    JPanel discreteLabelSelectionPanel = null;
    
    int controllerSelected = -1;
    int labelSetSelected1 = 0;
    int labelSetSelected2 = 0;
    int labelSetSelected3 = 0;
    boolean currentLabelsContinuous1 = false;
    boolean currentLabelsContinuous2 = false;
    boolean currentLabelsContinuous3 = false;
    
    public VisualizationGUI(String name, 
                            List<Pair<String,DistanceMatrix>> a_matrices,
                            List<Pair<String,InstancePositions>> a_positions,
                            List<Pair<String,List<String>>> a_labels,
                            List<String> a_names) {
        super(name);
        setPreferredSize(new Dimension(W_WIDTH,W_HEIGHT));
        setSize(W_WIDTH,W_HEIGHT);
        
        matrices = a_matrices;
        positions = a_positions;
                
        names = a_names;
        labelSets = a_labels;
        labelSetSelected1 = 0;
        labelSetSelected2 = 0;
        labelSetSelected3 = 0;
        currentLabelsContinuous1 = areLabelsContinuous(labelSets.get(labelSetSelected1).m_b);
        if (!currentLabelsContinuous1) {
            labelSetSelected1 = 1;
            generateLabelLists();
        }

        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e) {
          System.out.println("Error setting native LAF: " + e);
        }

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

        setBackground(Color.WHITE);

        getContentPane().removeAll();
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));
        visualization = new Visualization(labelSets.get(0).m_b, names, allLabelsSelected, currentLabelsContinuous1);
        visualization.setPreferredSize(new Dimension(W_WIDTH-INFO_WIDTH,W_HEIGHT));
        visualization.SHOW_KEY = SHOW_KEY;
        panel.add(visualization);
        
        controllers = new ArrayList<>();
        for(Pair<String,DistanceMatrix> tmp:matrices) {
            controllers.add(new Pair<String,VisualizationPositionController>(tmp.m_a, new ForceDistanceMatrixVisualization(tmp.m_b, visualization)));
        }
        for(Pair<String,InstancePositions> tmp:positions) {
            controllers.add(new Pair<String,VisualizationPositionController>(tmp.m_a, new InstancePositionsVisualization(tmp.m_b, visualization)));            
        }
        
        {
            inputpanel = new JPanel();
            inputpanel.setLayout(new BoxLayout(inputpanel,BoxLayout.Y_AXIS));
            
            freezeCheckBox = new Checkbox("Freeze");
            freezeCheckBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    repaint();
                }
            });
            inputpanel.add(freezeCheckBox);
            Checkbox showData = new Checkbox("Show Data");
            showData.setState(visualization.SHOW_DATA);
            showData.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    visualization.SHOW_DATA = e.getStateChange() == ItemEvent.SELECTED;
                    repaint();
                }
            });
            inputpanel.add(showData);
            Checkbox showOutlines = new Checkbox("Show Outlines");
            showOutlines.setState(visualization.OUTLINES);
            showOutlines.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    visualization.OUTLINES = e.getStateChange() == ItemEvent.SELECTED;
                    repaint();
                }
            });
            inputpanel.add(showOutlines);

            JButton updateStats = new JButton("Update Statistics");
            updateStats.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateStatistics();
                }
            });
            inputpanel.add(updateStats);
            
            inputpanel.add(new JLabel("Select Position Controller:"));
            
            String []controllerNames = new String[controllers.size()];
            for(int i = 0;i<controllers.size();i++) controllerNames[i] = controllers.get(i).m_a;
            controllerSelectionBox = new JComboBox(controllerNames);
            controllerSelectionBox.setPreferredSize(new Dimension(INFO_WIDTH,32));
            inputpanel.add(controllerSelectionBox);
            
            inputpanel.add(new JLabel("Select Labels (up to 3):"));

            String []clusteringNames = new String[labelSets.size()+1];
            clusteringNames[0] = null;
            for(int i = 0;i<labelSets.size();i++) clusteringNames[i+1] = labelSets.get(i).m_a;
            labelSetSelectionBox1 = new JComboBox(clusteringNames);
            labelSetSelectionBox1.setPreferredSize(new Dimension(INFO_WIDTH,32));
            inputpanel.add(labelSetSelectionBox1);
            labelSetSelectionBox2 = new JComboBox(clusteringNames);
            labelSetSelectionBox2.setPreferredSize(new Dimension(INFO_WIDTH,32));
            inputpanel.add(labelSetSelectionBox2);
            labelSetSelectionBox3 = new JComboBox(clusteringNames);
            labelSetSelectionBox3.setPreferredSize(new Dimension(INFO_WIDTH,32));
            inputpanel.add(labelSetSelectionBox3);
/*                        
            if (labelSetSelected1>0 && !currentLabelsContinuous1) {
                discreteLabelSelectionPanel = discreteLabelSelectionPanel();
                inputpanel.add(discreteLabelSelectionPanel);
            }
*/                      
            statistics = new JTextArea(500,20);
            statistics.setText("");
            statistics.setPreferredSize(new Dimension(INFO_WIDTH,400));
            statistics.setEditable(false);            
            statistics.setLineWrap(true);
            statistics.setWrapStyleWord(false);
            statistics.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            statistics.setFont(new Font("monospaced", Font.PLAIN, 12));
            statistics.setSize(512, 512);
            JScrollPane scrollingText = new JScrollPane(statistics);                
            
            inputpanel.add(scrollingText);
            
            panel.add(inputpanel);
            
            inputpanel.setPreferredSize(new Dimension(INFO_WIDTH,100));
        }
        
        getContentPane().add(panel);
        pack();
    }    
    
    
    public JPanel createDiscreteLabelSelectionPanel() {
        JPanel tmp = new JPanel();
        tmp.setLayout(new BoxLayout(tmp,BoxLayout.X_AXIS));
        generateLabelLists();
        selectedLabelsBox.setAlignmentY(TOP_ALIGNMENT);
        tmp.add(selectedLabelsBox);
        JButton ignore = new JButton("ignore");
        ignore.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selected = (String)selectedLabelsBox.getSelectedValue();
                if (selected!=null) ignoreLabel(selected);
            }
        });
        JButton add = new JButton("add");
        add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selected = (String)ignoredLabelsBox.getSelectedValue();
                if (selected!=null) unignoreLabel(selected);
            }
        });
        JButton color = new JButton("color");
        color.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selected = (String)selectedLabelsBox.getSelectedValue();
                if (selected!=null) {
                    changeLabelColor(selected);
                }
            }
        });
        JPanel tmp2 = new JPanel();
        tmp2.setLayout(new BoxLayout(tmp2,BoxLayout.Y_AXIS));
        tmp2.add(ignore);
        tmp2.add(add);
        tmp2.add(color);
        tmp2.setAlignmentY(TOP_ALIGNMENT);
        tmp.add(tmp2);
        ignoredLabelsBox.setAlignmentY(TOP_ALIGNMENT);
        tmp.add(ignoredLabelsBox);        
        
        return tmp;
    }
    
    
    public void ignore(int n) {
        visualization.ignore(n);
    }
    
    
    public void clearIgnored() {
        if (visualization!=null) visualization.clearIgnored();
    }
    
    
    public void generateLabelLists() {       
        Pair<String,List<String>> labels = labelSets.get(labelSetSelected1-1);
        allLabelsSelected = new LinkedList<>();
        allLabelsIgnored = new LinkedList<>();
        for(String l:labels.m_b) {
            if (!allLabelsSelected.contains(l)) allLabelsSelected.add(l);
        }
        
        String []allLabelsArray = new String[allLabelsSelected.size()];
        for(int i = 0;i<allLabelsSelected.size();i++) allLabelsArray[i] = allLabelsSelected.get(i);
        String []allLabelsIgnoredArray = new String[allLabelsIgnored.size()];
        for(int i = 0;i<allLabelsIgnored.size();i++) allLabelsIgnoredArray[i] = allLabelsIgnored.get(i);
        
        if (selectedLabelsBox==null) {
            selectedLabelsBox = new JList<String>(allLabelsArray);
            ignoredLabelsBox = new JList<String>(allLabelsIgnoredArray);
        } else {
            selectedLabelsBox.setListData(allLabelsArray);
            ignoredLabelsBox.setListData(allLabelsIgnoredArray);
        }
        
        updateIgnoredArray();
    }
    
    
    public void ignoreLabel(String labelToIgnore) {
        allLabelsSelected.remove(labelToIgnore);
        allLabelsIgnored.add(labelToIgnore);
        
        String []allLabelsSelectedArray = new String[allLabelsSelected.size()];
        for(int i = 0;i<allLabelsSelected.size();i++) allLabelsSelectedArray[i] = allLabelsSelected.get(i);
        String []allLabelsIgnoredArray = new String[allLabelsIgnored.size()];
        for(int i = 0;i<allLabelsIgnored.size();i++) allLabelsIgnoredArray[i] = allLabelsIgnored.get(i);
                
        selectedLabelsBox.setListData(allLabelsSelectedArray);
        ignoredLabelsBox.setListData(allLabelsIgnoredArray);
        
        visualization.setKey(allLabelsSelected);
        updateIgnoredArray();
    }

    
    public void unignoreLabel(String labelToIgnore) {
        allLabelsSelected.add(labelToIgnore);
        allLabelsIgnored.remove(labelToIgnore);
        
        String []allLabelsSelectedArray = new String[allLabelsSelected.size()];
        for(int i = 0;i<allLabelsSelected.size();i++) allLabelsSelectedArray[i] = allLabelsSelected.get(i);
        String []allLabelsIgnoredArray = new String[allLabelsIgnored.size()];
        for(int i = 0;i<allLabelsIgnored.size();i++) allLabelsIgnoredArray[i] = allLabelsIgnored.get(i);
        
        
        selectedLabelsBox.setListData(allLabelsSelectedArray);
        ignoredLabelsBox.setListData(allLabelsIgnoredArray);
        
        visualization.setKey(allLabelsSelected);
        updateIgnoredArray();
    }
    
    
    public void changeLabelColor(String label) {
        System.out.println("changing color of " + label);
        JFrame dialogue = new JFrame("Change Color for " + label);
        dialogue.setPreferredSize(new Dimension(100,140));
        dialogue.setSize(100,140);
        dialogue.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
        Color currentColor = visualization.labelColors.get(label);
        JTextField rtext = new JTextField(""+currentColor.getRed());
        JTextField gtext = new JTextField(""+currentColor.getGreen());
        JTextField btext = new JTextField(""+currentColor.getBlue());
        panel.add(rtext);
        panel.add(gtext);
        panel.add(btext);
        JButton set = new JButton("set");
        set.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int r = Integer.parseInt(rtext.getText());
                    int g = Integer.parseInt(gtext.getText());
                    int b = Integer.parseInt(btext.getText());
                    
                    visualization.labelColors.put(label, new Color(r,g,b));
                } catch(Exception ex) {
                    
                }
            }
        });
        panel.add(set);
        dialogue.add(panel);
        dialogue.setVisible(true);
    }
    
    
    public void updateIgnoredArray() {
        clearIgnored();
        Pair<String,List<String>> labels = labelSets.get(labelSetSelected1-1);
        for(int i = 0;i<names.size();i++) {
            if (allLabelsIgnored.contains(labels.m_b.get(i))) ignore(i);
        }
    }

    
    public void updateLoop() {
        while(true) {
            try {
                Thread.sleep(1);
            }catch(Exception e) {
                e.printStackTrace();
            }

            if (!freezeCheckBox.getState()) {
//            long start = System.currentTimeMillis();
            //for(int i = 0;i<2;i++) 
                if (controllerSelected>=0) controllers.get(controllerSelected).m_b.updatePositions();
//            long end = System.currentTimeMillis();
//            System.out.println(end-start);
            }
            
            if (visualization!=null) {
                if (controllerSelectionBox!=null) {
                    int selected = controllerSelectionBox.getSelectedIndex();
                    if (selected!=controllerSelected) {
                        controllerSelected = selected;                        
                    }
                }
                if (labelSetSelectionBox1!=null) {
                    int selected1 = labelSetSelectionBox1.getSelectedIndex();
                    int selected2 = labelSetSelectionBox2.getSelectedIndex();
                    int selected3 = labelSetSelectionBox3.getSelectedIndex();
                    if (selected1!=labelSetSelected1 ||
                        selected2!=labelSetSelected2 ||
                        selected3!=labelSetSelected3) {
                        labelSetSelected1 = selected1;
                        labelSetSelected2 = selected2;
                        labelSetSelected3 = selected3;
                        currentLabelsContinuous1 = false;
                        currentLabelsContinuous2 = false;
                        currentLabelsContinuous3 = false;
                        if (labelSetSelected1>0) currentLabelsContinuous1 = areLabelsContinuous(labelSets.get(labelSetSelected1-1).m_b);
                        if (labelSetSelected2>0) currentLabelsContinuous2 = areLabelsContinuous(labelSets.get(labelSetSelected2-1).m_b);
                        if (labelSetSelected3>0) currentLabelsContinuous3 = areLabelsContinuous(labelSets.get(labelSetSelected3-1).m_b);
                        
                        if (discreteLabelSelectionPanel!=null) {
                            inputpanel.remove(discreteLabelSelectionPanel);
                            discreteLabelSelectionPanel = null;
                        }
                       
                        if (labelSetSelected1>0 && !currentLabelsContinuous1) {
                            discreteLabelSelectionPanel = createDiscreteLabelSelectionPanel();
                            inputpanel.add(discreteLabelSelectionPanel);
                        }                        
                        
                        visualization.setClasses(labelSetSelected1>0 ? labelSets.get(labelSetSelected1-1).m_b:null,
                                                 labelSetSelected2>0 ? labelSets.get(labelSetSelected2-1).m_b:null,
                                                 labelSetSelected3>0 ? labelSets.get(labelSetSelected3-1).m_b:null,
                                                 allLabelsSelected, 
                                                 currentLabelsContinuous1,
                                                 currentLabelsContinuous2,
                                                 currentLabelsContinuous3);
                        
                        if (labelSetSelected1>0 && !currentLabelsContinuous1) {
                            List<String> toIgnore = new ArrayList<>();
                            for(String label:visualization.toAppearInKey) {
                                if (visualization.labelCounts.get(label) < MINIMUM_THRESHOLD) toIgnore.add(label);
                            }
                            for(String label:toIgnore) ignoreLabel(label);
                        }
                    }
                }
                repaint();
            }            
        }
    }
    
    public static class ClassesComparator implements Comparator<String> {

        HashMap<String,Integer> counts = null;
        
        public ClassesComparator(HashMap<String,Integer> a_counts) {
            counts = a_counts;
        }
        
        public int compare(String c1, String c2) {
            int v1 = counts.get(c1);
            int v2 = counts.get(c2);
            if (v1<v2) return 1;
            if (v1==v2) return 0;
            return -1;
        }

    };
    
    
    public void updateStatistics() {
        statistics.setText("");
        if (currentLabelsContinuous1) {
            addDistortionStatistics();
        } else {
            addDistortionStatistics();
//            updateStatisticsDiscrete();
        }
    }
    
    public void addDistortionStatistics() {
        int cs = labelSetSelected1;
        if (cs==-1) cs = 0;
        int ms = controllerSelected;
        if (ms==-1) ms = 0;
        
        statistics.append("Distortion:\n");
        for(Pair<String,DistanceMatrix> tmp:matrices) {
            double pearson = visualization.computeDistortionAsPearson(tmp.m_b);
            double error = visualization.computeDistortionAsAvgError(tmp.m_b);
            statistics.append("- " + tmp.m_a + ":\n");
            statistics.append("   Pearson: " + pearson + "\n");
            statistics.append("   Avg.Error: " + error + "\n");
        }
 
    }      
    
    /*
    public void updateStatisticsDiscrete() {
        int cs = labelSetSelected1;
        if (cs==-1) cs = 0;
        int ms = controllerSelected;
        if (ms==-1) ms = 0;
        List<String> clustering = labelSets.get(cs).m_b;
        DistanceMatrix m = matrices.get(ms).m_b;
        
        double intra_accum = 0;
        double inter_accum = 0;
        int intra_count = 0;
        int inter_count = 0;
        
        int classificationMatrix[][] = null;      
        int classIndexes[] = new int[clustering.size()];
        List<String> classes = new LinkedList<String>();
        HashMap<String,Integer> counts = new HashMap<String, Integer>();
        for(String c:clustering) {
            Integer count = counts.get(c);
            if (count==null) {
                count = 0;
                classes.add(c);
            }
            count++;
            counts.put(c,count);
        }
        
       
        Collections.sort(classes, new VisualizationGUI.ClassesComparator(counts));

        for(int i = 0;i<clustering.size();i++) {
            classIndexes[i] = classes.indexOf(clustering.get(i));
        }

        
        double average = 0;
        for(String c:counts.keySet()) {
            average+=counts.get(c);
        }
        average/=classes.size();
        classificationMatrix = new int[classes.size()][classes.size()];
        
        for(int i = 0;i<clustering.size();i++) {
            int idx1 = classIndexes[i];
            if (idx1>=0) {
                int minimum_index = -1;
                double minimum = 0;
                for(int j = 0;j<clustering.size();j++) {
                    int idx2 = classIndexes[j];
                    if (idx2>=0) {
                        if (i!=j) {
                            if (idx1 == idx2) {
                                intra_accum+=m.get(i,j);
                                intra_count++;
                            } else {
                                inter_accum+=m.get(i,j);
                                inter_count++;                    
                            }
                            // Nearest Neighbor:
                            if (minimum_index==-1 || minimum>m.get(i,j)) {
                                minimum_index = j;
                                minimum = m.get(i,j);
                            }
                        }
                    }
                }
                classificationMatrix[idx1]
                                    [classIndexes[minimum_index]] ++;
    //            if (clustering.get(i).equals(clustering.get(minimum_index))) nearestNeighborCorrect++;
            }
        }  
        
        DecimalFormat df = new DecimalFormat("#.000"); 
        statistics.append("Matrix used: " + matrices.get(ms).m_a + "\n\n");
        statistics.append("Intra class distance: " + (intra_accum/intra_count) + "\n");
        statistics.append("Inter class distance: " + (inter_accum/inter_count) + "\n");
        double avgD = ((inter_accum+intra_accum)/(inter_count+intra_count));
        statistics.append("Avg distance: " + avgD + "\n");
        statistics.append("\n");
        for(int k = 1;k<=7;k+=2) {
            statistics.append(k + "-NN: " + df.format(knnAccuracy(k,null)) + "\n");
        }
        
        statistics.append("\nClasses: " + classes.size() + "\n");
        statistics.append(("Instances per class: " + average + "\n"));
        statistics.append("Class instances: \n");
        for(int i = 0;i<classes.size();i++) {
            statistics.append(" - " + classes.get(i) + " : " + counts.get(classes.get(i)) + "\n");
        }
        
        // class analysis:
        statistics.append("\nClass analysis: \n");
        for(int i = 0;i<classes.size();i++) {
            statistics.append(" - " + classes.get(i) + ":\n");
            for(int k = 1;k<=7;k+=2) {            
                Pair<Double,Double> tmp = avgDistanceClosestOfSameAndDifferentClass(i,k);
                statistics.append("   k="+k+" ( = / != ): (" + df.format(tmp.m_a) + " / " + df.format(tmp.m_b) + " )\n");
            }
        }

        // 1-vs-1:
        statistics.append("\n1-vs-1 analysis: \n");
        for(int label1 = 0;label1<classes.size();label1++) {
            for(int label2 = label1+1;label2<classes.size();label2++) {
                double correct = 0;
                double total = 0;
                for(int i = 0;i<clustering.size();i++) {
                    if (classIndexes[i]==label1 || classIndexes[i]==label2) {
                        int prediction = knnVoting1vs1(i, 1, m, classIndexes, classes.size(), label1, label2);
                        if (prediction == classIndexes[i]) correct++;
                        total++;
                    }
                }
                double max = Math.max(counts.get(classes.get(label1)), counts.get(classes.get(label2)));
                statistics.append("- "+classes.get(label1)+"-vs-"+classes.get(label2)+": "+df.format(correct/total)+" / "+df.format(max/total)+"\n");
            }
        }    
    }
    
    public int knnVoting(int instance, int k, DistanceMatrix m, int classIndexes[], int nClasses) {
        int retrieved[] = new int[k];
        int tmp = 0;
        
        for(int i = 0;i<m.names.length;i++) {
            if (i==instance) continue;
            if (tmp<k) {
                retrieved[tmp] = i;
                tmp++;
            } else {
                int worst = -1;
                for(int j = 0;j<k;j++) {
                    if (m.get(instance,retrieved[j])>
                        m.get(instance,i)) {
                        if (worst==-1 || 
                            m.get(instance,retrieved[j])>
                            m.get(instance,retrieved[worst])) {
                            worst = j;
                        }
                    }
                }
                if (worst!=-1) {
                    retrieved[worst] = i;
                }
            }
        }
        
        int votes[] = new int[nClasses];
        int max = -1;
        for(int i = 0;i<k;i++) {
            int v = classIndexes[retrieved[i]];
            votes[v]++;
            if (max==-1 || votes[v]>votes[max]) max = v;
        }
        
        return max;
    }
    */
    
    public static boolean areLabelsContinuous(List<String> labels) {
        for (String label : labels) {
            if (!IsNumber.isNumber(label)) {
                return false;
            }
        }
        return true;
    }
    
}
