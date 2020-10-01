package com.example.deepchat.app;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
//imports de dependências do firebase
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    TextInputLayout emailTextInputLayout,passTextInputLayout;
    DatabaseReference mDatabaseReference;
    FirebaseAuth mauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //this.setTitle("Login");

        emailTextInputLayout=(TextInputLayout)findViewById(R.id.editText1);
        passTextInputLayout=(TextInputLayout)findViewById(R.id.editText2);
        progressDialog=new ProgressDialog(LoginActivity.this);

        mauth=FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");

    }

    //----SHOWING ALERT DIALOG FOR EXITING THE APP----
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage("Tem certeza que deseja sair?");
        builder.setTitle("Sair");
        builder.setCancelable(false);
        builder.setPositiveButton("Ok",new MyListener());
        builder.setNegativeButton("Cancel",null);
        builder.show();

    }
    public class MyListener implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface dialog, int which) {
            finish();
        }
    }

    public void buttonIsClicked(View view){

        switch(view.getId()){

            case R.id.buttonSign:

                String email=emailTextInputLayout.getEditText().getText().toString().trim();
                String password=passTextInputLayout.getEditText().getText().toString().trim();

                //---CHECKING IF EMAIL AND PASSWORD IS NOT EMPTY----
                if(TextUtils.isEmpty(email)||TextUtils.isEmpty(password)){
                    Toast.makeText(LoginActivity.this, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show();
                    return ;
                }
                progressDialog.setTitle("Verificando Credenciais");
                progressDialog.setMessage("Por favor, aguarde enquanto estamos verificando as credenciais..");
                progressDialog.setCancelable(false);
                progressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();
                login_user(email,password);
                break;

            case R.id.buttonRegister:

                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }
    }

    private void login_user(String email, String password) {

        //---SIGN IN FOR THE AUTHENTICATE EMAIL-----
        mauth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();

                        if(task.isSuccessful()){

                            //---ADDING DEVICE TOKEN ID AND SET ONLINE TO BE TRUE---
                            //---DEVICE TOKEN IS USED FOR SENDING NOTIFICATION----
                            String user_id=mauth.getCurrentUser().getUid();
                            String token_id= FirebaseInstanceId.getInstance().getToken();
                            Map addValue = new HashMap();
                            addValue.put("device_token",token_id);
                            addValue.put("online","true");

                            //---IF UPDATE IS SUCCESSFULL , THEN OPEN MAIN ACTIVITY---
                            mDatabaseReference.child(user_id).updateChildren(addValue, new DatabaseReference.CompletionListener(){

                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                    if(databaseError==null){

                                        //---OPENING MAIN ACTIVITY---
                                        Log.e("Login : ","Login realizado com sucesso!" );
                                        Toast.makeText(getApplicationContext(), "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
                                        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else{
                                        Toast.makeText(LoginActivity.this, databaseError.toString()  , Toast.LENGTH_SHORT).show();
                                        Log.e("Erro é : ",databaseError.toString());

                                    }
                                }
                            });



                        }
                        else{
                            //---IF AUTHENTICATION IS WRONG----
                            Toast.makeText(LoginActivity.this, "Credenciais erradas" +
                                    "", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
