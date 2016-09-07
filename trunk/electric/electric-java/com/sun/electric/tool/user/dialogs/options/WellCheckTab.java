/* -*- tab-width: 4 -*-
 *
 * Electric(tm) VLSI Design System
 *
 * File: WellCheckTab.java
 *
 * Copyright (c) 2004, Static Free Software. All rights reserved.
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
package com.sun.electric.tool.user.dialogs.options;

import javax.swing.JPanel;

import com.sun.electric.tool.erc.ERCWellCheck;
import com.sun.electric.tool.user.dialogs.PreferencesFrame;
import com.sun.electric.util.TextUtils;

/**
 * Class to handle the "Well Check" tab of the Preferences dialog.
 */
public class WellCheckTab extends PreferencePanel {
	/** Creates new form WellCheckTab */
	public WellCheckTab(PreferencesFrame parent, boolean modal) {
		super(parent, modal);
		initComponents();
	}

	/** return the panel to use for user preferences. */
	@Override
	public JPanel getUserPreferencesPanel() {
		return wellCheck;
	}

	/** return the name of this preferences tab. */
	@Override
	public String getName() {
		return "Well Check";
	}

	/**
	 * Method called at the start of the dialog. Caches current values and
	 * displays them in the Well Check tab.
	 */
	@Override
	public void init() {
		ERCWellCheck.WellCheckPreferences wcp = new ERCWellCheck.WellCheckPreferences(false);

		switch (wcp.pWellCheck) {
		case 0:
			wellPMustHaveAllContacts.setSelected(true);
			break;
		case 1:
			wellPMustHave1Contact.setSelected(true);
			break;
		case 2:
			wellPNoContactCheck.setSelected(true);
			break;
		}
		wellPMustConnectGround.setSelected(wcp.mustConnectPWellToGround);

		switch (wcp.nWellCheck) {
		case 0:
			wellNMustHaveAllContacts.setSelected(true);
			break;
		case 1:
			wellNMustHave1Contact.setSelected(true);
			break;
		case 2:
			wellNNoContactCheck.setSelected(true);
			break;
		}
		wellNMustConnectPower.setSelected(wcp.mustConnectNWellToPower);

		wellFindFarthestDistance.setSelected(wcp.findWorstCaseWell);
		drcCheck.setSelected(wcp.drcCheck);
		multiProc.setSelected(wcp.parallelWellAnalysis);
		numProcs.setText(Integer.toString(wcp.maxProc));
	}

	/**
	 * Method called when the "OK" panel is hit. Updates any changed fields in
	 * the Well Check tab.
	 */
	@Override
	public void term() {
		ERCWellCheck.WellCheckPreferences wcp = new ERCWellCheck.WellCheckPreferences(false);

		int currentPWellRule = 0;
		if (wellPMustHave1Contact.isSelected())
			currentPWellRule = 1;
		else if (wellPNoContactCheck.isSelected())
			currentPWellRule = 2;
		wcp.pWellCheck = currentPWellRule;
		wcp.mustConnectPWellToGround = wellPMustConnectGround.isSelected();

		int currentNWellRule = 0;
		if (wellNMustHave1Contact.isSelected())
			currentNWellRule = 1;
		else if (wellNNoContactCheck.isSelected())
			currentNWellRule = 2;
		wcp.nWellCheck = currentNWellRule;
		wcp.mustConnectNWellToPower = wellNMustConnectPower.isSelected();

		wcp.findWorstCaseWell = wellFindFarthestDistance.isSelected();
		wcp.drcCheck = drcCheck.isSelected();

		wcp.parallelWellAnalysis = multiProc.isSelected();
		wcp.maxProc = TextUtils.atoi(numProcs.getText());

		putPrefs(wcp);
	}

	/**
	 * Method called when the factory reset is requested.
	 */
	@Override
	public void reset() {
		putPrefs(new ERCWellCheck.WellCheckPreferences(true));
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed"
	// desc=" Generated Code ">//GEN-BEGIN:initComponents
	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		wellCheckPWell = new javax.swing.ButtonGroup();
		wellCheckNWell = new javax.swing.ButtonGroup();
		wellCheck = new javax.swing.JPanel();
		jPanel1 = new javax.swing.JPanel();
		wellPMustConnectGround = new javax.swing.JCheckBox();
		jPanel3 = new javax.swing.JPanel();
		wellPNoContactCheck = new javax.swing.JRadioButton();
		wellPMustHave1Contact = new javax.swing.JRadioButton();
		wellPMustHaveAllContacts = new javax.swing.JRadioButton();
		jPanel2 = new javax.swing.JPanel();
		wellNMustConnectPower = new javax.swing.JCheckBox();
		jPanel4 = new javax.swing.JPanel();
		wellNMustHaveAllContacts = new javax.swing.JRadioButton();
		wellNMustHave1Contact = new javax.swing.JRadioButton();
		wellNNoContactCheck = new javax.swing.JRadioButton();
		jPanel5 = new javax.swing.JPanel();
		drcCheck = new javax.swing.JCheckBox();
		multiProc = new javax.swing.JCheckBox();
		numProcs = new javax.swing.JTextField();
		wellFindFarthestDistance = new javax.swing.JCheckBox();
		jLabel1 = new javax.swing.JLabel();

		getContentPane().setLayout(new java.awt.GridBagLayout());

		setTitle("Tool Options");
		setName("");
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		wellCheck.setLayout(new java.awt.GridBagLayout());

		jPanel1.setLayout(new java.awt.GridBagLayout());

		jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("For P-Well"));
		wellPMustConnectGround.setText("Must connect to Ground");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel1.add(wellPMustConnectGround, gridBagConstraints);

