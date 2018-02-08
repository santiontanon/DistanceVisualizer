/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datavisualizer.threed.multiview;

import datavisualizer.threed.Visualization3D;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Checkbox;
import java.util.*;
import javax.swing.*;
import util.IsNumber;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author santi
 */
public class Visualization3DMultiViewGUIView extends JPanel {

    Visualization3DMultiViewGUI m_parent = null;
    Visualization3D m_visualization = null;

    JComboBox labelSetSelectionBox1 = null;
    JComboBox labelSetSelectionBox2 = null;
    JComboBox labelSetSelectionBox3 = null;
    Checkbox labelSetSelectionBox1reverse = null;
    Checkbox labelSetSelectionBox2reverse = null;
    Checkbox labelSetSelectionBox3reverse = null;

    // only for discrete labels (select which ones to see):
    List<String> allLabelsSelected = new LinkedList<>();
    List<String> allLabelsIgnored = new LinkedList<>();

    JSpinner pointSizeChooser = null;
    JPanel inputpanel = null;

    int labelSetSelected1 = 0;
    int labelSetSelected2 = 0;
    int labelSetSelected3 = 0;
    boolean reverse1 = false;
    boolean reverse2 = false;
    boolean reverse3 = false;
    boolean currentLabelsContinuous1 = false;
    boolean currentLabelsContinuous2 = false;
    boolean currentLabelsContinuous3 = false;

    public Visualization3DMultiViewGUIView(Visualization3DMultiViewGUI parent, int WIDTH) {
        m_parent = parent;
        
        labelSetSelected1 = 0;
        labelSetSelected2 = 0;
        labelSetSelected3 = 0;
        currentLabelsContinuous1 = areLabelsContinuous(m_parent.labelSets.get(labelSetSelected1).m_b);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Error setting native LAF: " + e);
        }

        setBackground(Color.WHITE);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        m_visualization = new Visualization3D(m_parent.visualization);
        m_visualization.setParent(m_parent.visualization);
        m_visualization.setPreferredSize(new Dimension(WIDTH, Visualization3DMultiViewGUI.W_HEIGHT));
        add(m_visualization);

