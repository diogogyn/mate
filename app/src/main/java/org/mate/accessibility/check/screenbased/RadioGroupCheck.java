package org.mate.accessibility.check.screenbased;

import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.AccessibilityViolationTypes;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

public class RadioGroupCheck implements IScreenAccessibilityCheck {
    @Override
    public AccessibilityViolation check(IScreenState state) {

        int radioButtonCount = 0;
        int radioGroupCount = 0;
        for (Widget widget: state.getWidgets()){
            if (widget.getClazz().contains("RadioButton")){
                radioButtonCount++;
            }

            if (widget.getClazz().contains("RadioGroup")){
                radioGroupCount++;
            }
        }

        if (radioButtonCount>0 && radioGroupCount==0)
            return new AccessibilityViolation(AccessibilityViolationTypes.RADIOGROUPCHECK,state,"");

        return null;
    }
}