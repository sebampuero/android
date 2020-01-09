package com.example.tm18app.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

/**
 * Manager of {@link AlertDialog} that are displayed inside the app.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 25.12.2019
 */
public class DialogManager {

    private static DialogManager instance;

    /**
     * Returns the {@link DialogManager} instance
     * @return the instance
     */
    public static DialogManager getInstance() {
        if(instance == null)
            instance = new DialogManager();
        return instance;
    }

    public interface DialogCommand {
        void execute(View dialogCustomView, AlertDialog dialog);
    }

    /**
     * Shows an {@link AlertDialog} that contains a message and a confirmation button
     * @param context {@link Context}
     * @param title {@link String}
     * @param message {@link String}
     * @param btnResId {@link Integer}
     * @param clickListener {@link DialogInterface.OnClickListener}
     */
    public void showAlertDialogSingleButton(Context context,
                                            String title,
                                            String message,
                                            int btnResId,
                                            DialogInterface.OnClickListener clickListener){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(message);
        alertBuilder.setPositiveButton(btnResId, clickListener);
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    /**
     * Shows an {@link AlertDialog} with a custom layout
     * @param layout {@link Integer}
     * @param context {@link Context}
     * @param command {@link DialogCommand} function interface to be executed with the custom
     *                                   {@link AlertDialog}
     */
    public void showCustomDialog(int layout, Context context, DialogCommand command) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        View dialogLayout = inflater.inflate(layout, null);
        dialog.setView(dialogLayout);
        dialog.show();
        dialog.setCancelable(true);
        command.execute(dialogLayout, dialog);
    }

}
