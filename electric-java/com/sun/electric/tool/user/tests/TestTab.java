		/* -*- tab-width: 4 -*-
 *
 * Electric(tm) VLSI Design System
 *
 * File: TestTab.java
 *
 * Copyright (c) 2006, Static Free Software. All rights reserved.
 *
 * Electric(tm) is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Electric(tm) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sun.electric.tool.user.tests;

import com.sun.electric.tool.user.User;
import com.sun.electric.tool.user.dialogs.OpenFile;
import com.sun.electric.tool.user.dialogs.PreferencesFrame;
import com.sun.electric.tool.user.dialogs.options.PreferencePanel;

import javax.swing.JPanel;

public class TestTab extends PreferencePanel
{
	/** Creates new form TestTab */
	public TestTab(PreferencesFrame parent, Boolean modal)
	{
		super(parent, modal.booleanValue());
		initComponents();
	}

	/** return the panel to use for user preferences. */
	public JPanel getUserPreferencesPanel() { return testPanel; }

	/** return the name of this preferences tab. */
	public String getName() { return "Tests"; }

	/**
	 * Method called at the start of the dialog.
	 * Caches current values and displays them in the CDL tab.
	 */
	public void init() { testRegressionText.setText(User.getRegressionPath()); }

	/**
	 * Method called when the "OK" panel is hit.
	 * Updates any changed fields in the CDL tab.
	 */
	public void term() { User.setRegressionPath(testRegressionText.getText()); }

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		testPanel = new javax.swing.JPanel();
		testRegressionLabel = new javax.swing.JLabel();
		browser = new javax.swing.JButton();
		testRegressionText = new javax.swing.JLabel();

		getContentPane().setLayout(new java.awt.GridBagLayout());

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		testPanel.setLayout(new java.awt.GridBagLayout());

		testRegressionLabel.setText("Regression Path: ");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
		testPanel.add(testRegressionLabel, gridBagConstraints);

		browser.setText("Set");
		browser.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
		browser.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				browserActionPerformed(evt);
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		testPanel.add(browser, gridBagConstraints);

		testRegressionText.setText("<set me up>");
		testPanel.add(testRegressionText, new java.awt.GridBagConstraints());

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		getContentPane().add(testPanel, gridBagConstraints);

		pack();
	}// </editor-fold>//GEN-END:initComponents

	private void browserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browserActionPerformed
		String dirName = OpenFile.chooseDirectory("Regression Root Directory");
		if (dirName == null) return;
		testRegressionText.setText(dirName);
	}//GEN-LAST:event_browserActionPerformed

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton browser;
	private javax.swing.JPanel testPanel;
	private javax.swing.JLabel testRegressionLabel;
	private javax.swing.JLabel testRegressionText;
	// End of variables declaration//GEN-END:variables
}
