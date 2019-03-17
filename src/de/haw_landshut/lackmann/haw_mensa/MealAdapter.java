package de.haw_landshut.lackmann.haw_mensa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MealAdapter extends BaseExpandableListAdapter {
    private ArrayList<Meal> data;
    private LayoutInflater inflater;

    MealAdapter(Context context, ArrayList<Meal> listItems) {
        this.data = listItems;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public Object getChild(int groupPosition, int childPosition) {
        return data.get(groupPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        View row = convertView;
        MealHolder holder;

        if (row == null) {
            row = inflater.inflate(R.layout.details, parent, false);

            holder = new MealHolder(row);

            row.setTag(holder);
        } else {
            holder = (MealHolder) row.getTag();
        }

        Meal meal = data.get(groupPosition);
        holder.setData(meal);

        return row;
    }

    public Object getGroup(int groupPosition) {
        return data.get(groupPosition);
    }

    public int getGroupCount() {
        return data.size();
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {
        View row = convertView;
        MealHolder holder;

        if (row == null) {
            row = inflater.inflate(R.layout.list_item, parent, false);
            holder = new MealHolder(row);

            row.setTag(holder);
        } else {
            holder = (MealHolder) row.getTag();
        }

        Meal meal = data.get(groupPosition);

        holder.setData(meal);


        return row;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public boolean hasStableIds() {
        return true;
    }


    static class MealHolder {

        MealHolder(View row) {
            category = (TextView) row.findViewById(R.id.txtCategory);
            name = (TextView) row.findViewById(R.id.txtName);
            priceStudents = (TextView) row.findViewById(R.id.txtPriceStudents);
            priceEmployees = (TextView) row.findViewById(R.id.txtPriceEmployees);
            pricePupils = (TextView) row.findViewById(R.id.txtPricePupils);
            priceOthers = (TextView) row.findViewById(R.id.txtPriceOthers);
            notes = (TextView) row.findViewById(R.id.txtNotes);


            gefluegel = (ImageView) row.findViewById(R.id.iconGefluegel);
            schwein = (ImageView) row.findViewById(R.id.iconSchwein);
            rind = (ImageView) row.findViewById(R.id.iconRind);
            lamm = (ImageView) row.findViewById(R.id.iconLamm);
            wild = (ImageView) row.findViewById(R.id.iconWild);
            fisch = (ImageView) row.findViewById(R.id.iconFisch);
            alcohol = (ImageView) row.findViewById(R.id.iconAlcohol);
            vegetarisch = (ImageView) row.findViewById(R.id.iconVegetarisch);
            vegan = (ImageView) row.findViewById(R.id.iconVegan);
            mensa_vital = (ImageView) row.findViewById(R.id.iconMensa_vital);
            juradistllamm = (ImageView) row.findViewById(R.id.iconJuradistllamm);
            bioland = (ImageView) row.findViewById(R.id.iconBioland);
            eu_organic = (ImageView) row.findViewById(R.id.eu_organic);

        }

        void setData(Meal meal) {
            if (category != null) {
                category.setText(meal.category);
                name.setText(meal.name);
                int i = meal.notes.length;
                notes.setText("");
                for (String note : meal.notes) {
                    notes.append(note);
                    if (--i > 0) {
                        notes.append(", ");
                    }
                }
            }

            if (priceStudents != null) {
                priceStudents.setText(stringOrNone(meal.prices.students));
                priceEmployees.setText(stringOrNone(meal.prices.employees));
                pricePupils.setText(stringOrNone(meal.prices.pupils));
                priceOthers.setText(stringOrNone(meal.prices.others));

                List<String> notes = Arrays.asList(meal.notes);
                gefluegel.setVisibility(notes.contains("Geflügel") ? View.VISIBLE : View.GONE);
                schwein.setVisibility(notes.contains("Schweinefleisch") ? View.VISIBLE : View.GONE);
                rind.setVisibility(notes.contains("Rindfleisch") ? View.VISIBLE : View.GONE);
                lamm.setVisibility(notes.contains("Lamm") ? View.VISIBLE : View.GONE);
                wild.setVisibility(notes.contains("Wild") ? View.VISIBLE : View.GONE);
                fisch.setVisibility(notes.contains("Fisch") ? View.VISIBLE : View.GONE);
                vegetarisch.setVisibility(notes.contains("vegetarisch") ? View.VISIBLE : View.GONE);
                vegan.setVisibility(notes.contains("vegan") ? View.VISIBLE : View.GONE);
                mensa_vital.setVisibility(notes.contains("vital") ? View.VISIBLE : View.GONE);
                juradistllamm.setVisibility(notes.contains("juradistlamm") ? View.VISIBLE : View.GONE);
                bioland.setVisibility(notes.contains("bioland") ? View.VISIBLE : View.GONE);
                eu_organic.setVisibility(notes.contains("mit ausschließlich biologisch erzeugten Rohstoffen") ? View.VISIBLE : View.GONE);


            }
        }

        private String stringOrNone(float price) {
            if (price > 0)
                return String.format(Locale.getDefault(), "%.2f", price);
            return MainActivity.getAppContext().getResources().getString(R.string.noprice);
        }

        TextView category;
        TextView name;
        TextView priceStudents;
        TextView priceEmployees;
        TextView pricePupils;
        TextView priceOthers;
        TextView notes;

        ImageView vegetarisch;
        ImageView gefluegel;
        ImageView schwein;
        ImageView rind;
        ImageView lamm;
        ImageView wild;
        ImageView fisch;
        ImageView alcohol;
        ImageView vegan;
        ImageView mensa_vital;
        ImageView juradistllamm;
        ImageView bioland;
        ImageView eu_organic;
    }
}
