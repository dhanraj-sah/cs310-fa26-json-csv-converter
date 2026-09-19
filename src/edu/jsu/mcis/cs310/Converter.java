package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import com.opencsv.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Locale;

public class Converter {
    
    /*
        
        Consider the following CSV data, a portion of a database of episodes of
        the classic "Star Trek" television series:
        
        "ProdNum","Title","Season","Episode","Stardate","OriginalAirdate","RemasteredAirdate"
        "6149-02","Where No Man Has Gone Before","1","01","1312.4 - 1313.8","9/22/1966","1/20/2007"
        "6149-03","The Corbomite Maneuver","1","02","1512.2 - 1514.1","11/10/1966","12/9/2006"
        
        (For brevity, only the header row plus the first two episodes are shown
        in this sample.)
    
        The corresponding JSON data would be similar to the following; tabs and
        other whitespace have been added for clarity.  Note the curly braces,
        square brackets, and double-quotes!  These indicate which values should
        be encoded as strings and which values should be encoded as integers, as
        well as the overall structure of the data:
        
        {
            "ProdNums": [
                "6149-02",
                "6149-03"
            ],
            "ColHeadings": [
                "ProdNum",
                "Title",
                "Season",
                "Episode",
                "Stardate",
                "OriginalAirdate",
                "RemasteredAirdate"
            ],
            "Data": [
                [
                    "Where No Man Has Gone Before",
                    1,
                    1,
                    "1312.4 - 1313.8",
                    "9/22/1966",
                    "1/20/2007"
                ],
                [
                    "The Corbomite Maneuver",
                    1,
                    2,
                    "1512.2 - 1514.1",
                    "11/10/1966",
                    "12/9/2006"
                ]
            ]
        }
        
        Your task for this program is to complete the two conversion methods in
        this class, "csvToJson()" and "jsonToCsv()", so that the CSV data shown
        above can be converted to JSON format, and vice-versa.  Both methods
        should return the converted data as strings, but the strings do not need
        to include the newlines and whitespace shown in the examples; again,
        this whitespace has been added only for clarity.
        
        NOTE: YOU SHOULD NOT WRITE ANY CODE WHICH MANUALLY COMPOSES THE OUTPUT
        STRINGS!!!  Leave ALL string conversion to the two data conversion
        libraries we have discussed, OpenCSV and json-simple.  See the "Data
        Exchange" lecture notes for more details, including examples.
        
    */
    
    @SuppressWarnings("unchecked")
    public static String csvToJson(String csvString) {
        
        String result = "{}";
        
        try (CSVReader reader = new CSVReader(new StringReader(csvString))) {
            String[] headings = reader.readNext();
            if (headings == null) {
                throw new IllegalArgumentException("CSV must include a header row.");
            }

            JsonArray colHeadings = new JsonArray(Arrays.asList(headings));
            JsonArray prodNums = new JsonArray();
            JsonArray data = new JsonArray();
            String[] row;

            while ((row = reader.readNext()) != null) {
                prodNums.add(row[0]);
                JsonArray values = new JsonArray();
                for (int column = 1; column < row.length; column++) {
                    // Season and episode are numbers in JSON; other fields are strings.
                    if (column == 2 || column == 3) {
                        values.add(Integer.valueOf(row[column]));
                    }
                    else {
                        values.add(row[column]);
                    }
                }
                data.add(values);
            }

            JsonObject json = new JsonObject();
            json.put("ProdNums", prodNums);
            json.put("ColHeadings", colHeadings);
            json.put("Data", data);
            result = Jsoner.serialize(json);
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return result.trim();
        
    }
    
    @SuppressWarnings("unchecked")
    public static String jsonToCsv(String jsonString) {
        
        String result = "";
        
        try {
            
            JsonObject json = (JsonObject) Jsoner.deserialize(jsonString);
            JsonArray headings = (JsonArray) json.get("ColHeadings");
            JsonArray prodNums = (JsonArray) json.get("ProdNums");
            JsonArray data = (JsonArray) json.get("Data");

            StringWriter output = new StringWriter();
            try (CSVWriter writer = new CSVWriter(output)) {
                writer.writeNext(headings.toArray(new String[0]), true);

                for (int index = 0; index < data.size(); index++) {
                    JsonArray values = (JsonArray) data.get(index);
                    String[] row = new String[headings.size()];
                    row[0] = (String) prodNums.get(index);
                    for (int column = 1; column < row.length; column++) {
                        Object value = values.get(column - 1);
                        if (column == 3) {
                            // Restore the CSV's two-digit episode field.
                            row[column] = String.format(Locale.ROOT, "%02d",
                                    ((Number) value).intValue());
                        }
                        else if (column == 2) {
                            row[column] = Integer.toString(((Number) value).intValue());
                        }
                        else {
                            row[column] = (String) value;
                        }
                    }
                    writer.writeNext(row, true);
                }
            }
            result = output.toString();
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return result.trim();
        
    }
    
}