        {
            inputpanel = new JPanel();
            inputpanel.setLayout(new BoxLayout(inputpanel, BoxLayout.Y_AXIS));

            {
                JPanel p = new JPanel();
                p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
                SpinnerNumberModel pintSizeModel = new SpinnerNumberModel(8, 1, 100, 1);
                pointSizeChooser = new JSpinner(pintSizeModel);
                p.add(new JLabel("Point Size:"));
                p.add(pointSizeChooser);
                inputpanel.add(p);
                pointSizeChooser.setMaximumSize(new Dimension(WIDTH, 24));
                pointSizeChooser.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        m_visualization.m_base_case_size = (Integer) pointSizeChooser.getValue();
                    }
                });
            }

            inputpanel.add(new JLabel("Select Labels (up to 3):"));

            String[] labelNames = new String[m_parent.labelSets.size() + 1];
            labelNames[0] = null;
            for (int i = 0; i < m_parent.labelSets.size(); i++) {
                labelNames[i + 1] = m_parent.labelSets.get(i).m_a;
            }

            {
                JPanel p = new JPanel();
                p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
                labelSetSelectionBox1 = new JComboBox(labelNames);
                labelSetSelectionBox1.setPreferredSize(new Dimension(WIDTH, 32));
                p.add(labelSetSelectionBox1);
                labelSetSelectionBox1reverse = new Checkbox("inv");
                p.add(labelSetSelectionBox1reverse);
                inputpanel.add(p);
            }

            {
                JPanel p = new JPanel();
                p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
                labelSetSelectionBox2 = new JComboBox(labelNames);
                labelSetSelectionBox2.setPreferredSize(new Dimension(WIDTH, 32));
                p.add(labelSetSelectionBox2);
                labelSetSelectionBox2reverse = new Checkbox("inv");
                p.add(labelSetSelectionBox2reverse);
                inputpanel.add(p);
            }

            {
                JPanel p = new JPanel();
                p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
                labelSetSelectionBox3 = new JComboBox(labelNames);
                labelSetSelectionBox3.setPreferredSize(new Dimension(WIDTH, 32));
                inputpanel.add(labelSetSelectionBox3);
                p.add(labelSetSelectionBox3);
                labelSetSelectionBox3reverse = new Checkbox("inv");
                p.add(labelSetSelectionBox3reverse);
                inputpanel.add(p);
            }

            add(inputpanel);
        }
    }


    public void changeLabelColor(String label) {
        System.out.println("changing color of " + label);
        JFrame dialogue = new JFrame("Change Color for " + label);
        dialogue.setPreferredSize(new Dimension(100, 140));
        dialogue.setSize(100, 140);
        dialogue.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        Color currentColor = m_parent.visualization.labelColors.get(label);
        JTextField rtext = new JTextField("" + currentColor.getRed());
        JTextField gtext = new JTextField("" + currentColor.getGreen());
        JTextField btext = new JTextField("" + currentColor.getBlue());
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

                    m_parent.visualization.labelColors.put(label, new Color(r, g, b));
                } catch (Exception ex) {

                }
            }
        });
        panel.add(set);
        dialogue.add(panel);
        dialogue.setVisible(true);
    }


    public static boolean areLabelsContinuous(List<String> labels) {
        for (String label : labels) {
            if (label!=null && !IsNumber.isNumber(label)) {
                return false;
            }
        }
        return true;
    }

    public void update() {
        m_visualization.syncWith(m_parent.visualization);
        m_visualization.modifiedByMouseListener = false;
        
        if (labelSetSelectionBox1 != null) {
            int selected1 = labelSetSelectionBox1.getSelectedIndex();
            int selected2 = labelSetSelectionBox2.getSelectedIndex();
            int selected3 = labelSetSelectionBox3.getSelectedIndex();
            if (selected1 != labelSetSelected1
                    || selected2 != labelSetSelected2
                    || selected3 != labelSetSelected3
                    || reverse1 != labelSetSelectionBox1reverse.getState()
                    || reverse2 != labelSetSelectionBox2reverse.getState()
                    || reverse3 != labelSetSelectionBox3reverse.getState()) {
                labelSetSelected1 = selected1;
                labelSetSelected2 = selected2;
                labelSetSelected3 = selected3;
                reverse1 = labelSetSelectionBox1reverse.getState();
                reverse2 = labelSetSelectionBox2reverse.getState();
                reverse3 = labelSetSelectionBox3reverse.getState();
                currentLabelsContinuous1 = false;
                currentLabelsContinuous2 = false;
                currentLabelsContinuous3 = false;
                if (labelSetSelected1 > 0) {
                    currentLabelsContinuous1 = areLabelsContinuous(m_parent.labelSets.get(labelSetSelected1 - 1).m_b);
                    if (currentLabelsContinuous1) {
                        allLabelsSelected.clear();
                    } else {
                        allLabelsSelected = new LinkedList<>();
                        for(String l:m_parent.labelSets.get(labelSetSelected1 - 1).m_b) {
                            if (!allLabelsSelected.contains(l)) allLabelsSelected.add(l);
                        }
                    }
                }
                if (labelSetSelected2 > 0) {
                    currentLabelsContinuous2 = areLabelsContinuous(m_parent.labelSets.get(labelSetSelected2 - 1).m_b);
                }
                if (labelSetSelected3 > 0) {
                    currentLabelsContinuous3 = areLabelsContinuous(m_parent.labelSets.get(labelSetSelected3 - 1).m_b);
                }           
    
                m_visualization.setClasses(labelSetSelected1 > 0 ? m_parent.labelSets.get(labelSetSelected1 - 1).m_b : null,
                        labelSetSelected2 > 0 ? m_parent.labelSets.get(labelSetSelected2 - 1).m_b : null,
                        labelSetSelected3 > 0 ? m_parent.labelSets.get(labelSetSelected3 - 1).m_b : null,
                        allLabelsSelected,
                        currentLabelsContinuous1,
                        currentLabelsContinuous2,
                        currentLabelsContinuous3,
                        reverse1,
                        reverse2,
                        reverse3);
            }
        }
        
        m_visualization.setPreferredSize(new Dimension(m_parent.getWidth()/m_parent.views.size(), m_parent.getHeight()));
        repaint();
    }

}