		jPanel3.setLayout(new java.awt.GridBagLayout());

		jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		wellCheckPWell.add(wellPNoContactCheck);
		wellPNoContactCheck.setText("Do not check for contacts");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(wellPNoContactCheck, gridBagConstraints);

		wellCheckPWell.add(wellPMustHave1Contact);
		wellPMustHave1Contact.setText("Must have at least 1 contact");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(wellPMustHave1Contact, gridBagConstraints);

		wellCheckPWell.add(wellPMustHaveAllContacts);
		wellPMustHaveAllContacts.setText("Must have contact in every area");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(wellPMustHaveAllContacts, gridBagConstraints);

		jPanel1.add(jPanel3, new java.awt.GridBagConstraints());

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		wellCheck.add(jPanel1, gridBagConstraints);
		jPanel1.getAccessibleContext().setAccessibleDescription("");

		jPanel2.setLayout(new java.awt.GridBagLayout());

		jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("For N-Well"));
		wellNMustConnectPower.setText("Must connect to Power");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel2.add(wellNMustConnectPower, gridBagConstraints);

		jPanel4.setLayout(new java.awt.GridBagLayout());

		jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		wellCheckNWell.add(wellNMustHaveAllContacts);
		wellNMustHaveAllContacts.setText("Must have contact in every area");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel4.add(wellNMustHaveAllContacts, gridBagConstraints);

		wellCheckNWell.add(wellNMustHave1Contact);
		wellNMustHave1Contact.setText("Must have at least 1 contact");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel4.add(wellNMustHave1Contact, gridBagConstraints);

		wellCheckNWell.add(wellNNoContactCheck);
		wellNNoContactCheck.setText("Do not check for contacts");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel4.add(wellNNoContactCheck, gridBagConstraints);

		jPanel2.add(jPanel4, new java.awt.GridBagConstraints());

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		wellCheck.add(jPanel2, gridBagConstraints);

		jPanel5.setLayout(new java.awt.GridBagLayout());

		drcCheck.setText("Check DRC Spacing Rules for Wells");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
		gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel5.add(drcCheck, gridBagConstraints);

		multiProc.setText("Use multiple processors, maximum:");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 1);
		jPanel5.add(multiProc, gridBagConstraints);

		numProcs.setColumns(6);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.insets = new java.awt.Insets(4, 1, 4, 1);
		jPanel5.add(numProcs, gridBagConstraints);

		wellFindFarthestDistance.setText("Find farthest distance from contact to edge");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
		gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel5.add(wellFindFarthestDistance, gridBagConstraints);

		jLabel1.setText("(0 to use all)");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(4, 1, 4, 4);
		jPanel5.add(jLabel1, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		wellCheck.add(jPanel5, gridBagConstraints);

		getContentPane().add(wellCheck, new java.awt.GridBagConstraints());

		pack();
	}// </editor-fold>//GEN-END:initComponents

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt)// GEN-FIRST:event_closeDialog
	{
		setVisible(false);
		dispose();
	}// GEN-LAST:event_closeDialog

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JCheckBox drcCheck;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JCheckBox multiProc;
	private javax.swing.JTextField numProcs;
	private javax.swing.JPanel wellCheck;
	private javax.swing.ButtonGroup wellCheckNWell;
	private javax.swing.ButtonGroup wellCheckPWell;
	private javax.swing.JCheckBox wellFindFarthestDistance;
	private javax.swing.JCheckBox wellNMustConnectPower;
	private javax.swing.JRadioButton wellNMustHave1Contact;
	private javax.swing.JRadioButton wellNMustHaveAllContacts;
	private javax.swing.JRadioButton wellNNoContactCheck;
	private javax.swing.JCheckBox wellPMustConnectGround;
	private javax.swing.JRadioButton wellPMustHave1Contact;
	private javax.swing.JRadioButton wellPMustHaveAllContacts;
	private javax.swing.JRadioButton wellPNoContactCheck;
	// End of variables declaration//GEN-END:variables

}
