<?php 
define ("DB_HOST","localhost");
define ("DB_USER","poketype");
define ("DB_PASSWORD","poketype");
define ("DB_NAME","pokemon");

require 'Slim/Slim.php';
    
$app = new Slim();
$db_con = initDB(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);

$app->get('/pokemon/:name', 'getPokemonByName');
$app->get('/pokemon/id/:id', 'getPokemonById');
$app->run();



function getPokemonByName($name) {
    echo getPokemon(queryName($name));
}

function getPokemonById($id) {
    echo getPokemon(queryId($id));
}

function queryName($name) {
    global $db_con;
    $query = "SELECT *, IFNULL(type1, 1) AS type1mod, IFNULL(type2, 1) AS 
                type2mod FROM pokemon WHERE name LIKE ? ORDER BY name LIMIT 1";
    return query($db_con, $query, array($name.'%'));
}

function queryId($id) {
    global $db_con;
    $query = "SELECT *, IFNULL(type1, 1) AS type1mod, IFNULL(type2, 1) AS 
                type2mod FROM pokemon WHERE id = ? LIMIT 1";
    return query($db_con, $query, array($id));
}

function getPokemon($pokemon) {
    global $db_con;

    if (isset($pokemon[0])) {
        $pokemon = $pokemon[0];
    }
    else {
        return "{}";
    }

    $output = array();
    if (isset($pokemon['type1mod']) && isset($pokemon['type2mod'])) {
        // Add pokemon info to the output
        $output['id'] = $pokemon['id'];
        $output['name'] = $pokemon['name'];
        $output['type1'] = $pokemon['type1'];
        $output['type2'] = $pokemon['type2'];
        $output['image'] = $pokemon['image'];

        // Get super effective types
        $query = "SELECT attack, ROUND(" . $pokemon['type1mod'] . "*" . 
            $pokemon['type2mod'] . "*100) AS effectiveness FROM types HAVING 
            effectiveness > 100 ORDER BY effectiveness DESC, attack";
        $result = query($db_con, $query);
        $output['weaknesses'] = $result;

        // Get normal types
        $query = "SELECT attack, ROUND(" . $pokemon['type1mod'] . "*" . 
            $pokemon['type2mod'] . "*100) AS effectiveness FROM types HAVING 
            effectiveness = 100 ORDER BY effectiveness DESC, attack";
        $result = query($db_con, $query);
        $output['normal'] = $result;

        // Get not effective types
        $query = "SELECT attack, ROUND(" . $pokemon['type1mod'] . "*" . 
            $pokemon['type2mod'] . "*100) AS effectiveness FROM types HAVING 
            effectiveness < 100 ORDER BY effectiveness DESC, attack";
        $result = query($db_con, $query);
        $output['resistances'] = $result;
    }
    return json_encode($output);
}

function initDB($host, $username, $password, $db_name) {
    $dsn = "mysql:host=$host;dbname=$db_name";
    try {
        $conn = new PDO($dsn,$username,$password);
        $conn->setAttribute(PDO::ATTR_ERRMODE,PDO::ERRMODE_EXCEPTION);

    }
    catch (PDOException $e) {
        $conn = null;
        exit("Connection failed: ".$e->getMessage());
    }
    return $conn;
}

function query($conn, $query,$params=array()){
    try {
        $statement = $conn->prepare($query);
        $statement->execute($params);
        $results = $statement->fetchAll(PDO::FETCH_ASSOC);
        return $results;
    }
    catch(PDOException $e) {
        throw new Exception($e->getMessage());
    }  
}
?>
