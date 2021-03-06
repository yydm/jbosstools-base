/*******************************************************************************
 * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.tools.common.model.ui.attribute.editor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.MessageFormat;

import org.jboss.tools.common.model.ui.IAttributeErrorProvider;
import org.jboss.tools.common.model.ui.IValueChangeListener;
import org.jboss.tools.common.model.ui.IValueProvider;
import org.jboss.tools.common.model.ui.attribute.AttributeContentProposalProviderFactory;
import org.jboss.tools.common.model.ui.attribute.adapter.DefaultValueAdapter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.jboss.tools.common.meta.key.WizardKeys;
import org.jboss.tools.common.model.XModelObject;
import org.jboss.tools.common.model.ui.ModelUIPlugin;
import org.jboss.tools.common.model.ui.actions.IActionProvider;
import org.jboss.tools.common.model.ui.widgets.IWidgetSettings;
import org.jboss.tools.common.model.ui.widgets.WhiteSettings;

public class StringButtonFieldEditorEx extends StringButtonFieldEditor implements IFieldEditor, IPropertyChangeListener, PropertyChangeListener, IPropertyFieldEditor {
	public final static String BUTTON_SELECTED = "Button.Selected"; //$NON-NLS-1$
	protected PropertyEditorDialog editorDialog;
	protected IPropertyEditor propertyEditor; 
	protected IValueProvider valueProvider;
	protected IValueChangeListener valueChangeListener;
	protected Composite composite;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
//	private IAction buttonAction;

	public StringButtonFieldEditorEx() {
		this.setChangeButtonText(EditorMessages.getString("StringButtonFieldEditorEx_Browse")); //$NON-NLS-1$
	}
	
	public StringButtonFieldEditorEx(IWidgetSettings settings) {
		super(settings);
		this.setChangeButtonText(EditorMessages.getString("StringButtonFieldEditorEx_Browse")); //$NON-NLS-1$
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}
	public void firePropertyChange(java.beans.PropertyChangeEvent event) {
		pcs.firePropertyChange(event);
	}

	public void setPropertyEditor(IPropertyEditor propertyEditor) {
		this.propertyEditor = propertyEditor;
		valueProvider = (IValueProvider)propertyEditor.getAdapter(IValueProvider.class);
		valueChangeListener = (IValueChangeListener)propertyEditor.getAdapter(IValueChangeListener.class);
		setPropertyChangeListener(this);
		valueProvider.addValueChangeListener(this);
		IActionProvider actionProvider = (IActionProvider)propertyEditor.getAdapter(IActionProvider.class);
		setErrorProvider((IAttributeErrorProvider)propertyEditor.getAdapter(IAttributeErrorProvider.class));
		if (actionProvider != null) {
			if (getSettings() instanceof WhiteSettings) {
				setLabelAction(actionProvider.getAction(StringButtonFieldEditorEx.LABEL_SELECTED));
			} else {
				// none
			}
//			buttonAction = actionProvider.getAction(StringButtonFieldEditorEx.BUTTON_SELECTED);	
		}
	}

	public IPropertyEditor getPropertyEditor() {
		return propertyEditor;
	}

	protected String changePressed() {
		if (propertyEditor != null) {
			if(propertyEditor.getInput() instanceof DefaultValueAdapter) {
				((DefaultValueAdapter)propertyEditor.getInput()).fireEvent(BUTTON_SELECTED, "false", "true");
			}
			if(((PropertyEditor)propertyEditor).callsExternal()) {
				Object result = ((PropertyEditor)propertyEditor).callExternal(getShell());
				return result != null ? result.toString() : null;
			}
			editorDialog = new PropertyEditorDialog(ModelUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), propertyEditor);
			//editorDialog.create();
			//ExtendedFieldEditor fieldEditor = propertyEditor.getFieldEditor(null);
			DefaultValueAdapter adapter = (DefaultValueAdapter)propertyEditor.getInput();
			String title = MessageFormat.format("Edit {0}", propertyEditor.getAttributeName());
			if(adapter != null && adapter.getAttribute() != null) {
				String key = "" + adapter.getAttribute().getModelEntity().getName() + "." + adapter.getAttribute().getName().replace(' ', '_') + ".edit"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				String t = WizardKeys.getLabelText(key);
				if(t != null) {
					title = t;
				} else {
					title = MessageFormat.format("Edit {0}", WizardKeys.getAttributeDisplayName(adapter.getAttribute(), true));
				}
			}
			editorDialog.create();
			editorDialog.getShell().setText(title);
			adapter.setStoreLocked(true);
			int i = editorDialog.open();
			if (PropertyEditorDialog.OK == i) {
				adapter.setStoreLocked(false);
				adapter.store();
			} else {
				adapter.load();
				adapter.setStoreLocked(false);
			}
			return valueProvider.getStringValue(false);
		} else {
			java.beans.PropertyChangeEvent event = new java.beans.PropertyChangeEvent(this, BUTTON_SELECTED, Boolean.TRUE, Boolean.FALSE);
			firePropertyChange(event);
			return ""; //$NON-NLS-1$
		}
	}

	protected void doFillIntoGrid(Composite parent, int numColumns) {
		getLabelComposite(parent);
		Control control = getTextChangeControl(parent);
		control.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}
	
	protected Composite getTextChangeControl(Composite parent)
	{
		//if (composite == null) 
		createTextChangeControl(parent);
		return composite;
	}
	
	protected Control createTextChangeControl(Composite parent) {
		GridData gd;
		Control control;
		if(composite == null)
			composite = new Composite(parent, SWT.NONE);
		composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
///		composite.setBackground(parent.getBackground());
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		composite.setLayout(gridLayout);
		
		Control textControl = createTextControl(composite);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		textControl.setLayoutData(gd);

		control = new Label(composite, SWT.NONE);
///		control.setBackground(parent.getBackground());
		gd = new GridData();
		gd.widthHint = 5;
		control.setLayoutData(gd);
		
		control = getChangeControl(composite);
		gd = new GridData();
		gd.widthHint = convertHorizontalDLUsToPixels(control, IDialogConstants.BUTTON_WIDTH);
		gd.heightHint = textControl.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		control.setLayoutData(gd);
		
		// init data
		if (valueProvider!=null) {
			String value = valueProvider.getStringValue(Boolean.TRUE.booleanValue());
			getTextField().setText(value);
			if (getLabelAction()!=null) {
				getLabelAction().setEnabled((value!=null/* && value.length()>0*/));
			}
		}

