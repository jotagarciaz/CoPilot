package com.mdh.ivanmuniz.copilotapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class EditDialog extends AppCompatDialogFragment {

    private EditText nameEditText;
    private EditText descriptionEditText;
    private EditDialogListener listener;
    private String pathId;

    public EditDialog(){
        super();
    }

    // Initiate edit dialog, with cancel button and confirm button
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        pathId = bundle.getString("pathid");
        String oldName = bundle.getString("pathname");
        String oldDescription = bundle.getString("pathdescription");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_edit_dialog, null);

        builder.setView(view)
                .setTitle("Edit name and description")
                .setIcon(R.drawable.ic_build_black_24dp)
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Do nothing, user canceled the edit
                    }
                })
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newName = nameEditText.getText().toString();
                        String newDescription = descriptionEditText.getText().toString();
                        listener.applyTexts( pathId, newName, newDescription);
                    }
                });

        nameEditText = view.findViewById(R.id.nameEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        nameEditText.setText(oldName);
        descriptionEditText.setText(oldDescription);

        return builder.create();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (EditDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " Must implement EditDialogListener");
        }
    }


    public interface EditDialogListener{
        void applyTexts( String pathId, String name, String description);
    }
}