package com.iv.logView.ui;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.TabExpander;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

class NoWrapEditorKit extends StyledEditorKit {

    public ViewFactory getViewFactory() {
        return new StyledViewFactory();
    }

    static class StyledViewFactory implements ViewFactory {

        public View create(Element elem) {
            String kind = elem.getName();

            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new MyLabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new ParagraphView(elem);
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new NoWrapBoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }

            return new LabelView(elem);
        }
    }

    static class NoWrapBoxView extends BoxView {
        public NoWrapBoxView(Element elem, int axis) {
            super(elem, axis);
        }

        public void layout(int width, int height) {
            super.layout(32768, height);
        }

        public float getMinimumSpan(int axis) {
            return super.getPreferredSpan(axis);
        }
    }

    static class MyLabelView extends LabelView {
        public MyLabelView(Element elem) {
            super(elem);
        }

        public float getPreferredSpan(int axis) {
            if (axis == View.X_AXIS) {
                TabExpander ex = getTabExpander();

                if (ex == null) {
                    //paragraph implements TabExpander
                    ex = (TabExpander) this.getParent().getParent();
                    getTabbedSpan(0, ex);
                }
            }

            return super.getPreferredSpan(axis);
        }
    }
}
