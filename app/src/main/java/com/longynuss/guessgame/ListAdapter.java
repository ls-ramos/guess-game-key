package com.longynuss.guessgame;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class ListAdapter extends ArrayAdapter<userGuess> {
    private final Context context;
    private final ArrayList<userGuess> elementos;
    private TextView userGuess;

    ListAdapter (Context context, ArrayList<userGuess>elementos){
        super(context,R.layout.linha,elementos);
        this.context = context;
        this.elementos = elementos;

    }

    public String getUserGuess() {
        return userGuess.toString();
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.linha,parent,false);

        userGuess = (TextView) rowView.findViewById(R.id.GuessUser_id);
        ImageView imagem1 = (ImageView) rowView.findViewById(R.id.image1_id);
        ImageView imagem2 = (ImageView) rowView.findViewById(R.id.image2_id);
        ImageView imagem3 = (ImageView) rowView.findViewById(R.id.image3_id);
        ImageView imagem4 = (ImageView) rowView.findViewById(R.id.image4_id);

        userGuess.setText(elementos.get(position).getUserGuess());
        imagem1.setImageResource( elementos.get(position).getImage1());
        imagem2.setImageResource(elementos.get(position).getImage2());
        imagem3.setImageResource(elementos.get(position).getImage3());
        imagem4.setImageResource(elementos.get(position).getImage4());
        return rowView;
    }

}
