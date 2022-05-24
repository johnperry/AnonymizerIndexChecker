/*---------------------------------------------------------------
*  Copyright 2022 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.aic;

import java.awt.Color;
import javax.swing.*;
import java.io.*;
import java.util.*;
import org.rsna.util.FileUtil;

import org.apache.log4j.*;

import org.rsna.ui.ColorPane;
import org.rsna.util.JdbmUtil;
import jdbm.RecordManager;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;

/**
 * A Thread for checking/fixing the Index.
 */
public class IndexChecker extends Thread {

	static final Logger logger = Logger.getLogger(IndexChecker.class);
	
	ColorPane cp;
	boolean backup;
	JLabel sts;
	File data;
	String[] tableNames = { "fwdPatientIndex", "invPatientIndex", "fwdStudyIndex", "uidIndex" };


	/**
	 * Class constructor.
	 */
    public IndexChecker(ColorPane cp, boolean backup, JLabel sts) {
		super();
		this.cp = cp;
		this.backup = backup;
		this.sts = sts;
	}
	
	public void run() {
		data = new File("data");
		try {
			//Original table to scan
			File dbOriginal = new File(data, "index");
			RecordManager recmanOriginal = JdbmUtil.getRecordManager( dbOriginal.getAbsolutePath() );
			HTree tableOriginal = null;
			
			//Create backup index if selected
			RecordManager recmanBackup = null;
			HTree tableBackup = null;
			if (backup) {
				//First delete the old backup
				new File(data, "index-backup.db").delete();
				new File(data, "index-backup.lg").delete();
				//Now create the new one
				File indexBackup = new File(data, "index-backup");
				recmanBackup = JdbmUtil.getRecordManager( indexBackup.getAbsolutePath() );
			}
			
			cp.println(Color.black, "Checking index");
			for (String tname : tableNames) {
				cp.println(Color.black, "Checking "+tname);
				tableOriginal = JdbmUtil.getHTree( recmanOriginal, tname );
				if (backup) {
					tableBackup = JdbmUtil.getHTree( recmanBackup, tname );
					if (tableBackup == null) {
						logger.warn("Unable to create the "+tname+" table backup");
						return;
					}
				}
				
				if (tableOriginal != null) {
					FastIterator fit = tableOriginal.keys();
					int count = 0;
					int errorCount = 0;
					int nullCount = 0;
					String key = "notnull";
					while (key != null) {
						try {
							key = (String)fit.next();
							if (key != null) {
								Object value = tableOriginal.get(key);
								if (value != null) {
									count++;
									String text = count + ": " + key + ": " + value;
									showSTS(text);
									if (recmanBackup != null) {
										tableBackup.put(key, value);
										if (count%40 == 0) recmanBackup.commit();
									}
								}
								else {
									nullCount++;
									cp.println("Null value for "+key);
								}
							}
						}
						catch (Throwable x) {
							errorCount++;
							String msg = null;
							if (x instanceof Exception) {
								msg = "Exception "+errorCount+" / "+count + ": "+x.getMessage();
							}
							else if (x instanceof Error) {
								msg = "Error "+errorCount+" / "+count + ": "+x.getMessage();
							}
							cp.println(msg);
							System.out.println(msg);
							key = "notnull";
						}
					}
					cp.println(Color.black, count + " non-null entries found and retrieved");
					cp.println(Color.black, nullCount + " null entries found");
					cp.println(Color.black, errorCount + " errors found");
					cp.println(Color.black, "Finished checking "+tname+"\n");
				}
				else cp.println(tname+" table not found");
			}
			if (recmanBackup != null) {
				recmanBackup.commit();
				recmanBackup.close();
			}
			recmanOriginal.commit();
			recmanOriginal.close();
		}
		catch (Exception ex) {
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			cp.println(Color.red, sw.toString());
		}
	}
	
	void showSTS(String msg) {
		final String msgFinal = msg;
		final JLabel stsFinal = sts;
		Runnable r = new Runnable() {
			public void run() {
				stsFinal.setText(msgFinal);
			}
		};
		SwingUtilities.invokeLater(r);
	}
}