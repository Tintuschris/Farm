package com.example.farm;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "MyDatabaseHelper";
    private static final String DATABASE_NAME = "RegionsDatabase.db";
    private static final int DATABASE_VERSION = 1;
    private final Context mContext;
    private SQLiteDatabase mDatabase;

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createRegionsTable = "CREATE TABLE regions ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT,"
                + "crops TEXT,"
                + "soil_properties TEXT,"
                + "soil_type TEXT)";
        db.execSQL(createRegionsTable);
        //Agroforesty Table
        String createAgroforestryTable = "CREATE TABLE agroforestry (\n" +
                "    id SERIAL PRIMARY KEY,\n" +
                "    crops TEXT,\n" +
                "    soil_name TEXT,\n" +
                "    soil_properties TEXT,\n" +
                "    trees TEXT\n" +
                ");\n";
        db.execSQL(createAgroforestryTable);


        String REGIONS_JSON_DATA = "[\n" +
                "    {\n" +
                "        \"Crops\": \"maize, beans, millet, sorghum, and sugarcane\",\n" +
                "        \"Id\": 1,\n" +
                "        \"Name\": \"Nyanza\",\n" +
                "        \"Soil_Properties\": \"Soil pH: 5.5-7.5\\r\\nNitrogen content: High\\r\\nWater Holding Capacity: High\",\n" +
                "        \"Soil_Type\": \"fluvisols, vertisols, alluvial \"\n" +
                "    },\n" +
                "    {\n" +
                "        \"Crops\": \"Maize, Beans, Bananas, Tea, \\r\\nSweet Potatoes, Irish Potatoes, Sugarcane\",\n" +
                "        \"Id\": 2,\n" +
                "        \"Name\": \"Western\",\n" +
                "        \"Soil_Properties\": \"Soil pH: 5.5-7.5\\r\\nNitrogen Content: Higher in higher grounds\\r\\nWater holding capacity: moderate\",\n" +
                "        \"Soil_Type\": \"Alfisols, Ultisols, Vertisols\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"Crops\": \"Maize, Wheat, Coffe, Tea\\r\\nVegetables: Onions, Cabbages, Tomatoes, Carrots\\r\\nFruits: Avocado, Passion, Mangoes, Citrus\",\n" +
                "        \"Id\": 3,\n" +
                "        \"Name\": \"Rift Valley\",\n" +
                "        \"Soil_Properties\": \"Soil pH: 5.5-8.5\\r\\nNitrogen Content: Moderate to High\\r\\nWater Holding Capacity: High\",\n" +
                "        \"Soil_Type\": \"Andosols, Nitisols ,Vertisols, Fluvisols, Cambisols, Regosols, Luvisols\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"Crops\": \"Coffee, Tea\\r\\nHorticultural crops:tomatoes, onions, cabbages, carrots, avocados, mangoes, and passion fruits.\\r\\nMaize, Wheat\",\n" +
                "        \"Id\": 4,\n" +
                "        \"Name\": \"Central \",\n" +
                "        \"Soil_Properties\": \"Soil pH: 4.0-7.0\\r\\nNitrogen Content: Low in acrisols ,High in Nitisols\\r\\nWater holding Capacity: High\",\n" +
                "        \"Soil_Type\": \"Nitisols, Acrisols\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"Crops\": \"Maiza, Beans, Sorghums and Millet\\r\\nFruits and Vegetable:mangoes, bananas, papayas, avocados, tomatoes, onions, and carrots.\\r\\n\",\n" +
                "        \"Id\": 5,\n" +
                "        \"Name\": \"Eastern\",\n" +
                "        \"Soil_Properties\": \"Soil pH: less than 6.0\\r\\nNitrogen: low\\r\\nWater Holding Capacity: generally low\",\n" +
                "        \"Soil_Type\": \"Ferralsols ,Arenosols, Nitisols ,Acrisols,  Leptosols\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"Crops\": \"Coconuts, Cashew nuts, Mangoes , Cassava\",\n" +
                "        \"Id\": 6,\n" +
                "        \"Name\": \"Coast\",\n" +
                "        \"Soil_Properties\": \"Soil pH: less to more than 7\\r\\nNitrogen Content: moderate to high\\r\\nWater holding capacity: low to high\",\n" +
                "        \"Soil_Type\": \"Alfisols, Vertisols, Ultisols, Fluvisols, Lithosols\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"Crops\": \"Sorghum and Millet, Cowpeas, Sesame, Watermelon\",\n" +
                "        \"Id\": 7,\n" +
                "        \"Name\": \"North Eastern\",\n" +
                "        \"Soil_Properties\": \"Soil pH: more than 9\\r\\nNitrogen Content: Low\\r\\nWater Holding Capacity: low\",\n" +
                "        \"Soil_Type\": \"Arenosols, Regosols, Vertisols, Solonchaks\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"Crops\": \"Vegetables:cabbage, spinach, kale, carrots, onions, tomatoes, and beans\\r\\nFruits: avocados, mangoes, passion fruits, and pineapples\\r\\nTea and coffee\",\n" +
                "        \"Id\": 8,\n" +
                "        \"Name\": \"Nairobi\",\n" +
                "        \"Soil_Properties\": \"Soil pH: 5.5 - 7.5\\r\\nNitrogen Content : Medium to High\\r\\nWter Holding Capacity: Medium to High\",\n" +
                "        \"Soil_Type\": \"Nitisols, Vertisols, Andosols, Cambisols, Luvisols\"\n" +
                "    }\n" +
                "]";
        // Parse the JSON data and insert each region into the database
        try {
            JSONArray regionsJsonArray = null;
            try {
                regionsJsonArray = new JSONArray(REGIONS_JSON_DATA);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            for (int i = 0; i < regionsJsonArray.length(); i++) {
                JSONObject regionJson = regionsJsonArray.getJSONObject(i);
                String name = regionJson.getString("Name");
                String crops = regionJson.getString("Crops");
                String soilProperties = regionJson.getString("Soil_Properties");
                String soilType = regionJson.getString("Soil_Type");

                ContentValues values = new ContentValues();
                values.put("name", name);
                values.put("crops", crops);
                values.put("soil_properties", soilProperties);
                values.put("soil_type", soilType);

                db.insert("regions", null, values);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String insertAgroforestryRow = "INSERT INTO Agroforestry (crops, soil_name, soil_properties, trees)\n" +
                "VALUES\n" +
                "    ('Coffee, Tea, Banana, Mango, maize, beans', 'Ferralsols', 'pH ranges from strongly acidic to slightly acidic, typically between 4.0 and 6.5. Nitrogen content is usually low and water holding capacity is also low due to their highly weathered nature.', 'Mukau (African Mahogany), Mvule (African Teak), Muhugu (African Sandalwood), Grevillea robusta, Leucaena leucocephala, and Sesbania sesban'),\n" +
                "    ('Maize, Beans, Peas, Soybeans, Coffee, Tea, potatoes', 'Nitisols', 'pH ranges from slightly acidic to neutral, typically between 5.5 and 7.5. They are generally fertile and have a high water holding capacity.', 'Mukau (African Mahogany), Mvule (African Teak), Muhugu (African Sandalwood), Gravellia, Eucalyptus, Grevillea robusta, Macadamia integrifolia, Calliandra calothyrsus'),\n" +
                "    ('Cotton, Maize, Sorghum, Millet, Groundnuts', 'Vertisols', 'pH ranges from neutral to slightly alkaline, typically between 6.5 and 8.5. They have a high water holding capacity but can be difficult to cultivate due to their tendency to shrink and swell.', 'Acacia, Eucalyptus, Mango, Melia,  Acacia senegal, Prosopis juliflora, and Faidherbia albida'),\n" +
                "    ('Cassava, Maize, Sorghum, Millet, Groundnuts, Beans, Peas, potatoes, and cassava', 'Cambisols', 'pH ranges from strongly acidic to slightly alkaline, typically between 4.0 and 8.5. They have a wide range of characteristics depending on location.', 'Mukau (African Mahogany), Mvule (African Teak), Muhugu (African Sandalwood), Gravellia, Eucalyptus, Acacia mearnsii, and Eucalyptus grandis'),\n" +
                "    ('Coffee, Tea, Pyrethrum, Vegetables, Fruits, Legumes', 'Andosols', 'pH ranges from strongly acidic to slightly acidic, typically between 4.0 and 6.5. They are generally rich in nutrients and have a high water holding capacity.', 'Mukau (African Mahogany), Mvule (African Teak), Muhugu (African Sandalwood), Gravellia, Eucalyptus, Alnus acuminata, Calliandra calothyrsus, and Erythrina abyssinica'),\n" +
                "    ('Sorghum, Maize, Millet, Groundnuts, Sorghum, Beans, Peas', 'Regosols', 'pH ranges from strongly acidic to slightly alkaline, typically between 4.5 and 8.0. They are often shallow and rocky.', 'Eucalyptus, Acacia senegal, Grevillea robusta, and Sesbania sesban'),\n" +
                "    ('Maize, Beans, Peas, Soybeans, Wheat, Barley and sweet potatoes', 'Luvisols', 'pH ranges from slightly acidic to slightly alkaline, typically between 6.0 and 7.5. They are generally fertile and have a moderate water holding capacity.', 'Acacia, Eucalyptus, Grevillea robusta, Leucaena leucocephala, and Sesbania sesban'),('Acacia tortilis, Balanites aegyptiaca, Commiphora africana, Ziziphus mauritiana, sorghum, pearl millet, cowpea, pigeon pea, sorghum, and cowpeas', 'Arenosols', 'pH ranges from strongly acidic to slightly alkaline, typically between 4.5 and 7.5. They have low water holding capacity and nutrient content, and are found in the arid regions of Kenya.', 'Acacia tortilis, Balanites aegyptiaca, Commiphora africana, Ziziphus mauritiana, Prosopis juliflora'),\n" +
                "\n" +
                "    ('Date palms, cotton, wheat, barley, forage crops, millet, sorghum, and cowpeas', 'Solonchaks', 'pH ranges from strongly acidic to slightly alkaline, typically between 4.0 and 8.5. They have a low water holding capacity and are saline, found in the arid regions of Kenya.', 'Acacia tortilis, Balanites aegyptiaca, Commiphora africana, Ziziphus mauritiana, Prosopis juliflora'), ('Sugarcane, Cassava, Rice, Soybeans, Wheat, Maize, Beans', 'Acrisols', 'pH ranges from strongly acidic to neutral, typically between 4.0 and 6.5. They have low nutrient content and water holding capacity, and are found in humid tropical regions of Kenya.', 'Acacia, Eucalyptus, Grevillea robusta, Leucaena leucocephala, and Sesbania sesban')";
        db.execSQL(insertAgroforestryRow);

            // We don't need to insert any data since the database already has data inserted using SQLite Browser

        Log.d(TAG, "Database created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Implementation of database upgrade if needed
    }

    public void openDatabase() {
        String dbPath = mContext.getDatabasePath(DATABASE_NAME).getPath();
        if (mDatabase != null && mDatabase.isOpen()) {
            return;
        }
        mDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public void closeDatabase() {
        if (mDatabase != null) {
            mDatabase.close();
        }
    }

    private boolean checkDatabase() {
        SQLiteDatabase db = null;
        try {
            String dbPath = mContext.getDatabasePath(DATABASE_NAME).getPath();
            db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (Exception e) {
            // The database doesn't exist yet
        }
        if (db != null) {
            db.close();
        }
        return db != null;
    }

    public void copyDatabase() throws IOException {
        InputStream inputStream = mContext.getAssets().open(DATABASE_NAME);
        String outFileName = mContext.getDatabasePath(DATABASE_NAME).getPath();
        OutputStream outputStream = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
        Log.d(TAG, "Database copied successfully");
    }
}