// deprecated - now addContentAssist works
//		IContentAssistProcessor processor = (IContentAssistProcessor)propertyEditor.getAdapter(IContentAssistProcessor.class);
//		if(processor != null) {
//			ControlContentAssistHelper.createTextContentAssistant(getTextField(), processor);
//		}
		
		return composite;
	}

	protected void valueChanged() {
		super.valueChanged();
		if(isSelectableLabel() && getLabelControl() != null && !getLabelControl().isDisposed()) {
			boolean enabled = getLabelAction() != null && getLabelAction().isEnabled();
			getLabelControl().setToolTipText(enabled ? getHyperlinkLableToolTip() : null);
		}
	}

	// IPropertyChangeListener
	public void propertyChange(PropertyChangeEvent event) {
		if (valueChangeListener!=null) {
			if (ExtendedFieldEditor.VALUE.equals(event.getProperty())) {
				setPropertyChangeListener(null);
				Object oldValue = event.getOldValue();
				Object newValue = event.getNewValue();
				java.beans.PropertyChangeEvent e = new java.beans.PropertyChangeEvent(this, IPropertyEditor.VALUE, oldValue, newValue);
				valueChangeListener.valueChange(e);
				setPropertyChangeListener(this);
			}
		}
	}

	public void propertyChange(java.beans.PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (IPropertyEditor.VALUE.equals(evt.getPropertyName())) {
			Object v = evt.getNewValue();
			valueProvider.removeValueChangeListener(this);
			this.setStringValue((v == null) ? "" : v.toString()); //$NON-NLS-1$
			valueProvider.addValueChangeListener(this);
		}
	}

	public int getNumberOfControls() {
		return 2;
	}

	// IFieldEditor
	public Control[] getControls(Composite parent) {
		return new Control[] {getLabelComposite(parent), getTextChangeControl(parent)};
	}
	
	public void setEnabled(boolean enabled){
		super.setEnabled(enabled); // label
	}

	protected boolean isAlwaysReadOnly() {
		if(propertyEditor == null) return false;
		Object input = propertyEditor.getInput();
		if(input instanceof DefaultValueAdapter) {
			DefaultValueAdapter a = (DefaultValueAdapter)input;
			XModelObject o = a.getModelObject();
			if(o == null || o.isObjectEditable()) return false;
			while(o != null && o.getFileType() < XModelObject.FOLDER) {
				o = o.getParent();
			}
			if(o == null) return false;
			String entity = o.getModelEntity().getName();
			if(entity.indexOf("Jar") >= 0) return true; //$NON-NLS-1$
		}
		return false;
	}

	public void setStringValue(String value) {
		if(!isSameValue(value)) {
			super.setStringValue(value);
		}
	}

	boolean isSameValue(String newValue) {
		Text text = getTextField();
		if(text == null || text.isDisposed() || newValue == null) return false;
		String oldTextValue = text.getText();
		if(propertyEditor != null && propertyEditor.getInput() instanceof DefaultValueAdapter) {
			DefaultValueAdapter a = (DefaultValueAdapter)propertyEditor.getInput();
			if(a != null && a.getAttribute() != null && a.getAttribute().isTrimmable()) {
				return oldTextValue != null && oldTextValue.trim().equals(newValue.trim());
			}
		}
		return oldTextValue != null && oldTextValue.equals(newValue);
	}

	protected void addContentAssist(Text text) {
		if(propertyEditor != null && propertyEditor.getInput() instanceof DefaultValueAdapter) {
			DefaultValueAdapter valueAdapter = (DefaultValueAdapter)propertyEditor.getInput();
			AttributeContentProposalProviderFactory.registerContentAssist(valueAdapter, text);
		}
	}

}
