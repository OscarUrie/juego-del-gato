package com.oucs.tictactoe.ui;

import android.os.Bundle;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.oucs.tictactoe.R;
import com.oucs.tictactoe.common.Constantes;
import com.oucs.tictactoe.model.Jugada;
import com.oucs.tictactoe.model.User;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    List<ImageView> casillas;
    TextView tvNombreJugadorUno, tvNombreJugadorDos;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    FirebaseUser firebaseUser;
    String uid,jugadaId,nombreJugadorUno="",nombreJugadorDos="",ganadorId="";
    String nombreJugador="";
    Jugada jugada;
    //User userUno,userDos;
    ListenerRegistration listenerJugada=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getTheExtras();
        initViews();
        initFirebase();
    }
    @Override
    protected void onStart() {
        super.onStart();
        jugadaListener();
    }

    @Override
    protected void onStop() {
        if (listenerJugada!=null){
            listenerJugada.remove();
        }
        super.onStop();
    }


    private void getTheExtras() {
        Bundle extras=getIntent().getExtras();
        jugadaId=extras.getString(Constantes.JUGADA_ID,"");
        Log.i("Firebase","Recivido: "+jugadaId);
    }
    private void initViews() {
        casillas=new ArrayList<>();
        tvNombreJugadorUno =findViewById(R.id.jugador_uno_game);
        tvNombreJugadorDos =findViewById(R.id.jugador_dos_game);
        tvNombreJugadorUno.setTextColor(getColor(R.color.azul));
        casillas.add(findViewById(R.id.casilla_0));
        casillas.add(findViewById(R.id.casilla_1));
        casillas.add(findViewById(R.id.casilla_2));
        casillas.add(findViewById(R.id.casilla_3));
        casillas.add(findViewById(R.id.casilla_4));
        casillas.add(findViewById(R.id.casilla_5));
        casillas.add(findViewById(R.id.casilla_6));
        casillas.add(findViewById(R.id.casilla_7));
        casillas.add(findViewById(R.id.casilla_8));
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> mostrarToast("hola"));
    }
    private void initFirebase() {
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        db=FirebaseFirestore.getInstance();
        uid=firebaseUser.getUid();
    }

    private void jugadaListener() {
        //Es para obtener los datos de la jugada
        listenerJugada=db.collection(Constantes.NOMBRE_COLECCION_JUGADAS)
                .document(jugadaId)
                .addSnapshotListener(GameActivity.this, (snapshot, error) -> {
                    if(error!=null){
                        mostrarToast("Error al obtener la jugada");
                        return;
                    }
                    String source=(snapshot!=null&&snapshot.getMetadata().hasPendingWrites())?"Local":"Server";
                    assert snapshot != null;
                    if(snapshot.exists()&&source.equals("Server")){
                        //Parseo de datos DocumentSnapshot->Jugada
                        jugada=snapshot.toObject(Jugada.class);
                        if (nombreJugadorUno.isEmpty()||nombreJugadorDos.isEmpty()){
                            //Se obtine los nombres de la jugada
                            getNombreJugadores();
                        }
                        updateUi();
                    }
                    updatePlayersUI();
                });

    }
    private void updateUi() {
        int casilla;
        for(int i=0;i<9;i++){
            casilla=jugada.getCeldas().get(i);
             if(casilla>0){
                 if(casilla==1)
                 casillas.get(i).setImageResource(R.drawable.ic_player_one);
                 else{
                     casillas.get(i).setImageResource(R.drawable.ic_player_two);
                 }
             }
        }
    }
    private void updatePlayersUI(){
        if(jugada.isTurno1()){
            tvNombreJugadorUno.setTextColor(getColor(R.color.azul));
            tvNombreJugadorDos.setTextColor(getColor(R.color.black));
        }
        else {
            tvNombreJugadorUno.setTextColor(getColor(R.color.black));
            tvNombreJugadorDos.setTextColor(getColor(R.color.rojo));
        }
        if(!jugada.getGanador().isEmpty()){
            ganadorId=jugada.getGanador();
            mostrarDialogoGameOver();
        }
    }

    @WorkerThread
    private void getNombreJugadores() {
        db.collection(Constantes.NOMBRE_COLECCION_USUARIOS)
                .document(jugada.getJugador1Id())
                .get()
                .addOnSuccessListener(GameActivity.this,documentSnapshot->{
                    nombreJugadorUno=documentSnapshot.get(User.NOMBRE).toString();
                    tvNombreJugadorUno.setText(nombreJugadorUno);
                    //userUno=documentSnapshot.toObject(User.class);
                    if(jugada.getJugador1Id().equals(uid)){
                        nombreJugador=nombreJugadorUno;
                    }
                });
        db.collection(Constantes.NOMBRE_COLECCION_USUARIOS)
                .document(jugada.getJugador2Id())
                .get()
                .addOnSuccessListener(GameActivity.this,documentSnapshot->{
                    //userDos=documentSnapshot.toObject(User.class);
                    nombreJugadorDos=documentSnapshot.get(User.NOMBRE).toString();
                    tvNombreJugadorDos.setText(nombreJugadorDos);
                    if(jugada.getJugador2Id().equals(uid)){
                        nombreJugador=nombreJugadorDos;
                    }
                });
    }
    public void casillaSeleccionada(View view){
        if(!jugada.getGanador().isEmpty()){
            mostrarToast("La partida ha terminado");
        }
        else{
            if(jugada.isTurno1()&&jugada.getJugador1Id().equals(uid)){
                //Esta jugando actualmente el jugador 1
                actualizarJugada(view.getTag().toString());
            }
            else if(!jugada.isTurno1()&&jugada.getJugador2Id().equals(uid)){
                //Esta jugando el jugador 2
                actualizarJugada(view.getTag().toString());
            }
            else{
                mostrarToast("No es tu turno");
            }
        }
    }

    private void actualizarJugada(String numeroCasilla) {
        int posicion=Integer.parseInt(numeroCasilla);
        //Cuando se intenta llenar una casilla ya ocupada
        if(jugada.getCeldas().get(posicion)!=0){
            mostrarToast("Selecciona una casilla libre");
        }
        else{
            if(jugada.isTurno1()){
                casillas.get(posicion).setImageResource(R.drawable.ic_player_one);
                jugada.getCeldas().set(posicion,1);
            }
            else{
                casillas.get(posicion).setImageResource(R.drawable.ic_player_two);
                jugada.getCeldas().set(posicion,2);
            }
            //cambio de turno en la jugada
            if(existeSolucion()){
                jugada.setGanador(uid);
                mostrarToast("Hay Ganador");
            }
            else if(todasLLenas()){
                //La solucion es falsa y todas estan llenas
                jugada.setGanador(Constantes.JUEGO_EMPATADO);
                mostrarToast("empate");
            }
            else{
                jugada.setTurno1(!jugada.isTurno1());
            }

            //cambio de turno en Firebase
            db.collection(Constantes.NOMBRE_COLECCION_JUGADAS)
                .document(jugadaId)
                .set(jugada)
                .addOnSuccessListener(GameActivity.this,aVoid -> Log.i("Firebase","Partida guardada en Firebase"))
                .addOnFailureListener(GameActivity.this,e -> Log.i("Firebase","Error al guardar jugada"));
        }
    }
    private boolean todasLLenas(){
        List<Integer> a=new ArrayList<>(jugada.getCeldas());
        for (int i=0;i<9;i++){
            if(a.get(i)==0){
                return false;
            }
        }
        return true;
    }
    private boolean existeSolucion(){
        //jugada.getCeldas().get(i)
        List<Integer> a=new ArrayList<>(jugada.getCeldas());
        //int total1=0,total2=0,fin;
        int total1,total2,fin;
        for(int i=0;i<8;i++) {
            fin = 2*Constantes.Gato[i][1]+Constantes.Gato[i][0]+1;
            total1 = 0;
            total2 = 0;
            for( int j=Constantes.Gato[i][0];j<fin;j=j+Constantes.Gato[i][1]){
                if (a.get(j)==0) break;
                if (a.get(j) == 1)total1 += 1;
                if (a.get(j) == 2)total2 += 2;
            }
            if (total1 == 3 || total2==6){
                //break;
                return true;
            }
        }
        return false;
    }

    private void mostrarToast(@NonNull final String mensaje){
        Toast.makeText(GameActivity.this,mensaje,Toast.LENGTH_SHORT).show();
    }
    private void mostrarDialogoGameOver() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view=getLayoutInflater().inflate(R.layout.game_over_dialog,null);
        TextView tvPuntos=view.findViewById(R.id.puntos_del_jugador);
        TextView tvInfo=view.findViewById(R.id.info_a_jugador);
        LottieAnimationView lottieDialog=view.findViewById(R.id.lottie_view_dialog);
        builder.setView(view)
                .setTitle("Game Over")
                .setCancelable(false);

        if(ganadorId.equals(Constantes.JUEGO_EMPATADO)){
            //actualizaPuntuacion(1);
            tvPuntos.setText(getString(R.string.un_punto));
            tvInfo.setText(nombreJugador+getString(R.string.juego_empatatado));
        }
        else if(ganadorId.equals(uid)){
            //actualizaPuntuacion(3);
            tvPuntos.setText(getString(R.string.tres_puntos));
            tvInfo.setText(nombreJugador+getString(R.string.solo_ganador));
            lottieDialog.setAnimation("winner_animation.json");
        }
        else{
            //actualizaPuntuacion(0);
            tvPuntos.setText(R.string.cero_puntos);
            tvInfo.setText(nombreJugador+" Perdiste!");
            lottieDialog.setAnimation("thumbs_down_animation.json");
        }
        //lottieDialog.setRepeatCount(0);
        lottieDialog.playAnimation();
        builder.setPositiveButton("Salir", (dialog, id) ->
            finish()
        );

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /*private void actualizaPuntuacion(int puntos) {
        User userUpdate=null;
        if(nombreJugador.equals(userUno.getNombre())){
            userUno.setPuntos(userUno.getPuntos()+puntos);
            userUno.setPartidas(userUno.getPartidas()+1);
            userUpdate=userUno;
        }
        else{
            userDos.setPuntos(userDos.getPuntos()+puntos);
            userDos.setPartidas(userDos.getPartidas()+1);
            userUpdate=userDos;
        }
        db.collection(Constantes.NOMBRE_COLECCION_USUARIOS)
                .document(uid)
                .set(userUpdate)
                .addOnSuccessListener(GameActivity.this,aVoid ->
                        Log.i("Firebase","Cambios guardados")
                )
                .addOnFailureListener(GameActivity.this,e ->
                        Log.i("Firebase","Cambios NO guardados: "+e)
                );
    }*/
}



/*
        if (total1==3){
            Log.i("Jugada", "Jugador Uno Gano");
        }
        else if( total2==6){
            Log.i("Jugada","Jugador Dos Gano, jugada");
        }
        else {
            Log.i("Jugada","Nadie Gano");
        }
        * */
//return total1 == 3 || total2 == 6;