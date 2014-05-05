$(document).ready(function() {
    $.ajax({
        url: "data.csv",
        success: function(output) {
            var csv = CSVToArray(output);
            for (var i = 0; i < csv.length; i++) {
                var pokemonArray = csv[i];
                // Make sure all the data is there.
                if (pokemonArray.length !== 4) {
                    continue;
                }

                // Parse the array of data into a Pokemon object
                var pokemon = new Object();
                pokemon.id = pokemonArray[0].substr(1);
                pokemon.name = pokemonArray[1];
                pokemon.type1 = "'" + pokemonArray[2].toLowerCase() + "'";
                pokemon.type2 = (pokemonArray[3] === "") ? "NULL" : "'" + pokemonArray[3].toLowerCase() + "'";

                // Scrape bulbapedia to get the image URL
                query = "http://bulbapedia.bulbagarden.net/wiki/File:" + pokemon.id + pokemon.name + ".png";
                $.ajax({
                    url: "http://query.yahooapis.com/v1/public/yql?q=SELECT%20*%20FROM%20html%20WHERE%20url%3D'" + encodeURIComponent(query) + "'&diagnostics=true",
                    async: false,
                    success: function(output) {
                        var image = $(output).find('.fullMedia a').eq(0).attr('href');
                        pokemon.url = !image ? "NULL" : "'" + image + "'";
                        var sql = "INSERT INTO pokemon (id, name, type1, type2, image) VALUES ('"+pokemon.id+"', '"+pokemon.name+"', "+pokemon.type1+", "+pokemon.type2+", "+pokemon.url+");";
                        console.log(sql);
                        $('body').append(sql + "<br>");
                    }
                });
            }
        }
    });
});

// From: http://stackoverflow.com/questions/1293147/javascript-code-to-parse-csv-data
// This will parse a delimited string into an array of
// arrays. The default delimiter is the comma, but this
// can be overriden in the second argument.
function CSVToArray( strData, strDelimiter ){
    // Check to see if the delimiter is defined. If not,
    // then default to comma.
    strDelimiter = (strDelimiter || ",");

    // Create a regular expression to parse the CSV values.
    var objPattern = new RegExp(
        (
            // Delimiters.
            "(\\" + strDelimiter + "|\\r?\\n|\\r|^)" +

            // Quoted fields.
            "(?:\"([^\"]*(?:\"\"[^\"]*)*)\"|" +

            // Standard fields.
            "([^\"\\" + strDelimiter + "\\r\\n]*))"
        ),
        "gi"
        );


    // Create an array to hold our data. Give the array
    // a default empty first row.
    var arrData = [[]];

    // Create an array to hold our individual pattern
    // matching groups.
    var arrMatches = null;


    // Keep looping over the regular expression matches
    // until we can no longer find a match.
    while (arrMatches = objPattern.exec( strData )){

        // Get the delimiter that was found.
        var strMatchedDelimiter = arrMatches[ 1 ];

        // Check to see if the given delimiter has a length
        // (is not the start of string) and if it matches
        // field delimiter. If id does not, then we know
        // that this delimiter is a row delimiter.
        if (
            strMatchedDelimiter.length &&
            (strMatchedDelimiter != strDelimiter)
            ){

            // Since we have reached a new row of data,
            // add an empty row to our data array.
            arrData.push( [] );

        }


        // Now that we have our delimiter out of the way,
        // let's check to see which kind of value we
        // captured (quoted or unquoted).
        if (arrMatches[ 2 ]){

            // We found a quoted value. When we capture
            // this value, unescape any double quotes.
            var strMatchedValue = arrMatches[ 2 ].replace(
                new RegExp( "\"\"", "g" ),
                "\""
                );

        } else {

            // We found a non-quoted value.
            var strMatchedValue = arrMatches[ 3 ];

        }


        // Now that we have our value string, let's add
        // it to the data array.
        arrData[ arrData.length - 1 ].push( strMatchedValue );
    }

    // Return the parsed data.
    return( arrData );
}