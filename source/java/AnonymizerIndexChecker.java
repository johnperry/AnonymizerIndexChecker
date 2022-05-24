/*---------------------------------------------------------------
*  Copyright 2016 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.aic;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import org.rsna.ui.*;
import org.rsna.util.*;
import org.apache.log4j.*;

public class AnonymizerIndexChecker extends JFrame implements ActionListener {

    String windowTitle = "Anonymizer Index Checker";
	Color background = new Color(0xc6d8f9);
	
	CheckPanel checkPanel;
	FooterPanel footerPanel;
	HeaderPanel headerPanel;

    public static void main(String args[]) {
		Logger.getRootLogger().addAppender(
				new ConsoleAppender(
					new PatternLayout("%d{HH:mm:ss} %-5p [%c{1}] %m%n")));
		Logger.getRootLogger().setLevel(Level.INFO);
        new AnonymizerIndexChecker();
    }

    public AnonymizerIndexChecker() {
		super();
		setTitle(windowTitle);
		JPanel mainPanel = new JPanel(new BorderLayout());
		checkPanel = new CheckPanel();
		headerPanel = new HeaderPanel("Check Indexes", 10, 10);
		footerPanel = new FooterPanel();
		footerPanel.checkIntegerTable.addActionListener(this);
		footerPanel.checkIndex.addActionListener(this);
		checkPanel = new CheckPanel();
		mainPanel.add(headerPanel, BorderLayout.NORTH);
		mainPanel.add(checkPanel, BorderLayout.CENTER);
		mainPanel.add(footerPanel, BorderLayout.SOUTH);
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });

        pack();
        centerFrame();
        setVisible(true);
        int option = JOptionPane.showConfirmDialog(this, "Make sure Anonymizer is not running", "Alert", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.CANCEL_OPTION) System.exit(0);
	}

	/**
	 * Implementation of the ActionListener for the s button.
	 * @param event the event.
	 */
    public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source.equals(footerPanel.checkIntegerTable)) {
			checkPanel.checkIntegerTable();
		}
		else if (source.equals(footerPanel.checkIndex)) {
			checkPanel.checkIndex();
		}
	}

	class CheckPanel extends JPanel {
		JScrollPane jsp;
		ColorPane cp;
		int margin = 15;
		JLabel sts;

		public CheckPanel() {
			super();
			setBackground(background);
			setLayout(new BorderLayout());
			jsp = new JScrollPane();
			add(jsp, BorderLayout.CENTER);
			cp = new ColorPane();
			jsp.setViewportView(cp);
			Box ftr = Box.createHorizontalBox();
			sts = new JLabel("  ");
			ftr.add(Box.createHorizontalStrut(10));
			ftr.add(sts);
			add(ftr, BorderLayout.SOUTH);
		}
		public void checkIntegerTable() {
			cp.clear();
			new IntegerTableChecker(cp, footerPanel.makeBackup.isSelected(), sts).start();
		}
		public void checkIndex() {
			cp.clear();
			new IndexChecker(cp, footerPanel.makeBackup.isSelected(), sts).start();
		}
	}
			
	class HeaderPanel extends Panel {
		public HeaderPanel(String title, int marginTop, int marginBottom) {
			super();
			setBackground(background);
			Box box = Box.createVerticalBox();
			JLabel panelTitle = new JLabel(title);
			panelTitle.setFont( new java.awt.Font( "SansSerif", java.awt.Font.BOLD, 18 ) );
			panelTitle.setForeground(Color.BLUE);
			box.add(Box.createVerticalStrut(marginTop));
			box.add(panelTitle);
			box.add(Box.createVerticalStrut(marginBottom));
			this.add(box);
		}		
	}

	class FooterPanel extends JPanel {
		public JButton checkIntegerTable;
		public JButton checkIndex;
		public JCheckBox makeBackup;
		public FooterPanel() {
			super();
			setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createBevelBorder(BevelBorder.LOWERED),
				BorderFactory.createEmptyBorder(2, 2, 2, 2)
			));
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setBackground(background);
			checkIntegerTable = new JButton(" Check IntegerTable");
			checkIndex = new JButton(" Check Index");
			makeBackup = new JCheckBox("Make Filtered Backup");
			makeBackup.setBackground(background);
			add(Box.createHorizontalStrut(15));
			add(checkIntegerTable);
			add(Box.createHorizontalStrut(15));
			add(checkIndex);
			add(Box.createHorizontalGlue());
			add(makeBackup);
			add(Box.createHorizontalStrut(15));
		}
	}
	
    private void centerFrame() {
        Toolkit t = getToolkit();
        Dimension scr = t.getScreenSize ();
        setSize(scr.width/2, scr.height/2);
        setLocation (new Point ((scr.width-getSize().width)/2,
                                (scr.height-getSize().height)/2));
    }

}
