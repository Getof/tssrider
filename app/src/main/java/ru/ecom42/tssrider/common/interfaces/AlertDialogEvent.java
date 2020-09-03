package ru.ecom42.tssrider.common.interfaces;

import ru.ecom42.tssrider.common.utils.AlertDialogBuilder;

public interface AlertDialogEvent {
    void onAnswerDialog(AlertDialogBuilder.DialogResult result);
}
