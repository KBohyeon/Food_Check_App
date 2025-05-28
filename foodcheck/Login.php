<?php
error_reporting(E_ALL);
ini_set('display_errors',1);

include('db_conn.php');

$userID = $_POST['userID'] ?? '';
$userPassword = $_POST['userPassword'] ?? '';

$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

if ($userID != "") {

    $sql = "SELECT * FROM `user` WHERE userID = :userID AND userPassword = SHA1(:userPassword)";
    $stmt = $con->prepare($sql);
    $stmt->bindParam(':userID', $userID);
    $stmt->bindParam(':userPassword', $userPassword);
    $stmt->execute();

    if ($stmt->rowCount() == 0){
        echo "'$userID' no id OR wrong password.";
    } else {
        $data = [];

        while($row = $stmt->fetch(PDO::FETCH_ASSOC)){
            array_push($data, [
                'userID' => $row["userID"],
                'userPassword' => $row["userPassword"],
                'email' => $row["email"],
                'phoneNumber' => $row["phoneNumber"],
                'userSort' => $row["userSort"]
            ]);
        }

        if (!$android) {
            echo "<pre>"; print_r($data); echo "</pre>";
        } else {
            header('Content-Type: application/json; charset=utf8');
            echo json_encode(["user" => $data], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
        }
    }

} else {
    echo " login. ";
}

if (!$android){
?>
<html>
   <body>
      <form action="<?= $_SERVER['PHP_SELF'] ?>" method="POST">
         ID: <input type="text" name="userID" />
         PASSWORD: <input type="text" name="userPassword" />
         <input type="submit" />
      </form>
   </body>
</html>
<?php
}
?>
