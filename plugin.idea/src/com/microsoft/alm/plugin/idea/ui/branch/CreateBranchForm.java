// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.ui.branch;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.microsoft.alm.plugin.idea.ui.common.forms.BasicForm;
import git4idea.GitRemoteBranch;
import org.apache.commons.lang.StringUtils;

import javax.swing.AbstractButton;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

/**
 * This form is for creating a new branch from an existing branch that is selected
 */
public class CreateBranchForm implements BasicForm {
    private JPanel contentPanel;
    private JLabel nameLabel;
    private JTextField nameTextField;
    private JComboBox remoteBranchComboBox;
    private JLabel basedOn;
    private JCheckBox checkoutBranch;
    private boolean initialized = false;

    public CreateBranchForm() {
        $$$setupUI$$$();
    }

    public JPanel getContentPanel() {
        ensureInitialized();
        return contentPanel;
    }

    private void ensureInitialized() {
        if (!this.initialized) {
            // override the renderer so it doesn't show the object toString but instead shows the branch name
            remoteBranchComboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    return super.getListCellRendererComponent(list,
                            value != null ? ((GitRemoteBranch) value).getName() : StringUtils.EMPTY,
                            index,
                            isSelected,
                            cellHasFocus);
                }
            });

            this.initialized = true;
        }
    }

    /**
     * Always focus on branch name text field
     */
    @Override
    public JComponent getPreferredFocusedComponent() {
        return nameTextField;
    }

    @Override
    public void addActionListener(ActionListener listener) {
        remoteBranchComboBox.addActionListener(listener);
    }

    public void setRemoteBranchDropdownModel(final ComboBoxModel model) {
        if (model != null) {
            remoteBranchComboBox.setModel(model);
        }
    }

    public GitRemoteBranch getSelectedRemoteBranch() {
        final Object item = remoteBranchComboBox.getSelectedItem();
        if (item instanceof GitRemoteBranch) {
            return (GitRemoteBranch) remoteBranchComboBox.getSelectedItem();
        }

        return null;
    }

    public void setSelectedRemoteBranch(final GitRemoteBranch targetBranch) {
        if (targetBranch != null && remoteBranchComboBox != null) {
            remoteBranchComboBox.setSelectedItem(targetBranch);
        }
    }

    public void setBranchName(final String branchName) {
        nameTextField.setText(branchName);
    }

    public String getBranchName() {
        return StringUtils.trim(nameTextField.getText());
    }

    public void setCheckoutBranch(boolean checkoutBranch) {
        this.checkoutBranch.setSelected(checkoutBranch);

    }

    public boolean getCheckoutBranch() {
        return checkoutBranch.isSelected();
    }

    private JComponent getBranchNameComponent() {
        return nameTextField;
    }

    public JComponent getComponent(final String name) {
        if (CreateBranchModel.PROP_BRANCH_NAME.equals(name)) {
            return getBranchNameComponent();
        }

        if (CreateBranchModel.PROP_SELECTED_REMOTE_BRANCH.equals(name)) {
            return remoteBranchComboBox;
        }

        return null;
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1));
        nameLabel = new JLabel();
        this.$$$loadLabelText$$$(nameLabel, ResourceBundle.getBundle("com/microsoft/alm/plugin/idea/ui/tfplugin").getString("CreateBranchDialog.NameLabel"));
        contentPanel.add(nameLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        remoteBranchComboBox = new JComboBox();
        contentPanel.add(remoteBranchComboBox, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameTextField = new JTextField();
        contentPanel.add(nameTextField, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        basedOn = new JLabel();
        this.$$$loadLabelText$$$(basedOn, ResourceBundle.getBundle("com/microsoft/alm/plugin/idea/ui/tfplugin").getString("CreateBranchDialog.BasedOn"));
        contentPanel.add(basedOn, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkoutBranch = new JCheckBox();
        checkoutBranch.setSelected(true);
        this.$$$loadButtonText$$$(checkoutBranch, ResourceBundle.getBundle("com/microsoft/alm/plugin/idea/ui/tfplugin").getString("CreateBranchDialog.CheckoutBranch"));
        contentPanel.add(checkoutBranch, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }
}
