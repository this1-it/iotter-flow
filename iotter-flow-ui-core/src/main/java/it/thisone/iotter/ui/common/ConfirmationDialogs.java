package it.thisone.iotter.ui.common;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

public final class ConfirmationDialogs {

    private static final String OVERLAY_CLASS_NAME = "iotter-confirm-dialog";
    private static final String DANGER_CONFIRM_THEME = "error primary";

    private ConfirmationDialogs() {
    }

    public static ConfirmDialog create(Component source, String header, String text, Runnable onConfirm) {
        return create(header, text,
                source.getTranslation("basic.editor.yes"),
                source.getTranslation("basic.editor.no"),
                onConfirm);
    }

    public static ConfirmDialog create(Component source, String header, Component content, Runnable onConfirm) {
        return create(header, content,
                source.getTranslation("basic.editor.yes"),
                source.getTranslation("basic.editor.no"),
                onConfirm);
    }

    public static ConfirmDialog create(String header, String text, String confirmText, String cancelText,
            Runnable onConfirm) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(header);
        dialog.setText(text);
        return configure(dialog, confirmText, cancelText, onConfirm);
    }

    public static ConfirmDialog create(String header, Component content, String confirmText, String cancelText,
            Runnable onConfirm) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(header);
        dialog.setText(content);
        return configure(dialog, confirmText, cancelText, onConfirm);
    }

    public static ConfirmDialog open(Component source, String header, String text, Runnable onConfirm) {
        ConfirmDialog dialog = create(source, header, text, onConfirm);
        dialog.open();
        return dialog;
    }

    public static ConfirmDialog open(Component source, String header, Component content, Runnable onConfirm) {
        ConfirmDialog dialog = create(source, header, content, onConfirm);
        dialog.open();
        return dialog;
    }

    public static ConfirmDialog createDanger(Component source, String header, String text, Runnable onConfirm) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(header);
        dialog.setText(text);
        return configure(dialog, source.getTranslation("basic.editor.yes"), source.getTranslation("basic.editor.no"),
                DANGER_CONFIRM_THEME, onConfirm);
    }

    public static ConfirmDialog openDanger(Component source, String header, String text, Runnable onConfirm) {
        ConfirmDialog dialog = createDanger(source, header, text, onConfirm);
        dialog.open();
        return dialog;
    }

    private static ConfirmDialog configure(ConfirmDialog dialog, String confirmText, String cancelText,
            Runnable onConfirm) {
        return configure(dialog, confirmText, cancelText, null, onConfirm);
    }

    private static ConfirmDialog configure(ConfirmDialog dialog, String confirmText, String cancelText,
            String confirmTheme, Runnable onConfirm) {
        dialog.addClassName(OVERLAY_CLASS_NAME);
        dialog.setConfirmText(confirmText);
        if (confirmTheme != null) {
            dialog.setConfirmButtonTheme(confirmTheme);
        }
        dialog.setCancelable(true);
        dialog.setCancelText(cancelText);
        if (onConfirm != null) {
            dialog.addConfirmListener(event -> onConfirm.run());
        }
        return dialog;
    }
}
