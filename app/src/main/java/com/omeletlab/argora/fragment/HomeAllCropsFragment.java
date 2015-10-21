package com.omeletlab.argora.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.omeletlab.argora.R;
import com.omeletlab.argora.adapter.RVAdapter;
import com.omeletlab.argora.model.Crop;
import com.omeletlab.argora.util.AppController;
import com.omeletlab.argora.util.GlobalConstant;
import com.omeletlab.argora.util.NameValuePair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;

/**
 * Created by akashs on 10/21/15.
 */
public class HomeAllCropsFragment extends Fragment {

    private final List<Crop> mCropList = new ArrayList<>();
    private JSONArray cropsJsonArray;

    public RVAdapter rvAdapter;
    private ProgressDialog pDialog;

    public HomeAllCropsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.crop_recycle_view, container, false);

        Context context = getActivity();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        rvAdapter = new RVAdapter(mCropList, getActivity());
        recyclerView.setAdapter(rvAdapter);

        mCropList.clear();
        rvAdapter.notifyDataSetChanged();

        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Loading, Please wait...");
        pDialog.setCancelable(false);

        loadCropsList();

        return view;
    }

    public void loadCropsList(){
        showpDialog();

        List<NameValuePair> params = new ArrayList<>();
        params.add(new NameValuePair(GlobalConstant.TAG_agg_level_desc, "STATE"));
        params.add(new NameValuePair(GlobalConstant.TAG_year, "2014"));
        params.add(new NameValuePair("class_desc", "ALL CLASSES"));
        params.add(new NameValuePair(GlobalConstant.TAG_source_desc, "SURVEY"));
        params.add(new NameValuePair(GlobalConstant.TAG_sector_desc, "CROPS"));
        params.add(new NameValuePair(GlobalConstant.TAG_group_desc, "FIELD%20CROPS"));
        params.add(new NameValuePair(GlobalConstant.TAG_statisticcat_desc, "AREA%20HARVESTED"));
        params.add(new NameValuePair(GlobalConstant.TAG_reference_period_desc, "YEAR"));

        String[] cropNameArray = getResources().getStringArray(R.array.crop_name);
        Log.d("all crop name",""+cropNameArray.length);
        for(int i=0;i<cropNameArray.length;i++){
            params.add(new NameValuePair(GlobalConstant.TAG_commodity_desc + "__or", cropNameArray[i]));
        }
        for(int i=2015;i>=1995;i--){
            params.add(new NameValuePair(GlobalConstant.TAG_year+ "__or", ""+i));
        }

        String fullUrl = GlobalConstant.urlBuilder(GlobalConstant.API_URL, params);
        Log.d("Nass api[Home all crop]", fullUrl);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                fullUrl, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d("Map server response", response.toString());

                try {


                        cropsJsonArray = response.getJSONArray("data");
                        HomeAllCropsFragment.this.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mCropList.clear();
                                    for (int i = 0; i < cropsJsonArray.length(); i++) {
                                        JSONObject item = cropsJsonArray.getJSONObject(i);

                                        String cropName = item.getString("commodity_desc");
                                        String stateName = item.getString("state_name");
                                        String year = item.getString("year");
                                        String value = item.getString("value");

                                        if(TextUtils.isDigitsOnly(value.replaceAll(",",""))){
                                            mCropList.add(new Crop(cropName, stateName, year, value));
                                        }
                                    }
                                    Collections.sort(mCropList, new CropComparator());
                                    rvAdapter.notifyDataSetChanged();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                hidepDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Volley library error in login activity", "Error: " + error.getMessage());
            }
        });

        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }
    private class CropComparator implements Comparator<Crop> {

        @Override
        public int compare(Crop s1, Crop s2) {

            Log.d("formate value",s1.getValue());
            long value1 = Long.parseLong(s1.getValue().replaceAll(",", ""));
            long value2 = Long.parseLong(s2.getValue().replaceAll(",", ""));

            return (value1<value2)?1:(value1>value2?-1:0);
        }

    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}