package com.AXC.Utolay;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Helper {

    private static List<Integer> list;

    public static void main() {
        list = new ArrayList<>();
        // list.add(R.raw.nice_to_meet_you);
        list.add(R.raw.ara_ara);
        list.add(R.raw.asa_dayo);
        list.add(R.raw.everyone_my_friend);
        list.add(R.raw.food);
        list.add(R.raw.gg_men);
        //list.add(R.raw.pain);
        list.add(R.raw.study);
        list.add(R.raw.tl_later);
        Log.v("USERINFO", "helper ready");
    }

    public static int getSound() {
        return list.get(new Random().nextInt(list.size()));
    }

}
