package com.mdh.ivanmuniz.copilotapp.gcode;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.mdh.ivanmuniz.copilotapp.R;
import com.mdh.ivanmuniz.copilotapp.collection.PathCollection;
import com.mdh.ivanmuniz.copilotapp.object.Path;
import com.mdh.ivanmuniz.copilotapp.object.Point;
import com.mdh.ivanmuniz.copilotapp.object.PointType;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class GCodeRecyclerViewAdapter extends RecyclerView.Adapter<GCodeRecyclerViewAdapter.GCodeRecyclerViewHolder> {

    private Path mPath;
    private ArrayList<Point> pathPoints;
    private Activity activity;

    public GCodeRecyclerViewAdapter(Activity activity,Path path) {
        mPath = path;
        this.activity = activity;
        pathPoints = path.getPathList();
    }

    @NonNull
    @Override
    public GCodeRecyclerViewAdapter.GCodeRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_gcode_item, viewGroup, false);
        return new GCodeRecyclerViewAdapter.GCodeRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GCodeRecyclerViewAdapter.GCodeRecyclerViewHolder gCodeRecyclerViewHolder, final int i) {
        gCodeRecyclerViewHolder.setData(pathPoints.get(i), i);
        gCodeRecyclerViewHolder.activity = activity;
        gCodeRecyclerViewHolder.mPath = mPath;
    }

    @Override
    public int getItemCount() {
        return pathPoints.size();
    }

    // GCodeRecyclerViewHolder class
    public static class GCodeRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, AdapterView.OnItemSelectedListener {

        private View view;
        private TextView tvPoint;
        private TextView tvType;
        private TextView tvLat;
        private TextView tvLng;
        private TextView tvState;
        private Activity activity;
        private Point mPoint;
        private Path mPath;
        private int index;
        PathCollection pathCollection = PathCollection.getInstance();
        DecimalFormat df_coord = new DecimalFormat("#.00000000000000");
        DecimalFormat df_curve = new DecimalFormat("#0.000");

        public GCodeRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            tvPoint = (TextView) view.findViewById(R.id.textView_point);
            tvType = (TextView) view.findViewById(R.id.textView_type);
            tvLat = (TextView) view.findViewById(R.id.textView_lat);
            tvLng = (TextView) view.findViewById(R.id.textView_lng);
            tvState = (TextView) view.findViewById(R.id.textView_state);
        }

        public void setData(Point point, int i) {
            index = i;
            // Retrieve and format values to be set on textviews
            // Decimal format for doubles

            mPoint = point;

            String type = PointType.toString(point.getType());

            // Set point index
            tvPoint.setText("Point: " + (i+1));

            // Set point type
            if(point.getType() == PointType.CURVE)
                tvType.setText("Type: " + type + " " + df_curve.format(point.getCurveMagnitude()) + "˚");
            else
                tvType.setText("Type: " +type);
            tvType.setOnClickListener(this);

            // Set longitude and latitude
            tvLat.setText("Lat: " + df_coord.format(point.getLat()) + "˚");
            tvLng.setText("Lng: " + df_coord.format(point.getLng()) + "˚");
            tvLat.setOnClickListener(this);
            tvLng.setOnClickListener(this);

            // Set point state
            String str = "State: ";
            if(point.isLoad()) str = str + " load";
            if(point.isUnload()) str = str + " unload";
            if(point.isWait()) str = str + " wait";
            if(point.isInterest()) str = str + " interest";
            if(point.isFinish()) str = str + " finish";
            tvState.setText(str);
            tvState.setOnClickListener(this);
        }


        private void updateState(){
            LayoutInflater inflater=activity.getLayoutInflater();
            View layout=inflater.inflate(R.layout.layout_edit_state, null);

            final CheckBox cbLoad = (CheckBox) layout.findViewById(R.id.checkBox_load);
            final CheckBox cbUnload = (CheckBox) layout.findViewById(R.id.checkBox_unload);
            final CheckBox cbWait = (CheckBox) layout.findViewById(R.id.checkBox_wait);
            final CheckBox cbInterest = (CheckBox) layout.findViewById(R.id.checkBox_interest);
            cbLoad.setChecked(mPoint.isLoad());
            cbUnload.setChecked(mPoint.isUnload());
            cbWait.setChecked(mPoint.isWait());
            cbInterest.setChecked(mPoint.isInterest());

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(tvPoint.getText().toString());
            builder.setMessage("Enter new state value");
            builder.setCancelable(true);
            builder.setPositiveButton(
                    "Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mPoint.setLoad(cbLoad.isChecked());
                            mPoint.setUnload(cbUnload.isChecked());
                            mPoint.setWait(cbWait.isChecked());
                            mPoint.setInterest(cbInterest.isChecked());
                            String str = "State: ";
                            if(mPoint.isLoad()) str = str + " load";
                            if(mPoint.isUnload()) str = str + " unload";
                            if(mPoint.isWait()) str = str + " wait";
                            if(mPoint.isInterest()) str = str + " interest";
                            if(mPoint.isFinish()) str = str + " finish";
                            tvState.setText(str);
                            mPath.pointEdit(index, mPoint);
                            pathCollection.update(mPath);
                            dialog.cancel();
                        }
                    });

            builder.setNegativeButton(
                    "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.setView(layout);
            alert.show();
        }


        private void updateLongitude(){
            LayoutInflater inflater=activity.getLayoutInflater();
            View layout=inflater.inflate(R.layout.layout_edit_coordinate, null);

            final EditText input = (EditText)layout.findViewById(R.id.doubleEditText);
            input.setText("" + mPoint.getLng());
            input.setBackgroundResource(android.R.color.transparent);

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(tvPoint.getText().toString());
            builder.setMessage("Enter new longitude value");
            builder.setCancelable(true);
            builder.setPositiveButton(
                    "Ok",
                     new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if(!input.getText().toString().isEmpty()) {
                                tvLng.setText("Lng: " + input.getText().toString());
                                mPoint.setLng(Double.valueOf(input.getText().toString()));
                                mPath.pointEdit(index, mPoint);
                                pathCollection.update(mPath);
                                dialog.cancel();
                            }
                        }
                    });
            builder.setNegativeButton(
                    "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.setView(layout);
            alert.show();
        }

        private void updateLatitude(){
            LayoutInflater inflater=activity.getLayoutInflater();
            View layout=inflater.inflate(R.layout.layout_edit_coordinate, null);

            final EditText input = (EditText)layout.findViewById(R.id.doubleEditText);
            input.setText("" + mPoint.getLat());
            input.setBackgroundResource(android.R.color.transparent);

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(tvPoint.getText().toString());
            builder.setMessage("Enter new latitude value");
            builder.setCancelable(true);
            builder.setPositiveButton(
                    "Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if(!input.getText().toString().isEmpty()) {
                                tvLat.setText("Lat: " + input.getText().toString());
                                mPoint.setLat(Double.valueOf(input.getText().toString()));
                                mPath.pointEdit(index, mPoint);
                                pathCollection.update(mPath);
                                dialog.cancel();
                            }
                        }
                    });
            builder.setNegativeButton(
                    "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.setView(layout);
            alert.show();
        }


        private void updateType(){
            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.layout_edit_type, null);

            final Double curveValue = mPoint.getCurveMagnitude();
            final RadioButton cbLine = (RadioButton) layout.findViewById(R.id.checkBox_line);
            final RadioButton cbCurve = (RadioButton) layout.findViewById(R.id.checkBox_curve);
            final EditText etRadius = (EditText) layout.findViewById(R.id.editText_radius);
            final Spinner spinner = (Spinner) layout.findViewById(R.id.spinner);
            final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity, R.array.curveDirection, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setOnItemSelectedListener(this);

            cbLine.setChecked(mPoint.isLine());
            cbCurve.setChecked(mPoint.isCurve());
            etRadius.setText(String.valueOf(curveValue));

            if(cbLine.isChecked()) {
                etRadius.setFocusable(false);
                spinner.setEnabled(false);
                spinner.setClickable(false);
            }
            spinner.setAdapter(adapter);

            cbLine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        etRadius.setFocusable(false);
                        spinner.setEnabled(false);
                        spinner.setClickable(false);
                        spinner.setAdapter(adapter);
                        etRadius.setText("0.0");
                    } else {
                        etRadius.setFocusableInTouchMode(true);
                        spinner.setEnabled(true);
                        spinner.setClickable(true);
                        spinner.setAdapter(adapter);
                        etRadius.setText(String.valueOf(curveValue));
                    }
                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(tvPoint.getText().toString());
            builder.setMessage("Enter new type value");
            builder.setCancelable(true);
            builder.setPositiveButton(
                    "Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if(cbLine.isChecked()) {
                                mPoint.setType(PointType.LINE);
                                mPoint.setCurveMagnitude(0.0);
                                String type = PointType.toString(mPoint.getType());
                                tvType.setText("Type: " + type);
                            } else {
                                mPoint.setType(PointType.CURVE);
                                mPoint.setCurveMagnitude(Double.valueOf(etRadius.getText().toString()));
                                String type = PointType.toString(mPoint.getType());
                                tvType.setText("Type: " + type + " " + df_curve.format(mPoint.getCurveMagnitude()) + "˚");
                            }
                            mPath.pointEdit(index, mPoint);
                            pathCollection.update(mPath);
                            dialog.cancel();
                        }
                    });

            builder.setNegativeButton(
                    "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.setView(layout);
            alert.show();
        }

        @Override
        public void onClick(View view) {
           if(view == tvState){
               updateState();
           } else if(view == tvLng) {
               updateLongitude();
           } else if(view == tvLat){
               updateLatitude();
           } else if (view == tvType) {
               if (mPoint.getType() != 0) {
                   updateType();
               }
           }
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String text = parent.getItemAtPosition(position).toString();
            if(text.equals("Inwards")) {
                mPoint.setCurveDirection(1);
            } else if(text.equals("Outwards")) {
                mPoint.setCurveDirection(-1);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

}
