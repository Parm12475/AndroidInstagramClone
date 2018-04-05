package com.example.harindermaan.instagramclone.Dialogs;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.harindermaan.instagramclone.R;

public class ConfirmPasswordDialog extends DialogFragment
{
    private static final String TAG = "ConfirmPasswordDialog";
    private TextView mPassword;

    public interface OnConfirmPasswordListener
    {
        public void onConfirmPassword(String password);
    }//OnConfrmPasswordListener

    OnConfirmPasswordListener mOnConfirmPasswordListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView: Started");
        View view = inflater.inflate(R.layout.dialog_confirm_password,container,false);
        mPassword = view.findViewById(R.id.confirm_password);

        TextView confirmDialog = view.findViewById(R.id.dialogConfirm);
        confirmDialog.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: Captured Password and Confirming ");
                String password = mPassword.getText().toString();
                if(!password.equals(""))
                {
                    mOnConfirmPasswordListener.onConfirmPassword(password);
                    getDialog().dismiss();
                }//if
                else
                {
                    Toast.makeText(getContext(), "You must enter a password", Toast.LENGTH_SHORT).show();
                }//else


            }
        });

        TextView cancelDialog = view.findViewById(R.id.dialogCancel);
        cancelDialog.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: Closing Dialog");
                getDialog().dismiss();
            }
        });

        return view;
    }//onCreateView

    @Override
    public void onAttach(Context context) 
    {
        super.onAttach(context);
        Log.d(TAG, "onAttach: ");
        
        try
        {
            mOnConfirmPasswordListener = (OnConfirmPasswordListener)getTargetFragment();
        }
        catch(ClassCastException e)
        {
            Log.d(TAG, "onAttach: ClassCastException : "+e.getMessage());
        }
    }//onAttach
}//ConfirmPasswordDialog
