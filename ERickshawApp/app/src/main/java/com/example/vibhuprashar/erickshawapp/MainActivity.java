package com.example.vibhuprashar.erickshawapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.vibhuprashar.erickshawapp.Common.Common;
import com.example.vibhuprashar.erickshawapp.Model.Users;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {



Button btnSignUp,btnRegister;
RelativeLayout rootLayout;


FirebaseAuth auth;
FirebaseDatabase db;
DatabaseReference users;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



         auth= FirebaseAuth.getInstance();
         db= FirebaseDatabase.getInstance();
         users=db.getReference(Common.user_driver_tb1);

        btnRegister = (Button)findViewById(R.id.btnRegister);
        btnSignUp =(Button)findViewById(R.id.btnSignIn);
        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegisterDialog();
            }
        });


        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoginDialog();
            }
        });


    }

    private void showLoginDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("SignIN");
        dialog.setMessage("Please use email to signIn");
        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_login,null);

        final MaterialEditText editEmail = login_layout.findViewById(R.id.editEmail);
        final MaterialEditText editPassword = login_layout.findViewById(R.id.editPassword);

        dialog.setView(login_layout);

        dialog.setPositiveButton("SignIN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();

                    btnSignUp.isEnabled();




                        if (TextUtils.isEmpty(editEmail.getText().toString())) {
                            Snackbar.make(rootLayout, "Please Enter The Email Address", Snackbar.LENGTH_SHORT).show();
                            ;
                            return;


                        }
                        if (editPassword.getText().toString().length() < 6) {
                            Snackbar.make(rootLayout, "Password Must be greater Than 6 digits", Snackbar.LENGTH_SHORT).show();
                            ;
                            return;


                        }

                        final SpotsDialog waitingDialog =new SpotsDialog(MainActivity.this);
                        waitingDialog.show();

                        auth.signInWithEmailAndPassword(editEmail.getText().toString(),editPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                waitingDialog .dismiss();

                                FirebaseDatabase.getInstance().getReference(Common.user_driver_tb1).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Common.currentUser = dataSnapshot.getValue(Users.class);

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                        startActivity(new Intent(MainActivity.this,Welcome.class));
                        finish();


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                waitingDialog.dismiss();
                                Snackbar.make(rootLayout,"Failed"+e.getMessage(),Snackbar.LENGTH_SHORT).show();


                                btnSignUp.setEnabled(true);
                            }
                        });

                    }
                });

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();



    }



    private void showRegisterDialog()
    {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Register");
        dialog.setMessage("Please use email to register");
        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_register,null);

        final MaterialEditText editEmail = register_layout.findViewById(R.id.editEmail);
        final MaterialEditText editPassword = register_layout.findViewById(R.id.editPassword);
        final MaterialEditText editName = register_layout.findViewById(R.id.editName);
        final MaterialEditText editPhone = register_layout.findViewById(R.id.editPhone);

        dialog.setView(register_layout);

        dialog.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

                if(TextUtils.isEmpty(editEmail.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Please Enter The Email Address",Snackbar.LENGTH_SHORT).show();;
                    return;


                }
                if(editPassword.getText().toString().length()<6)
                {
                    Snackbar.make(rootLayout,"Password Must be greater Than 6 digits",Snackbar.LENGTH_SHORT).show();;
                    return;


                } if(TextUtils.isEmpty(editName.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Please Enter The Name",Snackbar.LENGTH_SHORT).show();;
                    return;


                } if(TextUtils.isEmpty(editPhone.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Please Enter The Phone Number",Snackbar.LENGTH_SHORT).show();;
                    return;


                }

                auth.createUserWithEmailAndPassword(editEmail.getText().toString(),editPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {


                        Users user = new Users();
                        user.setEmail(editEmail.getText().toString());
                        user.setPassword(editPassword.getText().toString());
                        user.setName(editName.getText().toString());
                        user.setPhone(editPhone.getText().toString());

                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Snackbar.make(rootLayout,"Register Successfully",Snackbar.LENGTH_SHORT).show();;
                                return;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(rootLayout,"Registeration Not Successfull"+e.getMessage(),Snackbar.LENGTH_SHORT).show();;
                                return;
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootLayout,"Failed"+e.getMessage(),Snackbar.LENGTH_SHORT).show();;
                        return;
                    }
                });

            }
        });

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

            }
        });

        dialog.show();

    }
}
